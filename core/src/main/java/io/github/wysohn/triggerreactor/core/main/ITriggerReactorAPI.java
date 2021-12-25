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
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import java.util.logging.Logger;

/**
 * interface to expose the useful classes to be used by the various
 * dynamic instances that cannot inject the dependency.
 * <p>
 * DO NOT directly access the methods from the TriggerReactor unless
 * it's absolutely necessary. For example, Trigger class has to be
 * instantiated runtime therefore we cannot inject the dependency. Pass
 * this to those instances so that they have access to the necessary
 * managers, etc.
 * <p>
 * Third-party plugins may use these methods to
 * access and control TriggerReactor.
 */
public interface ITriggerReactorAPI {
    AbstractAreaTriggerManager getAreaManager();

    /// Triggers
    AbstractLocationBasedTriggerManager<ClickTrigger> getClickManager();

    AbstractCommandTriggerManager getCmdManager();

    AbstractCustomTriggerManager getCustomManager();

    /// Managers
    AbstractExecutorManager getExecutorManager();

    AbstractExternalAPIManager getExternalAPIManager();

    IGameController getGameController();

    AbstractInventoryEditManager getInvEditManager();

    AbstractPlayerLocationManager getLocationManager();

    AbstractNamedTriggerManager getNamedTriggerManager();

    AbstractPermissionManager getPermissionManager();

    AbstractPlaceholderManager getPlaceholderManager();

    PluginConfigManager getPluginConfigManager();

    AbstractRepeatingTriggerManager getRepeatManager();

    AbstractScriptEditManager getScriptEditManager();

    AbstractAreaSelectionManager getSelectionManager();

    SelfReference getSelfReference();

    TaskSupervisor getTaskSupervisor();

    IThrowableHandler getThrowableHandler();

    GlobalVariableManager getVariableManager();

    AbstractLocationBasedTriggerManager<WalkTrigger> getWalkManager();

    AbstractInventoryTriggerManager<?> invManager();

    Logger logger();

    Object pluginInstance();

    IPluginLifecycleController pluginLifecycleController();
}
