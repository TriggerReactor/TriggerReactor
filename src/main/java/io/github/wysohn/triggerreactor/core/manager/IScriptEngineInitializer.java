package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;
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

    /**
     * Extract and put necessary variables needed for the Executors to work properly. For Bukkit API for example,
     * you will have to extract 'player' variable manually for inventory events as Player instance is not saved in
     * the field of Inventory evnet classes.
     * @param variables the local variable map.
     * @param e the context.
     */
    void extractCustomVariables(Map<String, Object> variables, Object e);

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
