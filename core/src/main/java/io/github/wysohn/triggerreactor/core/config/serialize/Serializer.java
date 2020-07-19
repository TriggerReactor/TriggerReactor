package io.github.wysohn.triggerreactor.core.config.serialize;

import io.github.wysohn.gsoncopy.JsonDeserializer;
import io.github.wysohn.gsoncopy.JsonObject;
import io.github.wysohn.gsoncopy.JsonSerializationContext;
import io.github.wysohn.gsoncopy.JsonSerializer;

public interface Serializer<T> extends JsonSerializer<T>, JsonDeserializer<T> {
    String SER_KEY = "$serkey";
    String SER_VALUE = "$serval";

    static JsonObject serialize(Class<?> type, Object value, JsonSerializationContext context) {
        JsonObject ser = new JsonObject();
        ser.addProperty(SER_KEY, type.getName());
        ser.add(SER_VALUE, context.serialize(value));
        return ser;
    }
}
