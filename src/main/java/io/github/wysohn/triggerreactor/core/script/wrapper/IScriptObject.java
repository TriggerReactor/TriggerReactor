package io.github.wysohn.triggerreactor.core.script.wrapper;

public interface IScriptObject {
    /**
     * upwrap this interface.
     * @return
     */
    public <T> T get();
}
