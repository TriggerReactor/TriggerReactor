package io.github.wysohn.triggerreactor.core.manager.js;

import javax.script.ScriptEngine;

public interface IScriptEngineGateway {
    ScriptEngine getEngine();

    String getEngineName();

    /**
     * The priority of this gateway. IScriptEngineGateways will be sorted in descending order of priority.
     * In other words, the gateway with the highest priority will be used first.
     *
     * @return the priority of this gateway (default: 0)
     */
    default int getPriority() {
        return 0;
    }
}
