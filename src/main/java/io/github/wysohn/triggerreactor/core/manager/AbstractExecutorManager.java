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

    public abstract Set<Entry<String, Executor>> entrySet();

    public abstract boolean containsKey(Object key);

    public abstract Executor get(Object key);

    public abstract Map<String, Executor> getExecutorMap();

}