package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;
import io.github.wysohn.triggerreactor.core.manager.js.engine.CompiledScriptEngine;
import io.github.wysohn.triggerreactor.core.manager.js.engine.IJSEvaluator;
import io.github.wysohn.triggerreactor.core.manager.js.engine.PolyglotJSEngine;
import org.graalvm.polyglot.Engine;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Singleton
public class ScriptEngineProvider {

    @Inject
    @Named("PluginClassLoader")
    private ClassLoader pluginClassLoader;

    private final List<IScriptEngineGateway> gateways;

    @Inject
    public ScriptEngineProvider(Set<IScriptEngineGateway> gateways) {
        this.gateways = new ArrayList<>(gateways);
        this.gateways.sort(Comparator.comparingInt(IScriptEngineGateway::getPriority).reversed());
    }

    public IJSEvaluator getEngine() {
        for (IScriptEngineGateway gateway : gateways) {
            ScriptEngine engine = gateway.getEngine();
            if (engine != null)
                return new CompiledScriptEngine(engine);
        }

        Engine engine = Engine.newBuilder().build();
        boolean isAvailable = engine.getLanguages().containsKey("js");
        if (isAvailable) return new PolyglotJSEngine(engine, pluginClassLoader);

        throw new RuntimeException("No JavaScript engine found! " + gateways.stream()
                .map(IScriptEngineGateway::getEngineName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None"));
    }
}
