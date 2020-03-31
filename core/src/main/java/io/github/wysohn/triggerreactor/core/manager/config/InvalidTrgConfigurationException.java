package io.github.wysohn.triggerreactor.core.manager.config;

public class InvalidTrgConfigurationException extends Exception {
    public InvalidTrgConfigurationException(String s, IConfigSource configSource) {
        super(s + " caused by " + configSource);
    }
}
