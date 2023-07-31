/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.tools.Lag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * The main class of TriggerReactor. This is the center of the main logic of TriggerReactor,
 * and it should not depend on any platform specific code.
 *
 * @author wysohn
 */
@Singleton
public class TriggerReactorCore implements IPluginLifecycle {
    @Inject
    @Named("Plugin")
    private Object pluginObject;
    @Inject
    @Named("PluginClassLoader")
    private ClassLoader pluginClassLoader;
    @Inject
    private Set<ScriptEngineInitializer> scriptEngineInitializers;

    @Inject
    @Named("PluginLogger")
    private Logger logger;
    @Inject
    private TaskSupervisor taskSupervisor;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private IGameManagement gameManagement;
    @Inject
    private PluginConfigManager pluginConfigManager;
    @Inject
    private GlobalVariableManager globalVariableManager;
    @Inject
    private ScriptEngineManager scriptEngineManager;
    @Inject
    private Lag lag;

    @Override
    public void initialize() {
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        try {
            for (ScriptEngineInitializer initializer : scriptEngineInitializers) {
                initializer.initialize(scriptEngineManager);
            }
        } catch (ScriptException e) {
            initFailed(e);
            return;
        }

        try {
            Manager.getManagers().forEach(Manager::initialize);
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        // etc
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50L * 100);

                    while (isAlive() && !isInterrupted()) {
                        taskSupervisor.submitSync(() -> {
                            lag.run();
                            return null;
                        }).get();
                        Thread.sleep(50L);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    logger.info("TPS Helper stopped working." + ex);
                }

            }
        }.start();
    }

    @Override
    public void reload() {
        for(Manager manager : Manager.getManagers()){
            logger.info("Reloading " + manager.getClass().getSimpleName());
            manager.reload();
        }
        logger.info("Reload complete!");
    }

    @Override
    public void shutdown() {
        for(Manager manager : Manager.getManagers()){
            logger.info("Shutting down " + manager.getClass().getSimpleName());
            try{
                manager.shutdown();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        logger.info("Shutting down TriggerReactor. Bye!");
    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        logger.severe("Initialization failed!");
        logger.severe(e.getMessage());
        pluginManagement.disablePlugin();
    }

    public void info(String message) {
        logger.info(message);
    }

    public <T> T getPluginObject() {
        return (T) pluginObject;
    }
}
