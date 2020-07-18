package io.github.wysohn.triggerreactor.bukkit.main.serialize;

import io.github.wysohn.gsoncopy.*;
import io.github.wysohn.triggerreactor.core.config.serialize.Serializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class BukkitConfigurationSerializer implements Serializer<ConfigurationSerializable> {
    private static final String SERIALIZED_VALUE = ConfigurationSerialization.SERIALIZED_TYPE_KEY + "val";

    @Override
    public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject ser = (JsonObject) json;

        // ignore Map without SERIALIZED_TYPE_KEY (they are simple map in such case)
        if (ser.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY) == null)
            return null;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                ser.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY).getAsString());
        map.put(SERIALIZED_VALUE,
                context.deserialize(ser.get(SERIALIZED_VALUE), Map.class));

        try {
            Map<String, ConfigurationSerializable> subs = new HashMap<>();
            ser.entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof JsonObject)
                    .forEach(entry -> Optional.ofNullable(deserialize(entry.getValue(), typeOfT, context))
                            .ifPresent(serializable -> subs.put(entry.getKey(), serializable)));
            map.putAll(subs);

            return ConfigurationSerialization.deserializeObject(map);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot deserialize " + json, ex);
        }
    }

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject ser = new JsonObject();
        ser.addProperty(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
        ser.add(SERIALIZED_VALUE, context.serialize(src.serialize(), Map.class));
        return ser;
    }
}
