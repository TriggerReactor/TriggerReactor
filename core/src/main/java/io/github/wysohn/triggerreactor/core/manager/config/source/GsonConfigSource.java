package io.github.wysohn.triggerreactor.core.manager.config.source;

import io.github.wysohn.gsoncopy.*;
import io.github.wysohn.gsoncopy.internal.bind.TypeAdapters;
import io.github.wysohn.gsoncopy.reflect.TypeToken;
import io.github.wysohn.gsoncopy.stream.JsonReader;
import io.github.wysohn.gsoncopy.stream.JsonToken;
import io.github.wysohn.gsoncopy.stream.JsonWriter;
import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.serialize.DefaultSerializer;
import io.github.wysohn.triggerreactor.core.manager.config.serialize.MapDeserializer;
import io.github.wysohn.triggerreactor.core.manager.config.serialize.UUIDSerializer;
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
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();
    private static final TypeAdapter<String> NULL_ADOPTER_STRING = new TypeAdapter<String>() {

        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token == JsonToken.NULL) {
                in.nextNull();
                return "";
            } else if (token == JsonToken.NUMBER || token == JsonToken.STRING) {
                return in.nextString();
            } else {
                throw new JsonSyntaxException(token + " is not valid value for String!");
            }
        }

    };
    private static final TypeAdapter<Boolean> NULL_ADOPTER_BOOLEAN = new TypeAdapter<Boolean>() {

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token == JsonToken.NULL) {
                in.nextNull();
                return false;
            } else if (token == JsonToken.BOOLEAN) {
                return in.nextBoolean();
            } else {
                throw new JsonSyntaxException(token + " is not valid value for Boolean!");
            }
        }

    };
    private static final TypeAdapter<Number> NULL_ADOPTER_NUMBER = new TypeAdapter<Number>() {

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            if (value == null) {
                out.value(0);
            } else {
                out.value(value);
            }
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            JsonToken token = in.peek();

            if (token == JsonToken.NULL) {
                in.nextNull();
                return 0;
            } else if (token == JsonToken.NUMBER) {
                String value = in.nextString();
                if (value.contains("."))
                    return Double.parseDouble(value);
                else
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        return Long.parseLong(value);
                    }
            } else {
                throw new JsonSyntaxException(token + " is not valid value for Number!");
            }
        }

    };
    private static final TypeAdapter<Float> NULL_ADOPTER_FLOAT = new TypeAdapter<Float>() {

        @Override
        public void write(JsonWriter out, Float value) throws IOException {
            if (value == null) {
                out.value(0);
            } else {
                out.value(value);
            }
        }

        @Override
        public Float read(JsonReader in) throws IOException {
            JsonToken token = in.peek();

            if (token == JsonToken.NULL) {
                in.nextNull();
                return 0f;
            } else if (token == JsonToken.NUMBER) {
                String value = in.nextString();
                return Float.parseFloat(value);
            } else {
                throw new JsonSyntaxException(token + " is not valid value for Float!");
            }
        }

    };
    private static final GsonBuilder builder = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).enableComplexMapKeySerialization()
            .setPrettyPrinting().serializeNulls()
            .registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, NULL_ADOPTER_STRING))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class, NULL_ADOPTER_BOOLEAN))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, NULL_ADOPTER_FLOAT))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, NULL_ADOPTER_NUMBER))
            .registerTypeAdapter(UUID.class, new UUIDSerializer())
            .registerTypeAdapter(SimpleLocation.class, new DefaultSerializer<SimpleLocation>())
            .registerTypeAdapter(SimpleChunkLocation.class, new DefaultSerializer<SimpleChunkLocation>());
    private static final Map<Class<?>, MapDeserializer<?>> deserializerMap = new HashMap<>();

    public static <T> void registerTypeAdapter(Class<T> clazz, JsonSerializer<T> serializer) {
        builder.registerTypeHierarchyAdapter(clazz, serializer);
    }

    public static <T> void registerTypeAdapter(Class<T> clazz, MapDeserializer<T> mapDeserializer) {
        deserializerMap.put(clazz, mapDeserializer);
    }

    /**
     * Check if the given class is serializable. Do not use this to check data consistency as it may have performance downside.
     * Ideally, use it only once (on instantiation) to ensure that the provided class can be serialized.
     *
     * @param clazz the class
     * @throws IllegalArgumentException throw if 'clazz' is not serializable. See {@link Gson#getAdapter(Class)}.
     */
    public static void assertSerializable(Class<?> clazz) {
        builder.create().getAdapter(clazz);
    }

    private final Gson gson = builder.create();
    //Lock order: file -> cache
    private final File file;
    private final Function<File, Reader> readerFactory;
    private final Function<File, Writer> writerFactory;
    private final Map<String, Object> cache = new HashMap<>();

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
                    Map<String, Object> loaded = gson.fromJson(fr, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    if (loaded != null) {
                        cache.clear();
                        cache.putAll(loaded);
                    }
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

    private Class<?> getDeserializableType(Queue<Class<?>> queue) {
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            if (!Objects.equals(Object.class, current) && deserializerMap.containsKey(current))
                return current;

            Optional.ofNullable(current.getSuperclass())
                    .ifPresent(queue::add);
            Optional.of(current.getInterfaces())
                    .map(Arrays::asList)
                    .ifPresent(queue::addAll);
        }

        return null;
    }

    /**
     * Get a class which a deserializer is registered for it. The search start from most specific to general (BFS).
     * In other words, it starts by class itself, its direct superclass and interfaces, and so on.
     *
     * @param current the class to check
     * @return the most specific class which can be deserialized.
     */
    private Class<?> getDeserializableType(Class<?> current) {
        if (current == null)
            return null;

        Queue<Class<?>> queue = new LinkedList<>();
        queue.add(current);

        return getDeserializableType(queue);
    }

    private <T> T get(Map<String, Object> map, String[] path, Class<T> asType) {
        for (int i = 0; i < path.length; i++) {
            String key = path[i];
            Object value = map.get(key);

            if (i == path.length - 1) {
                if (value instanceof Map) {
                    MapDeserializer<?> deserializer = getMapDeserializer(asType);

                    if (deserializer == null) {
                        throw new RuntimeException("Cannot deserialize " + value + ". Deserializer not registered.");
                    } else {
                        return (T) deserializer.deserialize((Map) value);
                    }
                } else {
                    return asType.cast(value);
                }
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

//    public static void main(String[] ar){
//        System.out.println(Arrays.toString(toPath("..ab.c..de.f....ger")));
//        System.out.println(Arrays.toString(toPath(".c..de.f....ger...")));
//    }

    /**
     * Check whether the 'aClass' can be deserialized back from the json Object to its original data type.
     * The deserializer must be registered since gson will simply convert the entire json file into a Map, so if there
     * is an object that has to be deserialized, the deserializer must exist to avoid ClassCastException (because
     * the deserialized Map contains raw data, not the actual object which the data was serialized from).
     * <p>
     * Primitive types (Collection, primitive array, Map, String, Number, Boolean) always return true.
     * Refer to {@link TypeAdapters} for details.
     *
     * @param aClass
     * @return true if 'aClass' has deserializer registered or if is primitive types; false otherwise.
     */
    private boolean isDeserializable(Class<?> aClass) {
        // primitive types which does not require deserializer.
        if (Collection.class.isAssignableFrom(aClass)
                || aClass.isArray()
                || Map.class.isAssignableFrom(aClass)
                || String.class.isAssignableFrom(aClass)
                || Number.class.isAssignableFrom(aClass)
                || Boolean.class.isAssignableFrom(aClass))
            return true;

        // exact match
        if (deserializerMap.containsKey(aClass))
            return true;

        return getMapDeserializer(aClass) != null;
    }

    private MapDeserializer<?> getMapDeserializer(Class<?> aClass) {
        Class<?> targetType = getDeserializableType(aClass); // find possible deserializer
        if (targetType == null)
            targetType = aClass;

        MapDeserializer<?> deserializer = deserializerMap.get(targetType);
        if (deserializer != null && !deserializerMap.containsKey(aClass))
            deserializerMap.put(aClass, deserializer); // add it so we don't have to search again

        return deserializer;
    }

    private void put(Map<String, Object> map, String[] path, Object value) {
        for (int i = 0; i < path.length; i++) {
            String key = path[i];

            if (i == path.length - 1) {
                if (value != null && !isDeserializable(value.getClass()))
                    throw new RuntimeException("GsonConfigSource does not support type: " + value.getClass());

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
