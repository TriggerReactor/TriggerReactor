package io.github.wysohn.triggerreactor.core.manager.trigger;

public enum TriggerConfigKey {
    KEY_SYNC("sync"),
    ;

    private final String key;

    TriggerConfigKey(String key) {
        this.key = key;
    }

    public String getKey(){
        return key;
    }
}
