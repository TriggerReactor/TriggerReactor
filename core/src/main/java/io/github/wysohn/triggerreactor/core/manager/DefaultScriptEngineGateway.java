package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.logging.Logger;

public class DefaultScriptEngineGateway implements IScriptEngineGateway {
    private final ScriptEngineManagerProxy proxy;
    private final Logger logger;

    public DefaultScriptEngineGateway(ScriptEngineManagerProxy proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Override
    public ScriptEngine getEngine() {
        ScriptEngineManager sem = proxy.get();

        ScriptEngine engine = sem.getEngineByName("graal.js");
        if (engine != null) {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowAllAccess", true);
            logger.info("Using ScriptEngine: " + engine.getFactory().getEngineName());
            return engine;
        }

        engine = sem.getEngineByExtension("js");
        if (engine != null) {
            logger.info("Using ScriptEngine: " + engine.getFactory().getEngineName());
            return engine;
        }

        return null;
    }

    @Override
    public String getEngineName() {
        return "From ClassLoader: Graal.js or Nashorn";
    }
}
