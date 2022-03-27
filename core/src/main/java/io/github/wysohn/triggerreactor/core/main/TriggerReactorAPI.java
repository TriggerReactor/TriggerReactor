package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
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
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.script.ScriptEngineManager;
import java.util.logging.Logger;

/**
 * interface to expose the useful classes to be used by the various
 * dynamic instances that cannot inject the dependency.
 * <p>
 * DO NOT directly access these methods inside the TriggerReactor unless
 * it's absolutely necessary. For example, ExternalAPISupport class has to be
 * instantiated runtime therefore we cannot inject the dependency. Pass
 * this to those instances so that they have access to the necessary
 * managers, etc.
 * <p>
 * Third-party plugins may use these methods to
 * access and control TriggerReactor, but no guarantees are given
 * that there will be no changes for the internal methods, yet
 * methods provided here will be preserved.
 */
public final class TriggerReactorAPI {

    public static AreaTriggerManager getAreaManager() {
        return TriggerReactor.instance.areaManager;
    }

    public static AbstractLocationBasedTriggerManager<ClickTrigger> getClickManager() {
        return TriggerReactor.instance.clickManager;
    }

    public static CommandTriggerManager getCmdManager() {
        return TriggerReactor.instance.cmdManager;
    }

    public static CustomTriggerManager getCustomManager() {
        return TriggerReactor.instance.customManager;
    }

    public static ExecutorManager getExecutorManager() {
        return TriggerReactor.instance.executorManager;
    }


    public static ExternalAPIManager getExternalAPIManager() {
        return TriggerReactor.instance.externalAPIManager;
    }

    public static IGameController getGameController() {
        return TriggerReactor.instance.gameController;
    }

    public static InventoryEditManager getInvEditManager() {
        return TriggerReactor.instance.invEditManager;
    }

    public static PlayerLocationManager getLocationManager() {
        return TriggerReactor.instance.locationManager;
    }

    public static NamedTriggerManager getNamedTriggerManager() {
        return TriggerReactor.instance.namedTriggerManager;
    }

    public static PlaceholderManager getPlaceholderManager() {
        return TriggerReactor.instance.placeholderManager;
    }

    public static PluginConfigManager getPluginConfigManager() {
        return TriggerReactor.instance.pluginConfigManager;
    }

    public static RepeatingTriggerManager getRepeatManager() {
        return TriggerReactor.instance.repeatManager;
    }

    public static ScriptEditManager getScriptEditManager() {
        return TriggerReactor.instance.scriptEditManager;
    }


    public static ScriptEngineManager getScriptEngineManager() {
        return TriggerReactor.instance.scriptEngineManager;
    }

    public static AreaSelectionManager getSelectionManager() {
        return TriggerReactor.instance.selectionManager;
    }

    public static SelfReference getSelfReference() {
        return TriggerReactor.instance.selfReference;
    }

    public static TaskSupervisor getTaskSupervisor() {
        return TriggerReactor.instance.taskSupervisor;
    }


    public static IThrowableHandler getThrowableHandler() {
        return TriggerReactor.instance.throwableHandler;
    }

    public static GlobalVariableManager getGlobalVariableManager() {
        return TriggerReactor.instance.globalVariableManager;
    }

    public static AbstractLocationBasedTriggerManager<WalkTrigger> getWalkTriggerManager() {
        return TriggerReactor.instance.walkManager;
    }

    public static InventoryTriggerManager getInventoryTriggerManager() {
        return TriggerReactor.instance.invManager;
    }


    public static Logger logger() {
        return TriggerReactor.instance.logger;
    }


    public static Object pluginInstance() {
        return TriggerReactor.instance.pluginInstance;
    }


    public static IPluginLifecycleController pluginLifecycleController() {
        return TriggerReactor.instance.pluginLifecycleController;
    }

    public static IInventoryModifier inventoryModifier(){
        return TriggerReactor.instance.inventoryModifier;
    }

    public static IGUIOpenHelper guiOpenHelper() {
        return TriggerReactor.instance.guiOpenHelper;
    }
}
