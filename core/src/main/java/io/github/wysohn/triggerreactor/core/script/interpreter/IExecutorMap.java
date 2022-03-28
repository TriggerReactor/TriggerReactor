package io.github.wysohn.triggerreactor.core.script.interpreter;

public interface IExecutorMap extends IJSExecutableMap{
    Executor get(Object key);
}
