package io.github.wysohn.triggerreactor.core.manager.js.engine;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;

public class CompiledScriptEngine implements IJSEvaluator {

    private ScriptContext scriptContext;
    private Bindings bindings;
    private String sourceCode;

    private final ScriptEngine engine;
    private CompiledScript compiled = null;

    public CompiledScriptEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    @Override
    public void compile(String sourceCode) {
        this.sourceCode = sourceCode;

        synchronized (this.engine) {
            if (this.engine instanceof Compilable) {
                Compilable compiler = (Compilable) this.engine;
                try {
                    compiled = compiler.compile(sourceCode);
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        clearBindings();
    }

    @Override
    public boolean isCompiled() {
        return compiled == null;
    }

    @Override
    public void clearBindings() {
        bindings = engine.createBindings();
    }

    @Override
    public Map<String, Object> getBindings() {
        return new HashMap<>(bindings);
    }

    @Override
    public void putBindings(String key, Object obj) {
        bindings.put(key, obj);
    }

    @Override
    public boolean hasBindings(String key) {
        return bindings.containsKey(key);
    }

    @Override
    public void removeBindings(String key) {
        bindings.remove(key);
    }

    @Override
    public void eval() throws Exception {
        scriptContext = new SimpleScriptContext();
        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scriptContext.setBindings(engine.getBindings(ScriptContext.GLOBAL_SCOPE),
                ScriptContext.GLOBAL_SCOPE);

        if (compiled != null)
            compiled.eval(scriptContext);
        else
            engine.eval(this.sourceCode, scriptContext);
    }

    @Override
    public Object invokeFunction(String functionName, Object argObj) throws Exception {
        engine.setContext(scriptContext);
        return ((Invocable) engine).invokeFunction(functionName, argObj);
    }

    @Override
    public int hashCode() {
        return engine.hashCode();
    }
}
