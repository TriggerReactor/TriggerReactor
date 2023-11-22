package io.github.wysohn.triggerreactor.core;

@FunctionalInterface
public interface IEventHook {
    /**
     * Get the priority of this hook. Lesser value means it will handle the event earlier. The specific mapping of the
     * value depends on the implementation.
     *
     * @return priority. If less than 0, it will be ignored, and the default priority will be used. Default priority
     * depends on the implementation. (NORMAL for Bukkit for example)
     * <p>
     * The default implementation always returns -1.
     */
    default int getPriority() {
        return -1;
    }


    void onEvent(Object e);
}
