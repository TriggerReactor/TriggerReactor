package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.main.IPluginManagement;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import java.util.Set;

@Singleton
public class ScriptEngineManagerProxy extends Manager {
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
