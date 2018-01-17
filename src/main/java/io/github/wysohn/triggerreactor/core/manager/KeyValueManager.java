package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface KeyValueManager<V> {

    V get(Object key);

    boolean containsKey(Object key);

    Set<Entry<String, V>> entrySet();

    /**
     * Get map directly associated with this manager.
     * @return
     */
    Map<String, V> getBackedMap();

}