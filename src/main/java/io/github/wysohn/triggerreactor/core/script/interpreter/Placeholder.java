package io.github.wysohn.triggerreactor.core.script.interpreter;

public abstract class Placeholder extends SynchronizableTask{
    /**
     * Replace this placeholder with appropriate value.
     * @param context the context where placeholder was used. It's Event for Bukkit API.
     * @param args arguments to be used
     * @return replaced value. Should be always primitive type. Can be null if something went wrong
     */
    public abstract Object parse(Object context, Object... args) throws Exception;
}
