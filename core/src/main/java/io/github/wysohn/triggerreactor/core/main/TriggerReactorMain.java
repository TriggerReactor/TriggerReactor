/***************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.areaselection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * The main abstract class of TriggerReactor. Interacting with any platform should extends this class to
 * create important internal components.
 *
 * @author wysohn
 */
public class TriggerReactorMain implements IPluginProcedure {
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;
    @Inject
    Logger logger;
    @Inject
    @Named("DataFolder")
    File dataFolder;
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    IGameController gameController;
    @Inject
    TaskSupervisor task;
    @Inject
    IThrowableHandler throwableHandler;
    @Inject
    Set<Manager> managers;
    @Inject
    GlobalVariableManager globalVariableManager;
    @Inject
    PluginConfigManager pluginConfigManager;
    @Inject
    ExternalAPIManager externalAPIManager;
    @Inject
    IWrapper wrapper;
    @Inject
    ExecutorManager executorManager;
    @Inject
    PlaceholderManager placeholderManager;
    @Inject
    ScriptEditManager scriptEditManager;
    @Inject
    PlayerLocationManager locationManager;
    @Inject
    AreaSelectionManager selectionManager;
    @Inject
    InventoryEditManager invEditManager;
    @Inject
    AbstractLocationBasedTriggerManager<ClickTrigger> clickManager;
    @Inject
    AbstractLocationBasedTriggerManager<WalkTrigger> walkManager;
    @Inject
    CommandTriggerManager cmdManager;
    @Inject
    InventoryTriggerManager invManager;
    @Inject
    AreaTriggerManager areaManager;
    @Inject
    CustomTriggerManager customManager;
    @Inject
    RepeatingTriggerManager repeatManager;
    @Inject
    NamedTriggerManager namedTriggerManager;
    @Inject
    ScriptEngineManager scriptEngineManager;
    @Inject
    Set<IScriptEngineInitializer> scriptEngineInitializers;
    @Inject
    Map<String, Class<? extends AbstractAPISupport>> sharedVarProtos;
    @Inject
    TaskSupervisor taskSupervisor;
    @Inject
    SelfReference selfReference;
    @Inject
    IGUIOpenHelper guiOpenHelper;

    protected Map<String, AbstractAPISupport> sharedVars = new HashMap<>();
    private boolean debugging = false;

    @Inject
    protected TriggerReactorMain() {
        instance = this;
    }

    @Override
    public void onDisable() {
        managers.forEach(Manager::onDisable);

        logger.info("Finalizing the scheduled script executions...");
        CACHED_THREAD_POOL.shutdown();
        logger.info("Shut down complete!");
    }

    @Override
    public void onEnable() throws Exception {
        Thread.currentThread().setContextClassLoader(pluginInstance.getClass().getClassLoader());

        // theoretically, it is perfectly fine to be 0, but we assume that we have at least 1 API support
        ValidationUtil.assertTrue(sharedVars.size(), v -> v > 0);

        for (IScriptEngineInitializer init : scriptEngineInitializers) {
            init.initScriptEngine(scriptEngineManager);
        }

        for (Manager manager : managers) {
            manager.onEnable();
        }
    }

    @Override
    public void onReload() throws RuntimeException {
        managers.forEach(IPluginProcedure::onReload);
    }

    public Map<String, AbstractAPISupport> getSharedVars() {
        return sharedVars;
    }

    public IWrapper getWrapper() {
        return wrapper;
    }

    public boolean isDebugging() {
        return debugging;
    }

    /**
     * Cached Pool for thread execution.
     */
    protected static final ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread() {{
                this.setPriority(MIN_PRIORITY);
            }};
        }
    });
    // do not expose this. only to be access by TriggerReactorAPI
    static TriggerReactorMain instance;
}
