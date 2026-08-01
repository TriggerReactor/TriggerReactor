package io.github.wysohn.triggerreactor.core.manager.js.engine;

import org.graalvm.polyglot.*;

import java.util.HashMap;
import java.util.Map;

public class PolyglotJSEngine implements IJSEvaluator {
    private final Engine engine;
    private final ClassLoader pluginClassLoader;

    private Value bindings;

    private String sourceCode;

    private Source source;
    private Context context;
    private Value compiledScriptValue;

    public PolyglotJSEngine(Engine engine, ClassLoader pluginClassLoader) {
        this.engine = engine;
        this.pluginClassLoader = pluginClassLoader;
    }

    @Override
    public void compile(String sourceCode) {
        this.sourceCode = sourceCode;

        try {
            this.source = Source.newBuilder("js", sourceCode, "script.js")
                    .cached(true)
                    .build();
        } catch (Exception e) {
            // failed to caching
        }
    }

    @Override
    public boolean isCompiled() {
        return source != null;
    }

    @Override
    public void clearBindings() {
        context = Context.newBuilder("js")
                .engine(engine)
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .build();

        bindings = context.getBindings("js");
    }

    @Override
    public Map<String, Object> getBindings() {
        Map<String, Object> map = new HashMap<>();

        for (String key : bindings.getMemberKeys()) {

            map.put(key, convertValue(bindings.getMember(key)));

        }

        return map;
    }

    public void putBindings(String key, Object obj) {
        bindings.putMember(key, obj);
    }

    public boolean hasBindings(String key) {
        return bindings.hasMember(key);
    }

    public void removeBindings(String key) {
        bindings.removeMember(key);
    }

    @Override
    public void eval() throws Exception {
        if (source != null) {
            context.eval(source);
        } else {
            context.eval("js", sourceCode);
        }
    }

    @Override
    public Object invokeFunction(String functionName, Object argObj) throws Exception {
        Value function = context.getBindings("js").getMember(functionName);
        if (function == null || !function.canExecute()) {
            throw new NoSuchMethodException("Function not found: " + functionName);
        }

        Value result = function.execute(argObj);

        return convertValue(result);
    }

    private Object convertValue(Value value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            if (value.fitsInInt()) return value.asInt();
            if (value.fitsInLong()) return value.asLong();
            return value.asDouble();
        } else if (value.isString()) {
            return value.asString();
        }
        return value.as(Object.class);
    }
}
