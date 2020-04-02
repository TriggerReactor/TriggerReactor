package io.github.wysohn.triggerreactor.core.manager.config.serialize;

import copy.com.google.gson.JsonDeserializationContext;
import copy.com.google.gson.JsonElement;
import copy.com.google.gson.JsonParseException;
import copy.com.google.gson.JsonSerializationContext;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;

import java.lang.reflect.Type;

public abstract class AbstractIItemStackSerializer<Wrapper extends IItemStack, ItemStack> implements Serializer<Wrapper> {
    private final Class<Wrapper> clazz;

    public AbstractIItemStackSerializer(Class<Wrapper> clazz) {
        this.clazz = clazz;
    }

    protected abstract Wrapper wrap(ItemStack IS);

    @Override
    public Wrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return wrap(deserialize(json, context));
    }

    protected abstract ItemStack deserialize(JsonElement json, JsonDeserializationContext context);

    @Override
    public JsonElement serialize(Wrapper src, Type typeOfSrc, JsonSerializationContext context) {
        ItemStack internal = src.get(); // actual ItemStack (either Bukkit, Sponge, or something else)
        return serialize(internal, context);
    }

    protected abstract JsonElement serialize(ItemStack IS, JsonSerializationContext context);
}
