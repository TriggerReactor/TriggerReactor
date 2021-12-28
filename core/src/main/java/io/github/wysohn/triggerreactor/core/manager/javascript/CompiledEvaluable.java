package io.github.wysohn.triggerreactor.core.manager.javascript;

import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.*;

public class CompiledEvaluable<R> {
    private final String functionName;

    private CompiledScript compiled;
    private Object function;

    public CompiledEvaluable(String functionName) {
        this.functionName = functionName;
    }

    public void compile(ScriptEngine engine, String sourceCode) throws ScriptException {
        // warm up
        if (compiled == null || compiled.getEngine() != engine) {
            Compilable compiler = (Compilable) engine;
            compiled = compiler.compile(sourceCode);
        }
    }

    public void evaluate(ScriptEngine engine, ScriptContext scriptContext) throws Exception {
        ValidationUtil.notNull(compiled);

        if (function == null || compiled.getEngine() != engine) {
            compiled.eval(scriptContext);
            function = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).get(functionName);
            if (function == null)
                throw new Exception(functionName + ".js does not have 'function " + functionName + "()'.");
        }
    }

    public Object invokeFunction(ScriptEngine engine,
                                 ScriptContext scriptContext,
                                 Timings.Timing time,
                                 Object argObj) throws ScriptException, NoSuchMethodException {
        ValidationUtil.notNull(compiled);
        ValidationUtil.notNull(function);

        Object result = null;

        try (Timings.Timing t = time.begin(true)) {
            engine.setContext(scriptContext);
            result = ((Invocable) engine).invokeFunction(functionName, argObj);
        }

        return (R) result;
    }
}
