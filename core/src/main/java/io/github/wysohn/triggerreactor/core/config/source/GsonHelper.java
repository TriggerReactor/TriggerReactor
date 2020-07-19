package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.gsoncopy.Gson;
import io.github.wysohn.gsoncopy.TypeAdapter;
import io.github.wysohn.gsoncopy.internal.LinkedTreeMap;
import io.github.wysohn.gsoncopy.stream.JsonReader;
import io.github.wysohn.gsoncopy.stream.JsonToken;
import io.github.wysohn.gsoncopy.stream.JsonWriter;
import io.github.wysohn.triggerreactor.core.config.serialize.CustomSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GsonHelper {
    public static Map<String, Object> readJson(JsonReader jsonReader, Gson gson) throws IOException {
        Map<String, Object> map = new LinkedTreeMap<>();

        if (!jsonReader.hasNext())
            return null;

        JsonToken token = jsonReader.peek();
        if (token != JsonToken.BEGIN_OBJECT)
            return null;

        jsonReader.beginObject();
        readJson(map, jsonReader, gson);
        jsonReader.endObject();

        return map;
    }

    private static void readJson(Map<String, Object> map, JsonReader jsonReader, Gson gson)
            throws IOException {
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            Object value = convert(jsonReader, gson);
            map.put(fieldName, value);
        }
    }

    private static Object convert(JsonReader jsonReader, Gson gson) throws IOException {
        JsonToken token = jsonReader.peek();
        switch (token) {
            case BEGIN_OBJECT:
                jsonReader.beginObject();

                Map<String, Object> object = new LinkedTreeMap<>();
                TypeAdapter<?> adapter = null;
                while (jsonReader.hasNext()) {
                    String key = jsonReader.nextName();
                    if (CustomSerializer.SER_KEY.equals(key)) { // custom serializer found
                        String className = jsonReader.nextString();
                        try {
                            adapter = gson.getAdapter(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    if (adapter != null) {
                        if (!CustomSerializer.SER_VALUE.equals(key))
                            throw new RuntimeException("Found serializable key but field name of value is not " + CustomSerializer.SER_VALUE);

                        final Object read = adapter.read(jsonReader);
                        if (jsonReader.hasNext())
                            throw new RuntimeException("Finished deserialization, yet there are still more fields to read.");
                        jsonReader.endObject();
                        return read;
                    } else {
                        object.put(key, convert(jsonReader, gson)); // just a json object
                    }
                }
                jsonReader.endObject();
                return object;

            case BEGIN_ARRAY:
                List<Object> list = new ArrayList<>();
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    list.add(convert(jsonReader, gson));
                }
                jsonReader.endArray();
                return list;

            case STRING:
                return jsonReader.nextString();

            case NUMBER:
                String value = jsonReader.nextString();
                if (value.contains(".")) {
                    return Double.parseDouble(value);
                } else {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        return Long.parseLong(value);
                    }
                }

            case BOOLEAN:
                return jsonReader.nextBoolean();

            case NULL:
                jsonReader.nextNull();
                return null;

            default:
                throw new IllegalStateException();
        }
    }

    public static void writeJson(Map<String, Object> map, JsonWriter out, Gson gson) throws IOException {
        if (map == null) {
            out.beginObject();
            out.endObject();
            return;
        }

        gson.toJson(map, Map.class, out);
    }
}
