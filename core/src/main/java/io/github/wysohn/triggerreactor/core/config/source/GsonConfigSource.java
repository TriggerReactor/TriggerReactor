package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.gsoncopy.Gson;
import io.github.wysohn.gsoncopy.GsonBuilder;
import io.github.wysohn.gsoncopy.internal.bind.TypeAdapters;
import io.github.wysohn.gsoncopy.stream.JsonReader;
import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.NullTypeAdapters;
import io.github.wysohn.triggerreactor.core.config.serialize.*;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GsonConfigSource implements IConfigSource {
    private static final GsonBuilder builder = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).enableComplexMapKeySerialization()
            .setPrettyPrinting().serializeNulls()
            .registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, NullTypeAdapters.NULL_ADOPTER_STRING))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class, NullTypeAdapters.NULL_ADOPTER_BOOLEAN))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, NullTypeAdapters.NULL_ADOPTER_FLOAT))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeHierarchyAdapter(Set.class, new SetSerializer())
            .registerTypeAdapter(UUID.class, new UUIDSerializer())
            .registerTypeAdapter(SimpleLocation.class, new SimpleLocationSerializer())
            .registerTypeAdapter(SimpleChunkLocation.class, new SimpleChunkLocationSerializer());

    public static <T> void registerSerializer(Class<T> type, Serializer<T> serializer) {
        builder.registerTypeHierarchyAdapter(type, serializer);
    }

    private static final ExecutorService exec = Executors.newSingleThreadExecutor();

    //Lock order: file -> cache
    private final File file;
    private final Function<File, Reader> readerFactory;
    private final Function<File, Writer> writerFactory;
    private final Map<String, Object> cache = new HashMap<>();

    private final Gson gson = builder.create();

    GsonConfigSource(File file) {
        this(file, f -> {
            try {
                return new FileReader(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }, f -> {
            try {
                return new FileWriter(f);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * @param file
     * @param readerFactory
     * @param writerFactory
     * @deprecated for test. Do not use it directly unless necessary.
     */
    public GsonConfigSource(File file, Function<File, Reader> readerFactory, Function<File, Writer> writerFactory) {
        this.file = file;
        this.readerFactory = readerFactory;
        this.writerFactory = writerFactory;
    }

    @Override
    public boolean fileExists() {
        // this is not a perfect way yet can cover most cases.
        return file.exists() && file.length() > 0;
    }

    private void ensureFile() {
        if (!file.exists()) {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload() {
        ensureFile();

        synchronized (file) {
            try (Reader fr = this.readerFactory.apply(file)) {
                synchronized (cache) {
                    Map<String, Object> loaded = GsonHelper.readJson(new JsonReader(fr), gson);
                    cache.clear();
                    if (loaded != null)
                        cache.putAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disable() {
        shutdown();
    }

    @Override
    public void saveAll() {
        ensureFile();

        synchronized (file) {
            cacheToFile();
        }
    }

    /**
     * Blocking operation
     */
    private void cacheToFile() {
        try (Writer fw = this.writerFactory.apply(file)) {
            synchronized (cache) {
                String ser = gson.toJson(cache);
                fw.write(ser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> T get(Map<String, Object> map, String[] path, Class<T> asType) {
        for (int i = 0; i < path.length; i++) {
            String key = path[i];
            Object value = map.get(key);

            if (i == path.length - 1) {
                return asType.cast(value);
            } else if (value instanceof Map) {
                map = (Map<String, Object>) value;
            } else {
                return null;
            }
        }

        return null;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> asType) {
        synchronized (cache) {
            return Optional.ofNullable(get(cache, IConfigSource.toPath(key), asType));
        }
    }

    @Override
    public <T> Optional<T> get(String key) {
        synchronized (cache) {
            return Optional.ofNullable((T) get(cache, IConfigSource.toPath(key), Object.class));
        }
    }

    private void put(Map<String, Object> map, String[] path, Object value) {
        for (int i = 0; i < path.length; i++) {
            String key = path[i];

            if (i == path.length - 1) {
                map.put(key, value);
            } else {
                Object previous = map.computeIfAbsent(key, (k) -> new HashMap<>());
                if (!(previous instanceof Map))
                    throw new RuntimeException("Value found at " + key + " is not a section.");

                map = (Map<String, Object>) previous;
            }
        }
    }

    @Override
    public void put(String key, Object value) {
        synchronized (cache) {
            put(cache, IConfigSource.toPath(key), value);
            exec.execute(() -> {
                synchronized (file) {
                    cacheToFile();
                }
            });
        }
    }

    @Override
    public boolean has(String key) {
        return get(cache, IConfigSource.toPath(key), Object.class) != null;
    }

    @Override
    public Set<String> keys() {
        synchronized (cache) {
            return new HashSet<>(cache.keySet());
        }
    }

    @Override
    public boolean isSection(String key) {
        synchronized (cache) {
            return get(cache, IConfigSource.toPath(key), Object.class) instanceof Map;
        }
    }

    /**
     * Shutdown the saving tasks. Blocks the thread until the scheduled tasks are done.
     */
    public void shutdown() {
        exec.shutdownNow().forEach(Runnable::run);
        try {
            exec.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete() {
        exec.shutdownNow();
        file.delete();
    }

    @Override
    public String toString() {
        return "GsonConfigSource{" +
                "file=" + file +
                '}';
    }
}
