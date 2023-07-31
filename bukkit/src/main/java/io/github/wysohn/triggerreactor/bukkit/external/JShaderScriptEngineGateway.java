package io.github.wysohn.triggerreactor.bukkit.external;

import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.lang.reflect.Constructor;
import java.util.Optional;

@Singleton
public class JShaderScriptEngineGateway implements IScriptEngineGateway {

    @Inject
    private Server server;
    @Inject
    private IExceptionHandle exceptionHandle;

    private boolean initialized = false;
    private ScriptEngineFactory factory;

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
            } catch (Exception e) {
                exceptionHandle.handleException(null, e);
                return null;
            } finally {
                initialized = true;
            }
        }

        return Optional.ofNullable(factory)
                .map(ScriptEngineFactory::getScriptEngine)
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

