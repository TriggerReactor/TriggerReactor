package io.github.wysohn.triggerreactor.core.script.interpreter;

public interface IPlaceholderMap extends IJSExecutableMap{
    Placeholder get(Object key);
}
