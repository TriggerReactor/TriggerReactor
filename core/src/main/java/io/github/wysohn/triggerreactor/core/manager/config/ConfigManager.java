package io.github.wysohn.triggerreactor.core.manager.config;

import copy.com.google.gson.Gson;
import copy.com.google.gson.GsonBuilder;
import copy.com.google.gson.JsonSyntaxException;
import copy.com.google.gson.TypeAdapter;
import copy.com.google.gson.internal.bind.TypeAdapters;
import copy.com.google.gson.reflect.TypeToken;
import copy.com.google.gson.stream.JsonReader;
import copy.com.google.gson.stream.JsonToken;
import copy.com.google.gson.stream.JsonWriter;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.config.serialize.DefaultSerializer;
import io.github.wysohn.triggerreactor.core.manager.config.serialize.UUIDSerializer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ConfigManager extends Manager {
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();

    private final Gson gson = builder.create();

    //Lock order: file -> cache
    private final File file;
    private final Function<File, Reader> readerFactory;
    private final Function<File, Writer> writerFactory;
    private final Map<String, Object> cache = new HashMap<>();

    public ConfigManager(TriggerReactor plugin, File file) {
        this(plugin, file, f -> {
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
    
    public ConfigManager(TriggerReactor plugin, File file,
    		Function<File, Reader> readerFactory, Function<File, Writer> writerFactory) {
    	super(plugin);
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
                    cache.clear();
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
            synchronized (cache){
                gson.toJson(cache, fw);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> Optional<T> get(String key, Class<T> asType) {
        synchronized (cache) {
            return Optional.ofNullable(asType.cast(cache.get(key)));
        }
    }

    public void put(String key, Object value){
        synchronized (cache) {
            cache.put(key, value);
            exec.execute(()->{
                synchronized (file) {
                    cacheToFile();
                }
            });
        }
    }

    public Set<String> keys(){
    	synchronized(cache) {
    		return new HashSet<>(cache.keySet());
    	}
    }
    
    public boolean isSection(String key) {
    	synchronized(cache) {
    		return cache.get(key) instanceof Map;
    	}
    }

    /**
     * Shutdown the saving tasks. Blocks the thread until the scheduled tasks are done.
     */
    public void shutdown(){
        exec.shutdown();
        try {
            exec.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
    private static GsonBuilder builder = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).enableComplexMapKeySerialization()
            .serializeNulls().registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, NULL_ADOPTER_STRING))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class, NULL_ADOPTER_BOOLEAN))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, NULL_ADOPTER_NUMBER))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, NULL_ADOPTER_FLOAT))
            .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, NULL_ADOPTER_NUMBER))
            .registerTypeAdapter(UUID.class, new UUIDSerializer())
            .registerTypeAdapter(SimpleLocation.class, new DefaultSerializer<SimpleLocation>())
            .registerTypeAdapter(SimpleChunkLocation.class, new DefaultSerializer<SimpleChunkLocation>());
}
