package io.github.wysohn.triggerreactor.core.manager;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Set;

public class ScriptEngineProvider extends Manager implements IScriptEngineProvider{
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

        throw new RuntimeException(
                "No java script engine was available. If you are using Java version above 11, the stock Java does not"
                        + " contain the java script engine as it used to be. Install GraalVM instead of the stock Java,"
                        + " or you have to download third-party plugin, such as JShader.");
    }

}
