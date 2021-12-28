package io.github.wysohn.triggerreactor.core.config.serialize;

import io.github.wysohn.gsoncopy.*;

import java.lang.reflect.Type;

public class DefaultSerializer<T> implements Serializer<T> {
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return gson.fromJson(json, typeOfT);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return gson.toJsonTree(src, typeOfSrc);
    }

    private static final Gson gson = new Gson();
}
