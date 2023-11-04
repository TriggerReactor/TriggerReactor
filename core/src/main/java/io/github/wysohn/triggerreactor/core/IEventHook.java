package io.github.wysohn.triggerreactor.core;

public interface IEventHook {
    int getPriority();

    void onEvent(Object e);
}
