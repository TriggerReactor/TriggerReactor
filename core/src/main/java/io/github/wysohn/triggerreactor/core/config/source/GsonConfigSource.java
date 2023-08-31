package io.github.wysohn.triggerreactor.core.config.source;

import com.google.inject.assistedinject.Assisted;
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

import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory.assertFile;

public class GsonConfigSource implements IConfigSource {
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).enableComplexMapKeySerialization()
            .setPrettyPrinting().serializeNulls()
            .registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, NullTypeAdapters.NULL_ADOPTER_STRING))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class, NullTypeAdapters.NULL_ADOPTER_BOOLEAN))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, NullTypeAdapters.NULL_ADOPTER_FLOAT))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, NullTypeAdapters.NULL_ADOPTER_NUMBER))
            .registerTypeAdapter(UUID.class, new UUIDSerializer())
            .registerTypeAdapter(SimpleLocation.class, new SimpleLocationSerializer())
            .registerTypeAdapter(SimpleChunkLocation.class, new SimpleChunkLocationSerializer());

    public static <T> void registerSerializer(Class<T> type, Serializer<T> serializer) {
        GSON_BUILDER.registerTypeHierarchyAdapter(type, serializer);
    }

    private static final TypeValidatorChain.Builder VALIDATOR_BUILDER = new TypeValidatorChain.Builder()
            .addChain(new DefaultValidator())
            .addChain(new UUIDValidator())
            .addChain(new SimpleLocationValidator())
            .addChain(new SimpleChunkLocationValidator());

    public static void registerValidator(ITypeValidator... validators) {
        ValidationUtil.notNull(validators);
        ValidationUtil.allNotNull(validators);
        for (ITypeValidator validator : validators) {
            VALIDATOR_BUILDER.addChain(validator);
        }
    }

    //Lock order: file -> cache
    final File file;
    private final Function<File, Reader> readerFactory;
    private final Function<File, Writer> writerFactory;
    private final Map<String, Object> cache = new HashMap<>();

    private final Gson gson = GSON_BUILDER.create();

    private final ITypeValidator typeValidator;

    private final SaveWorker saveWorker;

    private boolean deleted = false;

    @Inject
    GsonConfigSource(@Assisted SaveWorker saveWorker, @Assisted File folder, @Assisted String fileName) {
        this(saveWorker, folder, fileName, (f) -> {
            try {
                return new FileReader(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }, (f) -> {
            try {
                return new FileWriter(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public GsonConfigSource(SaveWorker saveWorker, File folder, String fileName,
                            Function<File, Reader> readerFactory,
                            Function<File, Writer> writerFactory) {
        assertFile(folder, fileName);

        ValidationUtil.notNull(saveWorker);
        ValidationUtil.validate(saveWorker.isAlive(), "SaveWorker is not alive!");
        ValidationUtil.notNull(readerFactory);
        ValidationUtil.notNull(writerFactory);

        this.saveWorker = saveWorker;
        this.file = new File(folder, fileName + ".json");
        this.readerFactory = readerFactory;
        this.writerFactory = writerFactory;
        this.typeValidator = VALIDATOR_BUILDER.build();
    }

    @Override
    public boolean fileExists() {
        // this is not a perfect way yet can cover most cases.
        return file.exists() && file.length() > 0;
    }

    private void ensureFile(File file) {
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
        synchronized (file) {
            ensureFile(file);
            try (Reader fr = this.readerFactory.apply(file);
                 BufferedReader br = new BufferedReader(fr)) {
                Map<String, Object> loaded = null;
                if (file.exists() && file.length() > 0L)
                    loaded = GsonHelper.readJson(new JsonReader(br), gson);

                synchronized (cache) {
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
        synchronized (file) {
            cacheToFile();
        }
    }

    /**
     * Blocking operation
     */
    void cacheToFile() {
        String timestamp = DATE_FORMAT.format(new Date());
        File tempFile = new File(file.getParentFile(), file.getName() + ".tmp." + timestamp);
        ensureFile(tempFile);

        try (Writer fw = this.writerFactory.apply(tempFile);
             BufferedWriter bw = new BufferedWriter(fw)) {
            String ser;

            synchronized (cache) {
                ser = gson.toJson(cache);
            }

            bw.write(ser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!tempFile.renameTo(file)) {
            System.err.println("Failed to rename temp file to " + file.getName());
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

    @Override
    public void put(String key, Object value) {
        synchronized (cache) {
            put(cache, IConfigSource.toPath(key), value);
        }

        saveWorker.flush(this);
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
        synchronized (file) {
            cacheToFile();
        }
    }

    @Override
    public synchronized void delete() {
        file.delete();
        this.deleted = true;
    }

    public synchronized boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "GsonConfigSource{ file=" + file.getName() + " }";
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

}