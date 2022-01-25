package io.github.wysohn.triggerreactor.core.manager;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Set;
import java.util.logging.Logger;

public class ScriptEngineProvider extends Manager implements IScriptEngineProvider{
    @Inject
    Logger logger;
    @Inject
    ScriptEngineManager scriptEngineManager;
    @Inject
    Set<IScriptEngineInitializer> initializerSet;

    @Inject
    ScriptEngineProvider() {
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        // Warmup
        if(getEngine() == null){
            logger.severe("No java script engine was available. If you are using Java version above 11, the stock Java does not"
                    + " contain the java script engine as it used to be. Install GraalVM instead of the stock Java,"
                    + " or you have to download third-party plugin, such as JShader.");
            throw new RuntimeException("Script engine not found.");
        }

        for (IScriptEngineInitializer init : initializerSet) {
            init.initScriptEngine(scriptEngineManager);
        }
    }

    @Override
    public void onReload() throws RuntimeException {

    }

    @Override
    public ScriptEngine getEngine() {
        ScriptEngine engine = scriptEngineManager.getEngineByName("graal.js");
        if (engine != null) {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowAllAccess", true);
            return engine;
        }

        engine = scriptEngineManager.getEngineByName("JavaScript");
        if (engine != null) {
            return engine;
        }

        return null;
    }

}
