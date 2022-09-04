package io.github.wysohn.triggerreactor.core;

@FunctionalInterface
public interface IEventHook {
    void onEvent(Object e);
}
