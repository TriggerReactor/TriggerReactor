package io.github.wysohn.triggerreactor.bukkit.external;

import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.*;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.Set;

@Singleton
public class JShaderScriptEngineGateway implements IScriptEngineGateway {

    @Inject
    private Server server;
    @Inject
    private IExceptionHandle exceptionHandle;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private Set<ScriptEngineInitializer> scriptEngineInitializers;

    private boolean initialized = false;
    private ScriptEngineFactory factory;
    private Bindings globalScope = new SimpleBindings();

    @Inject
    private JShaderScriptEngineGateway() {
    }

    @Override
    public ScriptEngine getEngine() {
        PluginManager pluginManager = server.getPluginManager();

        // unfortunately, because DeluxeMenu overwrites the ScriptEngineManager, we have to do this
        //  without going through the ScriptEngineManager. If JShader is present, then
        //  we can assume that NashornScriptEngineFactory is present as well.
        if (!initialized && factory == null && pluginManager.isPluginEnabled("JShader")) {
            try {
                Class<?> clazz = Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory");
                Constructor<?> constructor = clazz.getConstructor();
                Object factoryObj = constructor.newInstance();
                factory = (ScriptEngineFactory) factoryObj;

                try {
                    for (ScriptEngineInitializer initializer : scriptEngineInitializers) {
                        initializer.initialize(globalScope);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (pluginManagement.isEnabled())
                        pluginManagement.disablePlugin();
                }
            } catch (Exception e) {
                exceptionHandle.handleException(null, e);
                return null;
            } finally {
                initialized = true;
            }
        }

        return Optional.ofNullable(factory)
                .map(ScriptEngineFactory::getScriptEngine)
                .map(engine -> {
                    engine.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
                    return engine;
                })
                .orElse(null);
    }

    @Override
    public String getEngineName() {
        return "JShader";
    }

    @Override
    public int getPriority() {
        return 100;
    }
}

