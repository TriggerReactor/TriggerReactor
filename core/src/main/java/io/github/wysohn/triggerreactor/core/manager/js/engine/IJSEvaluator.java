package io.github.wysohn.triggerreactor.core.manager.js.engine;

import java.util.Map;

public interface IJSEvaluator {

    void compile(String sourceCode);

    boolean isCompiled();

    void clearBindings();
    Map<String, Object> getBindings();
    void putBindings(String key, Object obj);
    boolean hasBindings(String key);
    void removeBindings(String key);


    void eval() throws Exception;

    Object invokeFunction(String functionName, Object argObj) throws Exception;

}
