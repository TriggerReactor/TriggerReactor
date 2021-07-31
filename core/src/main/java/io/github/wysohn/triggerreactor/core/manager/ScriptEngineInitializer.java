package io.github.wysohn.triggerreactor.core.manager;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.function.Function;

public class ScriptEngineInitializer {
    /**
     * Initializes pre-defined functions and variables for Executors.
     *
     * @throws ScriptException
     */
    public static void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        sem.put("Char", (Function<String, Character>) t -> t.charAt(0));
    }
}
