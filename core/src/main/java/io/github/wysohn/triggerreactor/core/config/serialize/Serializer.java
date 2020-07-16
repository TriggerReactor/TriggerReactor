package io.github.wysohn.triggerreactor.core.config.serialize;

import io.github.wysohn.gsoncopy.JsonDeserializer;
import io.github.wysohn.gsoncopy.JsonSerializer;

public interface Serializer<T> extends JsonSerializer<T>, JsonDeserializer<T> {
    String SER_KEY = "$serkey";
    String SER_VALUE = "$serval";
}
