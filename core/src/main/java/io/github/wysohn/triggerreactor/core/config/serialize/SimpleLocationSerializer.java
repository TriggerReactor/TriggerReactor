package io.github.wysohn.triggerreactor.core.config.serialize;

import io.github.wysohn.gsoncopy.*;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import java.lang.reflect.Type;

public class SimpleLocationSerializer implements Serializer<SimpleLocation> {
    @Override
    public SimpleLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException {
        return SimpleLocation.valueOf(json.getAsString());
    }

    @Override
    public JsonElement serialize(SimpleLocation src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
