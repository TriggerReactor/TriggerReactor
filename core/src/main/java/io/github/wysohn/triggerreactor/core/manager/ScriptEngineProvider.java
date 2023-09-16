package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Singleton
public class ScriptEngineProvider {
    private final List<IScriptEngineGateway> gateways;

    @Inject
    public ScriptEngineProvider(Set<IScriptEngineGateway> gateways) {
        this.gateways = new ArrayList<>(gateways);
        this.gateways.sort(Comparator.comparingInt(IScriptEngineGateway::getPriority).reversed());
    }

    public ScriptEngine getEngine() {
        for (IScriptEngineGateway gateway : gateways) {
            ScriptEngine engine = gateway.getEngine();
            if (engine != null)
                return engine;
        }

        throw new RuntimeException("No JavaScript engine found! " + gateways.stream()
                .map(IScriptEngineGateway::getEngineName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None"));
    }
}
