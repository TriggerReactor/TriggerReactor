package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import java.util.Collection;

public interface IEventRegistry {

    boolean eventExist(String eventStr);

    Collection<String> getAbbreviations();

    /**
     * First it tries to return Event in ABBREVIATIONS if such name exists. If it wasn't found, then it simply
     * treat the eventStr as full class name and try to get the Event using {@link Class#forName(String)} method.
     * ex) 1. onJoin -> 2. org.bukkit.event.player.PlayerJoinEvent
     *
     * @param eventStr name of event to search
     * @return the event class
     * @throws ClassNotFoundException throws if search fails or the result event is
     *                                a event that cannot receive events (abstract events).
     */
    Class<?> getEvent(String eventStr) throws ClassNotFoundException;

    void unregisterEvent(CustomTriggerManager.EventHook eventHook);

    /**
     * Hook event to handle it manually.
     *
     * @param clazz
     * @param eventHook
     */
    void registerEvent(Class<?> clazz, CustomTriggerManager.EventHook eventHook);

    void unregisterAll();
}
