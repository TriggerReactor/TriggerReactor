package io.github.wysohn.triggerreactor.core.manager.trigger;

/**
 * Collection of keys used in trigger configuration.
 * <p>
 * From now on, all keys must be camalCase.
 */
public enum TriggerConfigKey {
    KEY_SYNC("sync", "Sync"),

    KEY_TRIGGER_COMMAND_TABS("tabs"),
    KEY_TRIGGER_COMMAND_PERMISSION("permissions"),
    KEY_TRIGGER_COMMAND_ALIASES("aliases"),

    KEY_TRIGGER_INVENTORY_TITLE("title", "Title"),
    KEY_TRIGGER_INVENTORY_SIZE("size", "Size"),
    KEY_TRIGGER_INVENTORY_ITEMS("items", "Items"),

    KEY_TRIGGER_AREA_SMALLEST("smallest", "Smallest"),
    KEY_TRIGGER_AREA_LARGEST("largest", "Largest"),

    KEY_TRIGGER_REPEATING_AUTOSTART("autoStart", "AutoStart"),
    KEY_TRIGGER_REPEATING_INTERVAL("interval", "Interval"),

    KEY_TRIGGER_CUSTOM_EVENT("event", "Event"),

    KEY_TRIGGER_TEST_INTEGER("first"),
    KEY_TRIGGER_TEST_STRING("second"),
    KEY_TRIGGER_TEST_BOOLEAN("third"),
    ;

    private final String key;
    private final String oldKey;

    TriggerConfigKey(String key) {
        this(key, null);
    }

    TriggerConfigKey(String key, String oldKey) {
        this.key = key;
        this.oldKey = oldKey;
    }

    public String getKey() {
        return key;
    }

    /**
     * Get the key value but the index appended to the end.
     * getKey(1) -> "myKey.1"
     *
     * @param index
     * @return
     */
    public String getKey(int index) {
        return key + "." + index;
    }

    /**
     * Get the old key.
     *
     * @deprecated This is for migration purpose only. Use {@link #getKey()} instead.
     */
    public String getOldKey() {
        return oldKey;
    }

    /**
     * Get the key value but the index appended to the end.
     * getOldKey(1) -> "myKey.1"
     *
     * @param index
     * @deprecated This is for migration purpose only. Use {@link #getOldKey(int)} instead.
     */
    public String getOldKey(int index) {
        return oldKey == null ? null : oldKey + "." + index;
    }
}
