package io.github.wysohn.triggerreactor.sponge.manager;

import java.util.Map;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.spongepowered.api.Sponge;

import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;

public interface SpongeScriptEngineInitializer extends IScriptEngineInitializer {

    @Override
    default void extractCustomVariables(Map<String, Object> variables, Object e) {
        //TODO will come back to this if necessary
    }

    @Override
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);
        IScriptEngineInitializer.registerClass(sem, Sponge.class);
    }
}
