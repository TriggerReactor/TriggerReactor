package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.gsoncopy.Gson;
import io.github.wysohn.gsoncopy.GsonBuilder;
import io.github.wysohn.gsoncopy.internal.bind.TypeAdapters;
import io.github.wysohn.gsoncopy.stream.JsonReader;
import io.github.wysohn.triggerreactor.core.config.NullTypeAdapters;
import io.github.wysohn.triggerreactor.core.config.serialize.Serializer;
import io.github.wysohn.triggerreactor.core.config.serialize.SimpleChunkLocationSerializer;
import io.github.wysohn.triggerreactor.core.config.serialize.SimpleLocationSerializer;
import io.github.wysohn.triggerreactor.core.config.serialize.UUIDSerializer;
import io.github.wysohn.triggerreactor.core.config.validation.DefaultValidator;
import io.github.wysohn.triggerreactor.core.config.validation.SimpleChunkLocationValidator;
import io.github.wysohn.triggerreactor.core.config.validation.SimpleLocationValidator;
import io.github.wysohn.triggerreactor.core.config.validation.UUIDValidator;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GsonConfigSource implements IConfigSource {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    //Lock order: file -> cache
    private final File file;
    private final Function<File, Reader> readerFactory;
    private final Function<File, Writer> writerFactory;
    private final Map<String, Object> cache = new HashMap<>();
    private final Gson gson = GSON_BUILDER.create();
    private final ITypeValidator typeValidator;

    public GsonConfigSource(File file) {
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
        ValidationUtil.notNull(file);
        ValidationUtil.notNull(readerFactory);
        ValidationUtil.notNull(writerFactory);

        this.file = file;
        this.readerFactory = readerFactory;
        this.writerFactory = writerFactory;
        this.typeValidator = VALIDATOR_BUILDER.build();
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    @Override
    public void onEnable() {
        ensureFile();
    }

    @Override
    public void onReload() {
        ensureFile();

        synchronized (file) {
            try (Reader fr = this.readerFactory.apply(file)) {
                synchronized (cache) {
                    Map<String, Object> loaded = null;
                    if (file.exists() && file.length() > 0L)
                        loaded = GsonHelper.readJson(new JsonReader(fr), gson);

                    cache.clear();
                    if (loaded != null)
                        cache.putAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    /**
     * Shutdown the saving tasks. Blocks the thread until the scheduled tasks are done.
     */
    public void shutdown() {
        exec.shutdown();
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
    public boolean fileExists() {
        // this is not a perfect way yet can cover most cases.
        return file.exists() && file.length() > 0;
    }

    @Override
    public <T> Optional<T> get(String key) {
        synchronized (cache) {
            return Optional.ofNullable((T) get(cache, IConfigSource.toPath(key), Object.class));
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> asType) {
        synchronized (cache) {
            return Optional.ofNullable(get(cache, IConfigSource.toPath(key), asType));
        }
    }

    @Override
    public boolean has(String key) {
        return get(cache, IConfigSource.toPath(key), Object.class) != null;
    }

    @Override
    public boolean isSection(String key) {
        synchronized (cache) {
            return get(cache, IConfigSource.toPath(key), Object.class) instanceof Map;
        }
    }

    @Override
    public Set<String> keys() {
        synchronized (cache) {
            return new HashSet<>(cache.keySet());
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
    public void saveAll() {
        ensureFile();

        synchronized (file) {
            cacheToFile();
        }
    }

    private void put(Map<String, Object> map, String[] path, Object value) {
        for (int i = 0; i < path.length; i++) {
            String key = path[i];

            if (i == path.length - 1) {
                if (value == null) {
                    map.remove(key);
                } else if (value.getClass().isArray()) {
                    List l = new LinkedList();
                    for (int k = 0; k < Array.getLength(value); k++) {
                        Object elem = Array.get(value, k);
                        if (!typeValidator.isSerializable(elem))
                            throw new RuntimeException(Arrays.toString(path) + "< " + elem + " is not serializable.");

                        l.add(elem);
                    }
                    map.put(key, l);
                } else {
                    if (!typeValidator.isSerializable(value))
                        throw new RuntimeException(Arrays.toString(path) + "< " + value + " is not serializable.");

                    map.put(key, value);
                }
            } else {
                Object previous = map.computeIfAbsent(key, (k) -> new HashMap<>());
                if (!(previous instanceof Map))
                    throw new RuntimeException("Value found at " + key + " is not a section.");

                map = (Map<String, Object>) previous;
            }
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
        } catch (Exception e) {
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
    public String toString() {
        synchronized (cache) {
            return toString(cache, 2);
        }
    }

    private String toString(Map<String, Object> cache, int depth) {
        if (depth < 1)
            return "...";

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            if (entry.getValue() instanceof Map) {
                builder.append(entry.getKey())
                        .append(": ")
                        .append(toString((Map<String, Object>) cache.get(entry.getKey()), depth - 1))
                        .append(' ');
            } else {
                builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(' ');
            }
        }
        builder.append("}");

        return builder.toString();
    }

    private static final GsonBuilder GSON_BUILDER = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT,
                    Modifier.STATIC)
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, NullTypeAdapters.NULL_ADOPTER_STRING))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class,
                    NullTypeAdapters.NULL_ADOPTER_BOOLEAN))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class,
                    NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class,
                    NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class,
                    NullTypeAdapters.NULL_ADOPTER_FLOAT))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class,
                    NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapter(UUID.class, new UUIDSerializer())
            .registerTypeAdapter(SimpleLocation.class, new SimpleLocationSerializer())
            .registerTypeAdapter(SimpleChunkLocation.class, new SimpleChunkLocationSerializer());
    private static final TypeValidatorChain.Builder VALIDATOR_BUILDER =
            new TypeValidatorChain.Builder().addChain(new DefaultValidator())
            .addChain(new UUIDValidator())
            .addChain(new SimpleLocationValidator())
            .addChain(new SimpleChunkLocationValidator());

    public static <T> void registerSerializer(Class<T> type, Serializer<T> serializer) {
        GSON_BUILDER.registerTypeHierarchyAdapter(type, serializer);
    }

    public static void registerValidator(ITypeValidator... validators) {
        ValidationUtil.notNull(validators);
        ValidationUtil.allNotNull(validators);
        for (ITypeValidator validator : validators) {
            VALIDATOR_BUILDER.addChain(validator);
        }
    }
}
