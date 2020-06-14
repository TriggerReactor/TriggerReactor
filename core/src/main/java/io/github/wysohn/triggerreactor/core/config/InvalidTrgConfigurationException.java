package io.github.wysohn.triggerreactor.core.config;

public class InvalidTrgConfigurationException extends Exception {
    public InvalidTrgConfigurationException(String s, IConfigSource configSource) {
        super(configSource + ": " + s);
    }
}
