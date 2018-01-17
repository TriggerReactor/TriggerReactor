package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class AbstractJavascriptBasedManager extends Manager {

    protected static final ScriptEngineManager sem = new ScriptEngineManager(null);
    public static AbstractJavascriptBasedManager instance;

    protected static ScriptEngine getNashornEngine() {
        return sem.getEngineByName("nashorn");
    }

    /**
     * Initializes pre-defined functions and variables for Executors.
     * @throws ScriptException
     */
    protected abstract void initScriptEngine() throws ScriptException;

    protected void registerClass(Class<?> clazz) throws ScriptException {
        registerClass(clazz.getSimpleName(), clazz);
    }

    protected void registerClass(String name, Class<?> clazz) throws ScriptException {
        ScriptEngine engine = getNashornEngine();
        Object value = engine.eval("Java.type('"+clazz.getName()+"');");
        sem.put(name, value);
    }

    /**
     * Extract and put necessary variables needed for the Executors to work properly. For Bukkit API for example,
     * you will have to extract 'player' variable manually for inventory events as Player instance is not saved in
     * the field of Inventory evnet classes.
     * @param variables the local variable map.
     * @param e the context.
     */
    protected abstract void extractCustomVariables(Map<String, Object> variables, Object e);

    public AbstractJavascriptBasedManager(TriggerReactor plugin) throws ScriptException {
        super(plugin);

        instance = this;
        initScriptEngine();
    }

}