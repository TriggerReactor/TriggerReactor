package io.github.wysohn.triggerreactor.core.config;

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;

public class InvalidTrgConfigurationException extends RuntimeException {
    public InvalidTrgConfigurationException(String s, IConfigSource configSource) {
        super(configSource + ": " + s);
    }
}
