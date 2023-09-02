package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.main.IPluginManagement;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

@Singleton
public class ScriptEngineManagerProxy extends Manager {
    @Inject
    Logger logger;
    @Inject
    @Named("PluginClassLoader")
    ClassLoader classLoader;
    @Inject
    Set<ScriptEngineInitializer> scriptEngineInitializers;
    @Inject
    IPluginManagement pluginManagement;

    private ScriptEngineManager sem;

    @Override
    public void initialize() {
        ServiceLoader<ScriptEngineFactory> loader = ServiceLoader.load(ScriptEngineFactory.class, classLoader);
        loader.forEach(factory -> {
            logger.info("ScriptEngineFactory found: " + factory.getEngineName());
            logger.info("Loaded from location: " + factory.getClass().getProtectionDomain().getCodeSource().getLocation());
        });

        sem = new ScriptEngineManager(classLoader);

        try {
            Bindings globalBindings = sem.getBindings();
            for (ScriptEngineInitializer initializer : scriptEngineInitializers) {
                initializer.initialize(globalBindings);
            }
            sem.setBindings(globalBindings);
        } catch (Exception e) {
            e.printStackTrace();
            if (pluginManagement.isEnabled())
                pluginManagement.disablePlugin();
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    public ScriptEngineManager get() {
        return sem;
    }
}
