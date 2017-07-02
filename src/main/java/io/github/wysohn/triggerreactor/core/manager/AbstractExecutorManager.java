package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;

@SuppressWarnings("serial")
public abstract class AbstractExecutorManager extends Manager {

    public AbstractExecutorManager(TriggerReactor plugin) {
        super(plugin);
    }

    /**
     * get a copy for Executor map.
     * @return the entry set copy
     */
    public abstract Set<Entry<String, Executor>> entrySet();

    /**
     * check if the executor with name 'key' exist.
     * @param key the name of executor. This does not include '#' sign.
     * @return true if exists; false if not
     */
    public abstract boolean containsKey(Object key);

    /**
     * get executor with the 'key'
     * @param key the name of executor. This does not include '#' sign.
     * @return the Executor; null if not found
     */
    public abstract Executor get(Object key);

    /**
     * get the actual executor map which contains all the Executors.
     * @return map of Executors.
     */
    public abstract Map<String, Executor> getExecutorMap();

}