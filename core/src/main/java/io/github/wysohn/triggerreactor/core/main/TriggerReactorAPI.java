package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

@PluginScope
public class TriggerReactorAPI implements ITriggerReactorAPI{
    @Inject
    Logger logger;
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;

    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    IThrowableHandler throwableHandler;
    @Inject
    IGameController gameController;
    @Inject
    TaskSupervisor taskSupervisor;
    @Inject
    SelfReference selfReference;

    @Inject
    GlobalVariableManager globalVariableManager;
    @Inject
    PluginConfigManager pluginConfigManager;
    @Inject
    AbstractExecutorManager executorManager;
    @Inject
    AbstractPlaceholderManager placeholderManager;
    @Inject
    AbstractScriptEditManager scriptEditManager;
    @Inject
    AbstractPlayerLocationManager locationManager;
    @Inject
    AbstractPermissionManager permissionManager;
    @Inject
    AbstractAreaSelectionManager selectionManager;
    @Inject
    AbstractInventoryEditManager invEditManager;
    @Inject
    AbstractExternalAPIManager externalAPIManager;

    @Inject
    AbstractLocationBasedTriggerManager<ClickTrigger> clickManager;
    @Inject
    AbstractLocationBasedTriggerManager<WalkTrigger> walkManager;
    @Inject
    AbstractCommandTriggerManager cmdManager;
    @Inject
    AbstractInventoryTriggerManager<?> invManager;
    @Inject
    AbstractAreaTriggerManager areaManager;
    @Inject
    AbstractCustomTriggerManager customManager;
    @Inject
    AbstractRepeatingTriggerManager repeatManager;
    @Inject
    AbstractNamedTriggerManager namedTriggerManager;

    @Inject
    TriggerReactorAPI(){

    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public Object pluginInstance() {
        return pluginInstance;
    }

    @Override
    public IPluginLifecycleController pluginLifecycleController() {
        return pluginLifecycleController;
    }

    @Override
    public IThrowableHandler getThrowableHandler() {
        return throwableHandler;
    }

    @Override
    public IGameController getGameController() {
        return gameController;
    }

    @Override
    public TaskSupervisor getTaskSupervisor() {
        return taskSupervisor;
    }

    @Override
    public SelfReference getSelfReference() {
        return selfReference;
    }

    public final PluginConfigManager getPluginConfigManager() {
        return pluginConfigManager;
    }

    public final GlobalVariableManager getVariableManager() {
        return globalVariableManager;
    }

    public AbstractExecutorManager getExecutorManager() {
        return executorManager;
    }

    public AbstractPlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    public AbstractScriptEditManager getScriptEditManager() {
        return scriptEditManager;
    }

    public AbstractPlayerLocationManager getLocationManager() {
        return locationManager;
    }

    public AbstractPermissionManager getPermissionManager() {
        return permissionManager;
    }

    public AbstractAreaSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public AbstractInventoryEditManager getInvEditManager() {
        return invEditManager;
    }

    @Override
    public AbstractExternalAPIManager getExternalAPIManager() {
        return externalAPIManager;
    }

    public AbstractLocationBasedTriggerManager<ClickTrigger> getClickManager() {
        return clickManager;
    }

    public AbstractLocationBasedTriggerManager<WalkTrigger> getWalkManager() {
        return walkManager;
    }

    public AbstractCommandTriggerManager getCmdManager() {
        return cmdManager;
    }

    public AbstractInventoryTriggerManager<?> invManager() {
        return invManager;
    }

    public AbstractAreaTriggerManager getAreaManager() {
        return areaManager;
    }

    public AbstractCustomTriggerManager getCustomManager() {
        return customManager;
    }

    public AbstractRepeatingTriggerManager getRepeatManager() {
        return repeatManager;
    }

    public AbstractNamedTriggerManager getNamedTriggerManager() {
        return namedTriggerManager;
    }
}
