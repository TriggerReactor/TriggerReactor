package io.github.wysohn.triggerreactor.core.manager.config.serialize;

import copy.com.google.gson.*;

import java.lang.reflect.Type;

public class DefaultSerializer<T> implements Serializer<T> {
    private static final Gson gson = new Gson();

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return gson.fromJson(json, typeOfT);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return gson.toJsonTree(src, typeOfSrc);
    }
}
