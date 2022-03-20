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
        return TriggerReactorMain.instance.areaManager;
    }

    public static AbstractLocationBasedTriggerManager<ClickTrigger> getClickManager() {
        return TriggerReactorMain.instance.clickManager;
    }

    public static CommandTriggerManager getCmdManager() {
        return TriggerReactorMain.instance.cmdManager;
    }

    public static CustomTriggerManager getCustomManager() {
        return TriggerReactorMain.instance.customManager;
    }

    public static ExecutorManager getExecutorManager() {
        return TriggerReactorMain.instance.executorManager;
    }


    public static ExternalAPIManager getExternalAPIManager() {
        return TriggerReactorMain.instance.externalAPIManager;
    }

    public static IGameController getGameController() {
        return TriggerReactorMain.instance.gameController;
    }

    public static InventoryEditManager getInvEditManager() {
        return TriggerReactorMain.instance.invEditManager;
    }

    public static PlayerLocationManager getLocationManager() {
        return TriggerReactorMain.instance.locationManager;
    }

    public static NamedTriggerManager getNamedTriggerManager() {
        return TriggerReactorMain.instance.namedTriggerManager;
    }

    public static PlaceholderManager getPlaceholderManager() {
        return TriggerReactorMain.instance.placeholderManager;
    }

    public static PluginConfigManager getPluginConfigManager() {
        return TriggerReactorMain.instance.pluginConfigManager;
    }

    public static RepeatingTriggerManager getRepeatManager() {
        return TriggerReactorMain.instance.repeatManager;
    }

    public static ScriptEditManager getScriptEditManager() {
        return TriggerReactorMain.instance.scriptEditManager;
    }


    public static ScriptEngineManager getScriptEngineManager() {
        return TriggerReactorMain.instance.scriptEngineManager;
    }

    public static AreaSelectionManager getSelectionManager() {
        return TriggerReactorMain.instance.selectionManager;
    }

    public static SelfReference getSelfReference() {
        return TriggerReactorMain.instance.selfReference;
    }

    public static TaskSupervisor getTaskSupervisor() {
        return TriggerReactorMain.instance.taskSupervisor;
    }


    public static IThrowableHandler getThrowableHandler() {
        return TriggerReactorMain.instance.throwableHandler;
    }

    public static GlobalVariableManager getGlobalVariableManager() {
        return TriggerReactorMain.instance.globalVariableManager;
    }

    public static AbstractLocationBasedTriggerManager<WalkTrigger> getWalkTriggerManager() {
        return TriggerReactorMain.instance.walkManager;
    }

    public static InventoryTriggerManager getInventoryTriggerManager() {
        return TriggerReactorMain.instance.invManager;
    }


    public static Logger logger() {
        return TriggerReactorMain.instance.logger;
    }


    public static Object pluginInstance() {
        return TriggerReactorMain.instance.pluginInstance;
    }


    public static IPluginLifecycleController pluginLifecycleController() {
        return TriggerReactorMain.instance.pluginLifecycleController;
    }

    public static IInventoryModifier inventoryModifier(){
        return TriggerReactorMain.instance.inventoryModifier;
    }

    public static IGUIOpenHelper guiOpenHelper() {
        return TriggerReactorMain.instance.guiOpenHelper;
    }
}
