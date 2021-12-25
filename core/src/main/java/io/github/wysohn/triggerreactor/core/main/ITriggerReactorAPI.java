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
 *
 * DO NOT directly access the methods from the TriggerReactor unless
 * it's absolutely necessary. For example, Trigger class has to be
 * instantiated runtime therefore we cannot inject the dependency. Pass
 * this to those instances so that they have access to the necessary
 * managers, etc.
 *
 * Third-party plugins may use these methods to
 * access and control TriggerReactor.
 */
public interface ITriggerReactorAPI {
    Logger logger();

    Object pluginInstance();

    IPluginLifecycleController pluginLifecycleController();

    IThrowableHandler getThrowableHandler();

    IGameController getGameController();

    TaskSupervisor getTaskSupervisor();

    SelfReference getSelfReference();

    /// Managers
    AbstractExecutorManager getExecutorManager();

    AbstractPlaceholderManager getPlaceholderManager();

    AbstractScriptEditManager getScriptEditManager();

    AbstractPlayerLocationManager getLocationManager();

    AbstractPermissionManager getPermissionManager();

    AbstractAreaSelectionManager getSelectionManager();

    AbstractInventoryEditManager getInvEditManager();

    AbstractExternalAPIManager getExternalAPIManager();

    /// Triggers
    AbstractLocationBasedTriggerManager<ClickTrigger> getClickManager();

    AbstractLocationBasedTriggerManager<WalkTrigger> getWalkManager();

    AbstractCommandTriggerManager getCmdManager();

    AbstractInventoryTriggerManager<?> invManager();

    AbstractAreaTriggerManager getAreaManager();

    AbstractCustomTriggerManager getCustomManager();

    AbstractRepeatingTriggerManager getRepeatManager();

    AbstractNamedTriggerManager getNamedTriggerManager();

    PluginConfigManager getPluginConfigManager();

    GlobalVariableManager getVariableManager();
}
