package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.main.IPluginManagement;

import java.util.HashMap;
import java.util.logging.Logger;

class GlobalVariableAccessLoggingProxy extends HashMap<Object, Object> {
    private final HashMap<Object, Object> globalVariableAdapter;
    private final IPluginManagement pluginManagement;
    private final Trigger usingTrigger;
    private final Logger logger;

    public GlobalVariableAccessLoggingProxy(HashMap<Object, Object> globalVariableAdapter,
                                            IPluginManagement pluginManagement,
                                            Trigger usingTrigger,
                                            Logger logger) {
        this.globalVariableAdapter = globalVariableAdapter;
        this.pluginManagement = pluginManagement;
        this.usingTrigger = usingTrigger;
        this.logger = logger;
    }

    @Override
    public Object get(Object key) {
        Object result = globalVariableAdapter.get(key);
        if (pluginManagement.isDebugging()) {
            logger.info("Trigger " + usingTrigger + " accessed global variable " + key + " with value " + result);
        }
        return result;
    }

    @Override
    public Object put(Object key, Object value) {
        Object result = globalVariableAdapter.put(key, value);
        if (pluginManagement.isDebugging()) {
            logger.info("Trigger " + usingTrigger + " set global variable " + key + " to value " + value);
        }
        return result;
    }


    @Override
    public Object remove(Object key) {
        Object result = globalVariableAdapter.remove(key);
        if (pluginManagement.isDebugging()) {
            logger.info("Trigger " + usingTrigger + " removed global variable " + key);
        }
        return result;
    }
}
