package io.github.wysohn.triggerreactor.core.manager;

import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public interface IScriptEngineInitializer {
    /**
     * Initializes pre-defined functions and variables for Executors.
     * @throws ScriptException
     */
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        sem.put("Char", new Function<String, Character>(){
            @Override
            public Character apply(String t) {
                return t.charAt(0);
            }
        });
    }

    static void registerClass(ScriptEngineManager sem, Class<?> clazz) throws ScriptException {
        registerClass(sem, clazz.getSimpleName(), clazz);
    }

    static void registerClass(ScriptEngineManager sem, String name, Class<?> clazz) throws ScriptException {
        ScriptEngine engine = getNashornEngine(sem);
        Object value = engine.eval("Java.type('"+clazz.getName()+"');");
        sem.put(name, value);
    }

    static ScriptEngine getNashornEngine(ScriptEngineManager sem) {
        return sem.getEngineByName("nashorn");
    }
}
