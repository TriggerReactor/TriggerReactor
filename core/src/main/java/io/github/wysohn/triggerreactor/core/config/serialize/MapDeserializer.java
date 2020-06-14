package io.github.wysohn.triggerreactor.core.config.serialize;

import java.util.Map;

public interface MapDeserializer<T> {
    T deserialize(Map<String, Object> map);
}
