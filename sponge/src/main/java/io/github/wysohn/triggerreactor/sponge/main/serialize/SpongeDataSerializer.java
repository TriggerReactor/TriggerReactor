package io.github.wysohn.triggerreactor.sponge.main.serialize;

import io.github.wysohn.gsoncopy.*;
import io.github.wysohn.triggerreactor.core.config.serialize.Serializer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.item.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SpongeDataSerializer implements Serializer<DataSerializable> {
    @Override
    public DataSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject ser = (JsonObject) json;

        DataContainer container = DataContainer.createNew();
        ser.entrySet().forEach(entry -> {
            String s = entry.getKey();
            Object o = context.deserialize(entry.getValue(), Map.class);
            container.set(DataQuery.of(s.split("\\.")), o);
        });
        try {
            return ItemStack.builder()
                    .fromContainer(container)
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot deserialize " + json, ex);
        }
    }

    @Override
    public JsonElement serialize(DataSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        DataContainer container = src.toContainer();
        Map<String, Object> map = new HashMap<>();
        container.getValues(true)
                .entrySet()
                .stream()
                .filter(entry -> !(entry.getValue() instanceof Map))
                .forEach(entry -> {
                    DataQuery dataQuery = entry.getKey();
                    Object o = entry.getValue();
                    map.put(dataQuery.toString(), o);
                });
        return context.serialize(map);
    }
}
