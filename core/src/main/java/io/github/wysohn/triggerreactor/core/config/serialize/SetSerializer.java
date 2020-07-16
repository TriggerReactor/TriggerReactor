package io.github.wysohn.triggerreactor.core.config.serialize;

import io.github.wysohn.gsoncopy.JsonArray;
import io.github.wysohn.gsoncopy.JsonDeserializationContext;
import io.github.wysohn.gsoncopy.JsonElement;
import io.github.wysohn.gsoncopy.JsonSerializationContext;

import java.util.HashSet;
import java.util.Set;

public class SetSerializer extends CustomSerializer<Set<?>> {
    public SetSerializer() {
        super(Set.class);
    }

    @Override
    public Set<?> deserialize(JsonElement json, JsonDeserializationContext context) {
        JsonArray arr = (JsonArray) json;
        Set<?> set = new HashSet<>();
        arr.forEach(jsonElement -> set.add(context.deserialize(jsonElement, Object.class)));
        return set;
    }

    @Override
    public JsonElement serialize(Set<?> src, JsonSerializationContext context) {
        JsonArray arr = new JsonArray();
        src.forEach(obj -> arr.add(context.serialize(obj)));
        return arr;
    }
}
