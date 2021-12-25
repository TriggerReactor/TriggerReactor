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

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import io.github.wysohn.triggerreactor.tools.TimeUtil;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.stream.SenderOutputStream;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The main abstract class of TriggerReactor. Interacting with any platform should extends this class to
 * create important internal components.
 *
 * @author wysohn
 */
@PluginScope
public class TriggerReactorMain implements IPluginProcedure {
    private final Set<Class<? extends Manager>> savings = new HashSet<>();
    protected Map<String, AbstractAPISupport> sharedVars = new HashMap<>();
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
    AbstractExternalAPIManager externalAPIManager;
    @Inject
    IWrapper wrapper;
    @Inject
    ITriggerReactorAPI api;
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
    ScriptEngineManager scriptEngineManager;
    @Inject
    Set<IScriptEngineInitializer> scriptEngineInitializers;
    @Inject
    Map<String, Class<? extends AbstractAPISupport>> sharedVarProtos;
    private boolean debugging = false;
    @Inject
    protected TriggerReactorMain() {

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

    public ITriggerReactorAPI api() {
        return api;
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
     * @param args
     * @param indexFrom inclusive
     * @param indexTo   inclusive
     * @return
     */
    private String mergeArguments(String[] args, int indexFrom, int indexTo) {
        StringBuilder builder = new StringBuilder(args[indexFrom]);
        for (int i = indexFrom + 1; i <= indexTo; i++) {
            builder.append(" " + args[i]);
        }
        return builder.toString();
    }

    public boolean onCommand(ICommandSender sender, String command, String[] args) {
        if (command.equalsIgnoreCase("triggerreactor")) {
            if (!sender.hasPermission(PERMISSION)) return true;

            if (!pluginLifecycleController.isEnabled()) {
                sender.sendMessage("&cTriggerReactor is disabled. Check your latest.log to see why the plugin is not" + " loaded properly. If there was an error while loading, please report it through github issue" + " or our discord channel.");
                return true;
            }

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("debug")) {
                    debugging = !debugging;
                    String color;
                    if (debugging) {
                        color = "a";
                    } else {
                        color = "c";
                    }
                    sender.sendMessage("Debugging is set to &" + color + debugging);

                    logger.info("Debugging state: " + debugging);
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage("Current version: " + pluginLifecycleController.getVersion());
                    return true;
                } else if (args[0].equalsIgnoreCase("click") || args[0].equalsIgnoreCase("c")) {
                    if (args.length == 1) {
                        scriptEditManager.startEdit(sender, "Click Trigger", "", (SaveHandler) script -> {
                            if (clickManager.startLocationSet((IPlayer) sender, script)) {
                                sender.sendMessage("&7Now click the block to set click trigger.");
                            } else {
                                sender.sendMessage("&7Already on progress.");
                            }
                        });
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++)
                            builder.append(args[i]).append(" ");
                        if (clickManager.startLocationSet((IPlayer) sender, builder.toString())) {
                            sender.sendMessage("&7Now click the block to set click trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("w")) {
                    if (args.length == 1) {
                        scriptEditManager.startEdit(sender, "Walk Trigger", "", script -> {
                            if (walkManager.startLocationSet((IPlayer) sender, script)) {
                                sender.sendMessage("&7Now click the block to set walk trigger.");
                            } else {
                                sender.sendMessage("&7Already on progress.");
                            }
                        });
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++)
                            builder.append(args[i]).append(" ");
                        if (walkManager.startLocationSet((IPlayer) sender, builder.toString())) {
                            sender.sendMessage("&7Now click the block to set walk trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args.length > 1 && (args[0].equalsIgnoreCase("command") || args[0].equalsIgnoreCase("cmd"))) {
                    if (args.length == 3 && cmdManager.has(args[1]) && args[2].equals("sync")) {
                        Trigger trigger = cmdManager.get(args[1]);

                        trigger.getInfo().setSync(!trigger.getInfo().isSync());

                        sender.sendMessage("&7Sync mode: " + (trigger.getInfo()
                                .isSync() ? "&a" : "&c") + trigger.getInfo().isSync());
                        saveAsynchronously(cmdManager);
                    } else if (args.length > 2 && cmdManager.has(args[1]) && (args[2].equals("p") || args[2].equals(
                            "permission"))) {
                        CommandTrigger trigger = cmdManager.get(args[1]);

                        //if no permission is given, delete all permission required
                        String[] permissions = null;
                        if (args.length == 3) {
                            trigger.setPermissions(null);
                        } else {
                            permissions = new String[args.length - 3];
                            for (int i = 3; i < args.length; i++) {
                                permissions[i - 3] = args[i];
                            }
                            trigger.setPermissions(permissions);
                        }
                        if (permissions == null) {
                            sender.sendMessage("&7Cleared permissions");
                        } else {
                            sender.sendMessage("&7Set permissions.");
                        }

                        saveAsynchronously(cmdManager);
                    } else if (args.length > 2 && cmdManager.has(args[1]) && (args[2].equals("a") || args[2].equals(
                            "aliases"))) {
                        CommandTrigger trigger = cmdManager.get(args[1]);

                        //if no aliases are given, delete all aliases
                        String[] aliases = null;
                        if (args.length == 3) {
                            trigger.setAliases(null);
                        } else {
                            aliases = new String[args.length - 3];
                            for (int i = 3; i < args.length; i++) {
                                aliases[i - 3] = args[i];
                            }
                            trigger.setAliases(aliases);
                        }
                        if (aliases == null) {
                            sender.sendMessage("&7Cleared aliases");
                        } else {
                            sender.sendMessage("&7Set Aliases");
                        }

                        saveAsynchronously(cmdManager);
                        cmdManager.reregisterCommand(args[1]);
                    } else if (args.length > 2 && cmdManager.has(args[1]) && (args[2].equals("tab") || args[2].equals(
                            "settab"))) {
                        TriggerInfo info = Optional.of(cmdManager)
                                .map(man -> man.get(args[1]))
                                .map(Trigger::getInfo)
                                .orElseThrow(() -> new RuntimeException("Missing TriggerInfo"));

                        //trg cmd <name> settab a b c
                        List<Map<String, Object>> tabs = new ArrayList<>();
                        for (int i = 3; i < args.length; i++) {
                            String[] split = args[i].split(":", 2);
                            String hint = split.length == 2 ? split[0] : null;
                            String values = split.length == 2 ? split[1] : split[0];

                            Map<String, Object> tab = new LinkedHashMap<>();
                            if (hint != null) tab.put(AbstractCommandTriggerManager.HINT, hint);
                            tab.put(AbstractCommandTriggerManager.CANDIDATES, values);
                            tabs.add(tab);
                        }

                        info.getConfig().put(AbstractCommandTriggerManager.TABS, tabs);
                        cmdManager.reload(args[1]);

                        sender.sendMessage("&7Set tab-completer");
                    } else if (cmdManager.has(args[1])) {
                        Trigger trigger = cmdManager.get(args[1]);

                        scriptEditManager.startEdit(sender,
                                                    trigger.getInfo().getTriggerName(),
                                                    trigger.getScript(),
                                                    script -> {
                                                        try {
                                                            trigger.setScript(script);
                                                        } catch (Exception e) {
                                                            throwableHandler.handleException(sender, e);
                                                        }

                                                        sender.sendMessage("&aScript is updated!");

                                                        saveAsynchronously(cmdManager);
                                                    });
                    } else {
                        final Consumer<String> scriptConsumer = script -> {
                            try {
                                if (cmdManager.addCommandTrigger(args[1], script)) {
                                    sender.sendMessage("&aCommand trigger is binded!");
                                } else {
                                    sender.sendMessage("&cCommand is already binded.");
                                }
                            } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                                throwableHandler.handleException(sender, e);
                                return;
                            }

                            saveAsynchronously(cmdManager);
                        };

                        if (args.length == 2) {
                            scriptEditManager.startEdit(sender, "Command Trigger", "", scriptConsumer);
                        } else {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 2; i < args.length; i++)
                                builder.append(args[i] + " ");

                            scriptConsumer.accept(builder.toString());
                        }
                    }
                    return true;
                } else if ((args[0].equalsIgnoreCase("variables") || args[0].equalsIgnoreCase("vars"))) {
                    if (args.length == 3) {
                        if (args[1].equalsIgnoreCase("Item")) {
                            String name = args[2];
                            if (!GlobalVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                            if (IS == null) {
                                sender.sendMessage("&c" + "You are holding nothing on your main hand!");
                                return true;
                            }

                            try {
                                globalVariableManager.put(name, IS.get());
                            } catch (Exception e) {
                                throwableHandler.handleException(sender, e);
                            }

                            sender.sendMessage("&aItem saved!");
                        } else if (args[1].equalsIgnoreCase("Location")) {
                            String name = args[2];
                            if (!GlobalVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            ILocation loc = ((IPlayer) sender).getLocation();
                            try {
                                globalVariableManager.put(name, loc.get());
                            } catch (Exception e) {
                                throwableHandler.handleException(sender, e);
                            }

                            sender.sendMessage("&aLocation saved!");
                        } else {
                            String name = args[1];
                            String value = args[2];

                            if (!GlobalVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            if (INTEGER_PATTERN.matcher(value).matches()) {
                                try {
                                    globalVariableManager.put(name, Integer.parseInt(value));
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }
                            } else if (DECIMAL_PATTERN.matcher(value).matches()) {
                                try {
                                    globalVariableManager.put(name, Double.parseDouble(value));
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }
                            } else if (value.equals("true") || value.equals("false")) {
                                try {
                                    globalVariableManager.put(name, Boolean.parseBoolean(value));
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }
                            } else {
                                try {
                                    globalVariableManager.put(name, value);
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }
                            }

                            sender.sendMessage("&aVariable saved!");
                        }
                        return true;
                    } else if (args.length == 2) {
                        String name = args[1];
                        sender.sendMessage("&7Value of " + name + ": " + globalVariableManager.get(name));

                        return true;
                    }
                } else if (args.length > 1 && args[0].equalsIgnoreCase("run")) {
                    String script = mergeArguments(args, 1, args.length - 1);

                    try {
                        Trigger trigger = cmdManager.createTempCommandTrigger(script);

                        trigger.activate(gameController.createEmptyPlayerEvent(sender), new HashMap<>());

                    } catch (Exception e) {
                        throwableHandler.handleException(sender, e);
                    }

                    return true;
                } else if (args.length > 2 && args[0].equalsIgnoreCase("sudo")) {
                    String playerName = args[1];
                    String script = mergeArguments(args, 2, args.length - 1);

                    IPlayer targetPlayer = gameController.getPlayer(playerName);
                    if (targetPlayer == null) {
                        sender.sendMessage("&cNo such player named &6" + playerName + "&c!");
                        return true;
                    }

                    try {
                        Trigger trigger = cmdManager.createTempCommandTrigger(script);

                        trigger.activate(gameController.createEmptyPlayerEvent(targetPlayer), new HashMap<>());

                    } catch (Exception e) {
                        throwableHandler.handleException(sender, e);
                    }

                    return true;
                } else if (args.length > 1 && args[0].equalsIgnoreCase("call")) {
                    String namedTriggerName = args[1];
                    String script = args.length > 2 ? mergeArguments(args, 2, args.length - 1) : "";

                    try {
                        Trigger trigger = cmdManager.createTempCommandTrigger(script);
                        Trigger targetTrigger = namedTriggerManager.get(namedTriggerName);
                        if (targetTrigger == null) {
                            sender.sendMessage("&cCannot find &6" + namedTriggerName + "&c! &7Remember that the folder" + " hierarchy is represented with ':' sign. (ex. FolderA:FolderB:Trigger)");
                            return true;
                        }

                        Map<String, Object> variables = new HashMap<>(); // shares same variable space
                        trigger.activate(gameController.createEmptyPlayerEvent(sender), variables);
                        targetTrigger.activate(gameController.createEmptyPlayerEvent(sender), variables);

                    } catch (Exception e) {
                        throwableHandler.handleException(sender, e);
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("i")) {
                    if (args.length > 3 && args[2].equalsIgnoreCase("create")) {
                        String name = args[1];
                        int size = -1;
                        try {
                            size = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("&c" + "" + size + " is not a valid number");
                            return true;
                        }
                        if (size > 54 || size < 9) {
                            sender.sendMessage("&csize must be between 9 and 54");
                            return true;
                        }
                        if (size % 9 != 0) {
                            sender.sendMessage("&csize must be a multiple of 9");
                            return true;
                        }

                        if (args.length == 4) {
                            final int sizeCopy = size;
                            scriptEditManager.startEdit(sender, "Inventory Trigger", "", new SaveHandler() {
                                @Override
                                public void accept(String script) {
                                    try {
                                        if (invManager.createTrigger(sizeCopy, name, script)) {
                                            sender.sendMessage("&aInventory Trigger created!");

                                            saveAsynchronously(invManager);
                                        } else {
                                            sender.sendMessage(
                                                    "&7Another Inventory Trigger with that name already exists");
                                        }
                                    } catch (Exception e) {
                                        throwableHandler.handleException(sender, e);
                                    }
                                }
                            });
                        } else {
                            String script = mergeArguments(args, 4, args.length - 1);

                            try {
                                if (invManager.createTrigger(size, name, script)) {
                                    sender.sendMessage("&aInventory Trigger created!");

                                    saveAsynchronously(invManager);
                                } else {
                                    sender.sendMessage("&7Another Inventory Trigger with that name already exists");
                                }
                            } catch (Exception e) {
                                throwableHandler.handleException(sender, e);
                            }
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        if (invManager.remove(name) != null) {
                            sender.sendMessage("&aDeleted!");

                            saveAsynchronously(invManager);
                        } else {
                            sender.sendMessage("&7No such inventory trigger found.");
                        }
                    } else if (args.length == 4 && args[2].equals("item")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        IS = IS == null ? null : IS.clone();

                        String name = args[1];

                        int index;
                        try {
                            index = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("&c" + "" + args[3] + " is not a valid number.");
                            return true;
                        }

                        InventoryTrigger trigger = invManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        if (index > trigger.getItems().length || index < 1) {
                            sender.sendMessage("&c" + "" + index + " is out of bounds. (Size: " + (trigger.getItems().length) + ")");
                            return true;
                        }

                        trigger.getItems()[index - 1] = IS;
                        saveAsynchronously(invManager);

                        sender.sendMessage("Successfully set item " + index);

                    } else if (args.length > 2 && args[2].equalsIgnoreCase("open")) {
                        String name = args[1];
                        IPlayer forWhom = null;
                        if (args.length == 3) {
                            forWhom = (IPlayer) sender;
                        } else {
                            IPlayer p = gameController.getPlayer(args[3]);
                            if (p != null) forWhom = p;
                        }

                        if (forWhom == null) {
                            sender.sendMessage("&7Can't find that player.");
                            return true;
                        }

                        IInventory opened = invManager.openGUI(forWhom, name);
                        if (opened == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("edit")) {
                        String name = args[1];

                        InventoryTrigger trigger = invManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        scriptEditManager.startEdit(sender,
                                                    trigger.getInfo().getTriggerName(),
                                                    trigger.getScript(),
                                                    new SaveHandler() {
                                                        @Override
                                                        public void accept(String script) {
                                                            try {
                                                                trigger.setScript(script);
                                                            } catch (Exception e) {
                                                                throwableHandler.handleException(sender, e);
                                                            }

                                                            sender.sendMessage("&aScript is updated!");

                                                            saveAsynchronously(invManager);
                                                        }
                                                    });
                    } else if (args.length == 4 && args[2].equals("row")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        IS = IS == null ? null : IS.clone();

                        String name = args[1];

                        int index;
                        try {
                            index = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("&c" + "" + args[3] + " is not a valid number.");
                            return true;
                        }

                        InventoryTrigger trigger = invManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        int rows = trigger.getItems().length / 9;
                        if (index > rows || index < 1) {
                            sender.sendMessage("&c" + "" + index + " is out of bounds. (Maximum: " + rows + ")");
                            return true;
                        }

                        for (int i = 0; i < 9; i++) {
                            trigger.getItems()[(index - 1) * 9 + i] = IS;
                        }

                        saveAsynchronously(invManager);
                        sender.sendMessage("Successfully filled row " + index);

                    } else if (args.length == 4 && args[2].equals("column")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        IS = IS == null ? null : IS.clone();

                        String name = args[1];

                        int index;
                        try {
                            index = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("&c" + "" + args[3] + " is not a valid number.");
                            return true;
                        }

                        InventoryTrigger trigger = invManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        int rows = trigger.getItems().length / 9;
                        if (index > 9 || index < 1) {
                            sender.sendMessage("&c" + "" + index + " is out of bounds. (Maximum: 9)");
                            return true;
                        }

                        for (int i = 0; i < rows; i++) {
                            trigger.getItems()[index - 1 + i * 9] = IS;
                        }

                        saveAsynchronously(invManager);
                        sender.sendMessage("Successfully filled column " + index);

                    } else if (args.length == 3 && args[2].equalsIgnoreCase("edititems")) {
                        String name = args[1];

                        InventoryTrigger trigger = invManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        invEditManager.startEdit((IPlayer) sender, trigger);
                        return true;
                    } else if (args.length > 3 && args[2].equalsIgnoreCase("settitle")) {
                        String name = args[1];
                        String title = mergeArguments(args, 3, args.length - 1);

                        InventoryTrigger trigger = invManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        TriggerInfo info = trigger.getInfo();
                        info.getConfig().put(AbstractInventoryTriggerManager.TITLE, title);

                        invManager.reload(name);

                        sender.sendMessage("Successfully changed title");

                        return true;
                    } else {
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> create <size> [...]",
                                        "create a new inventory. <size> must be multiple of 9." + " The <size> cannot be larger than 54");
                        sendDetails(sender, "/trg i MyInventory create 54");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> delete",
                                        "delete this inventory");
                        sendDetails(sender, "/trg i MyInventory delete");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> item <index>",
                                        "sets item of inventory to the held item. " + "Clears the slot if you are holding nothing.");
                        sendDetails(sender, "/trg i MyInventory item 0");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> column <index>",
                                        "same as the item subcommand, but applied to an entire column." + "Clears the slot if you are holding nothing.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> row <index>",
                                        "same as the item subcommand, but applied to an entire row.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> open",
                                        "Preview the inventory");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> open <player name>",
                                        "Send <player name> a preview of the inventory");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> edit",
                                        "Edit the inventory trigger.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] inventory[i] <inventory name> settitle <title>",
                                        "set title of inventory");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("item")) {
                    if (args.length > 2 && args[1].equalsIgnoreCase("title")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        String title = mergeArguments(args, 2, args.length - 1);
                        gameController.setItemTitle(IS, title);

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length > 3 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("add")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        String lore = mergeArguments(args, 3, args.length - 1);
                        gameController.addItemLore(IS, lore);

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length > 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("set")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        int index = -1;
                        try {
                            index = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("&c" + "" + index + " is not a valid number");
                            return true;
                        }

                        String lore = mergeArguments(args, 4, args.length - 1);

                        if (!gameController.setLore(IS, index - 1, lore)) {
                            sender.sendMessage("&c" + "" + index + " is out of bounds.");
                            return true;
                        }

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("remove")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        int index = -1;
                        try {
                            index = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("&c" + "" + index + " is not a valid number");
                            return true;
                        }

                        if (!gameController.removeLore(IS, index - 1)) {
                            sender.sendMessage("&7No lore at index " + index);
                            return true;
                        }

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else {
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] item title <item title>",
                                        "Change the title of holding item");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] item lore add <line>",
                                        "Append lore to the holding item");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] item lore set <index> <line>",
                                        "Replace lore at the specified index." + "(Index start from 0)");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] item lore remove <index>",
                                        "Delete lore at the specified index.");
                    }

                    return true;
                } else if (args.length > 0 && (args[0].equalsIgnoreCase("area") || args[0].equalsIgnoreCase("a"))) {
                    if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
                        boolean result = selectionManager.toggleSelection(((IPlayer) sender).getUniqueId());

                        sender.sendMessage("&7Area selection mode enabled: &6" + result);
                    } else if (args.length == 3 && args[2].equals("create")) {
                        String name = args[1];

                        // validate the name
                        if (!NAME_PATTERN.matcher(name).matches()) {
                            sender.sendMessage("&cThe name " + name + " has not allowed character!");
                            sender.sendMessage("&7Use only character, number, and underscore(_).");
                            return true;
                        }

                        AreaTrigger trigger = areaManager.get(name);
                        if (trigger != null) {
                            sender.sendMessage("&c" + "Area Trigger " + name + " is already exists!");
                            return true;
                        }

                        Area selected = selectionManager.getSelection(((IPlayer) sender).getUniqueId());
                        if (selected == null) {
                            sender.sendMessage("&7Invalid or incomplete area selection.");
                            return true;
                        }

                        Set<Area> conflicts = areaManager.getConflictingAreas(selected, selected::equals);
                        if (!conflicts.isEmpty()) {
                            sender.sendMessage("&7Found [" + conflicts.size() + "] conflicting areas:");
                            for (Area conflict : conflicts) {
                                sender.sendMessage("&d  " + conflict);
                            }
                            return true;
                        }

                        if (areaManager.createArea(name, selected.getSmallest(), selected.getLargest())) {
                            sender.sendMessage("&aCreated area trigger: " + name);

                            saveAsynchronously(areaManager);

                            selectionManager.resetSelections(((IPlayer) sender).getUniqueId());
                        } else {
                            sender.sendMessage("&7Area Trigger " + name + " already exists.");
                        }
                    } else if (args.length == 3 && args[2].equals("delete")) {
                        String name = args[1];

                        if (areaManager.remove(name) != null) {
                            sender.sendMessage("&aArea Trigger deleted");

                            saveAsynchronously(areaManager);

                            selectionManager.resetSelections(((IPlayer) sender).getUniqueId());
                        } else {
                            sender.sendMessage("&7Area Trigger " + name + " does not exist.");
                        }
                    } else if (args.length > 2 && args[2].equals("enter")) {
                        String name = args[1];

                        AreaTrigger trigger = areaManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if (trigger.getEnterTrigger() != null) {
                            scriptEditManager.startEdit(sender,
                                                        trigger.getInfo().getTriggerName(),
                                                        trigger.getEnterTrigger().getScript(),
                                                        new SaveHandler() {
                                                            @Override
                                                            public void accept(String script) {
                                                                try {
                                                                    trigger.setEnterTrigger(script);

                                                                    saveAsynchronously(areaManager);

                                                                    sender.sendMessage("&aScript is updated!");
                                                                } catch (Exception e) {
                                                                    throwableHandler.handleException(sender, e);
                                                                }
                                                            }
                                                        });
                        } else {
                            if (args.length == 3) {
                                scriptEditManager.startEdit(sender, "Area Trigger [Enter]", "", new SaveHandler() {
                                    @Override
                                    public void accept(String script) {
                                        try {
                                            trigger.setEnterTrigger(script);

                                            saveAsynchronously(areaManager);
                                        } catch (Exception e) {
                                            throwableHandler.handleException(sender, e);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    trigger.setEnterTrigger(mergeArguments(args, 3, args.length - 1));

                                    saveAsynchronously(areaManager);
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length > 2 && args[2].equals("exit")) {
                        String name = args[1];

                        AreaTrigger trigger = areaManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if (trigger.getExitTrigger() != null) {
                            scriptEditManager.startEdit(sender,
                                                        trigger.getInfo().getTriggerName(),
                                                        trigger.getExitTrigger().getScript(),
                                                        script -> {
                                                            try {
                                                                trigger.setExitTrigger(script);

                                                                saveAsynchronously(areaManager);

                                                                sender.sendMessage("&aScript is updated!");
                                                            } catch (Exception e) {
                                                                throwableHandler.handleException(sender, e);
                                                            }
                                                        });
                        } else {
                            if (args.length == 3) {
                                scriptEditManager.startEdit(sender, "Area Trigger [Exit]", "", new SaveHandler() {
                                    @Override
                                    public void accept(String script) {
                                        try {
                                            trigger.setExitTrigger(script);

                                            saveAsynchronously(areaManager);
                                        } catch (Exception e) {
                                            throwableHandler.handleException(sender, e);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    trigger.setExitTrigger(mergeArguments(args, 3, args.length - 1));

                                    saveAsynchronously(areaManager);
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length == 3 && args[2].equals("sync")) {
                        String name = args[1];

                        AreaTrigger trigger = areaManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        trigger.getInfo().setSync(!trigger.getInfo().isSync());

                        saveAsynchronously(areaManager);

                        sender.sendMessage("&7Sync mode: " + (trigger.getInfo()
                                .isSync() ? "&a" : "&c") + trigger.getInfo().isSync());
                    } else {
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] area[a] toggle",
                                        "Enable/Disable area selection mode.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] area[a] <name> create",
                                        "Create area trigger out of selected region.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] area[a] <name> delete",
                                        "Delete area trigger. BE CAREFUL!");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] area[a] <name> enter [...]",
                                        "Enable/Disable area selection mode.");
                        sendDetails(sender, "/trg a TestingArea enter #MESSAGE \"Welcome\"");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] area[a] <name> exit [...]",
                                        "Enable/Disable area selection mode.");
                        sendDetails(sender, "/trg a TestingArea exit #MESSAGE \"Bye\"");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] area[a] <name> sync",
                                        "Enable/Disable sync mode.");
                        sendDetails(sender,
                                    "Setting it to true when you want to cancel event (with #CANCELEVENT)." + " However, setting sync mode will make the trigger run on server thread; keep in mind that" + " it can lag the server if you have too much things going on within the code." + " Set it to false always if you are not sure.");
                    }
                    return true;
                } else if (args.length > 2 && args[0].equalsIgnoreCase("custom")) {
                    String eventName = args[1];
                    String name = args[2];

                    CustomTrigger trigger = customManager.get(name);
                    if (trigger != null) {
                        scriptEditManager.startEdit(sender,
                                                    trigger.getInfo().getTriggerName(),
                                                    trigger.getScript(),
                                                    script -> {
                                                        try {
                                                            trigger.setScript(script);
                                                        } catch (Exception e) {
                                                            throwableHandler.handleException(sender, e);
                                                        }

                                                        sender.sendMessage("&aScript is updated!");

                                                        saveAsynchronously(customManager);
                                                    });
                    } else {
                        if (args.length == 3) {
                            scriptEditManager.startEdit(sender,
                                                        "Custom Trigger[" + eventName.substring(Math.max(0,
                                                                                                         eventName.length() - 10)) + "]",
                                                        "",
                                                        script -> {
                                                            try {
                                                                customManager.createCustomTrigger(eventName,
                                                                                                  name,
                                                                                                  script);

                                                                saveAsynchronously(customManager);

                                                                sender.sendMessage("&aCustom Trigger created!");
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                                sender.sendMessage("&c" + "Could not save! " + e.getMessage());
                                                                sender.sendMessage("&c" + "See console for detailed messages.");
                                                            }
                                                        });
                        } else {
                            String script = mergeArguments(args, 3, args.length - 1);

                            try {
                                customManager.createCustomTrigger(eventName, name, script);

                                saveAsynchronously(customManager);

                                sender.sendMessage("&aCustom Trigger created!");
                            } catch (ClassNotFoundException e2) {
                                sender.sendMessage("&c" + "Could not save! " + e2.getMessage());
                                sender.sendMessage("&c" + "Provided event name is not valid.");
                            } catch (Exception e) {
                                throwableHandler.handleException(sender, e);
                            }
                        }
                    }
                    return true;
                } else if (args.length > 0 && (args[0].equalsIgnoreCase("repeat") || args[0].equalsIgnoreCase("r"))) {
                    if (args.length == 2) {
                        String name = args[1];

                        Trigger trigger = repeatManager.get(name);
                        if (trigger != null) {
                            scriptEditManager.startEdit(sender,
                                                        trigger.getInfo().getTriggerName(),
                                                        trigger.getScript(),
                                                        new SaveHandler() {
                                                            @Override
                                                            public void accept(String script) {
                                                                try {
                                                                    trigger.setScript(script);
                                                                } catch (Exception e) {
                                                                    throwableHandler.handleException(sender, e);
                                                                }

                                                                sender.sendMessage("&aScript is updated!");

                                                                saveAsynchronously(repeatManager);
                                                            }
                                                        });
                        } else {
                            this.scriptEditManager.startEdit(sender, "Repeating Trigger", "", new SaveHandler() {
                                @Override
                                public void accept(String script) {
                                    try {
                                        repeatManager.createTrigger(name, script);
                                    } catch (Exception e) {
                                        throwableHandler.handleException(sender, e);
                                    }

                                    saveAsynchronously(repeatManager);
                                }
                            });
                        }
                    } else if (args.length == 4 && args[2].equalsIgnoreCase("interval")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        String intervalValue = args[3];
                        long interval = TimeUtil.parseTime(intervalValue);

                        trigger.setInterval(interval);

                        saveAsynchronously(repeatManager);

                        sender.sendMessage("&aNow " + "&6[" + name + "]" + "&a will run every " + "&6[" + TimeUtil.milliSecondsToString(
                                interval) + "]");
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("autostart")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        trigger.setAutoStart(!trigger.isAutoStart());

                        saveAsynchronously(repeatManager);

                        sender.sendMessage("Auto start: " + (trigger.isAutoStart() ? "&a" : "&c") + trigger.isAutoStart());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("toggle")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        if (repeatManager.isRunning(name)) {
                            repeatManager.stopTrigger(name);
                            sender.sendMessage("&aScheduled stop. It may take some time depends on CPU usage.");
                        } else {
                            repeatManager.startTrigger(name);
                            sender.sendMessage("&aScheduled start up. It may take some time depends on CPU usage.");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("pause")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        trigger.setPaused(!trigger.isPaused());

                        sender.sendMessage("Paused: " + (trigger.isPaused() ? "&a" : "&c") + trigger.isPaused());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("status")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        repeatManager.showTriggerInfo(sender, trigger);
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        repeatManager.remove(name);
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name>", "Create Repeating Trigger.");
                        sendDetails(sender, "&4Quick create is not supported.");
                        sendDetails(sender,
                                    "This creates a Repeating Trigger with default settings. You probably will want to change default values" + " using other commands below. Also, creating Repeating Trigger doesn't start it automatically.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] repeat[r] <name> interval <time format>",
                                        "Change the interval of this trigger.");
                        sendDetails(sender,
                                    "Notice the <time format> is not just a number but has specific format for it. For example, you first" + " type what number you want to set and also define the unit of it. If you want it to repeat it every 1 hour, 20 minutes," + " 50seconds, and 10ticks, then it will be &6" + "/trg r BlahBlah interval 1h20m50s10t." + "&7 Currently only h, m," + " s, and t are supported for this format. Also notice that if you have two numbers with same format, they will add up as well. For example," + "&6 /trg r BlahBlah interval 30s40s" + "&7 will be added up to 70seconds total. All units other than" + " h, m, s, or t will be ignored.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] repeat[r] <name> autostart",
                                        "Enable/Disable automatic start for this trigger.");
                        sendDetails(sender,
                                    "By setting this to " + "&atrue" + "&7, this trigger will start on plugin enables itself. " + "Otherwise, you have to start it yourself every time.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] repeat[r] <name> toggle",
                                        "Start or stop the Repeating Trigger.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] repeat[r] <name> pause",
                                        "Pause or unpause the Repeating Trigger.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] repeat[r] <name> status",
                                        "See brief information about this trigger.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] repeat[r] <name> delete",
                                        "Delete repeating trigger.");
                    }

                    return true;
                } else if (args.length == 2 && (args[0].equalsIgnoreCase("synccustom") || args[0].equalsIgnoreCase(
                        "sync"))) {
                    String name = args[1];

                    CustomTrigger trigger = customManager.get(name);
                    if (trigger == null) {
                        sender.sendMessage("&7No Custom Trigger found with that name.");
                        return true;
                    }

                    trigger.getInfo().setSync(!trigger.getInfo().isSync());

                    saveAsynchronously(customManager);

                    sender.sendMessage("&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c") + trigger.getInfo()
                            .isSync());
                    return true;
                } else if (args.length == 3 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del"))) {
                    String key = args[2];
                    switch (args[1]) {
                        case "vars":
                        case "variables":
                            globalVariableManager.remove(key);
                            sender.sendMessage("&aRemoved the variable &6" + key);
                            break;
                        case "cmd":
                        case "command":
                            if (cmdManager.remove(key) != null) {
                                sender.sendMessage("&aRemoved the command trigger &6" + key);

                                saveAsynchronously(cmdManager);
                            } else {
                                sender.sendMessage("&7Command trigger &6" + key + "&7 does not exist");
                            }
                            break;
                        case "custom":
                            if (customManager.remove(key) != null) {
                                sender.sendMessage("&aRemoved the custom trigger &6" + key);

                                saveAsynchronously(customManager);
                            } else {
                                sender.sendMessage("&7Custom Trigger &6" + key + "&7 does not exist");
                            }
                            break;
                        default:
                            sender.sendMessage("Ex) /trg del vars player.count");
                            sender.sendMessage("List: variables[vars], command[cmd], custom");
                            break;
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("search")) {
                    SimpleChunkLocation scloc = ((IPlayer) sender).getChunk();
                    gameController.showGlowStones(sender, clickManager.getTriggersInChunk(scloc));
                    gameController.showGlowStones(sender, walkManager.getTriggersInChunk(scloc));
                    sender.sendMessage("&7Now trigger blocks will be shown as &6" + "glowstone");
                    return true;
                } else if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("- - - - - Result - - - - ");
                    for (Manager manager : managers) {
                        if (!(manager instanceof AbstractTriggerManager<?>)) continue;

                        for (String val : ((AbstractTriggerManager<? extends Trigger>) manager).getTriggerList((name) -> {
                            for (int i = 1; i < args.length; i++) {
                                if (!name.contains(args[i])) return false;
                            }
                            return true;
                        })) {
                            sender.sendMessage("&d" + val);
                        }
                    }
                    sender.sendMessage(" ");
                    return true;
                } else if (args[0].equalsIgnoreCase("timings")) {
                    if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
                        Timings.on = !Timings.on;

                        if (Timings.on) {
                            sender.sendMessage("&aEnabled");
                        } else {
                            sender.sendMessage("&cDisabled");
                        }
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
                        Timings.reset();

                        sender.sendMessage("&aReset Complete.");
                    } else if (args.length > 1 && args[1].equalsIgnoreCase("print")) {
                        OutputStream os;

                        if (args.length > 2) {
                            String fileName = args[2];
                            File folder = new File(dataFolder, "timings");
                            if (!folder.exists()) folder.mkdirs();
                            File file = new File(folder, fileName + ".timings");
                            if (file.exists()) file.delete();
                            try {
                                file.createNewFile();
                                os = new FileOutputStream(file);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                sender.sendMessage("&cCould not create log file. Check console for details.");
                                return true;
                            }
                        } else {
                            os = new SenderOutputStream(sender);
                        }

                        try {
                            Timings.printAll(os);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] timings toggle",
                                        "turn on/off timings analysis. Also analysis will be reset.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] timings reset",
                                        "turn on/off timings analysis. Also analysis will be reset.");
                        sendCommandDesc(sender, "/triggerreactor[trg] timings print", "Show analysis result.");
                        sendCommandDesc(sender,
                                        "/triggerreactor[trg] timings print xx",
                                        "Save analysis to file named xx.timings");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("saveall")) {
                    for (Manager manager : managers)
                        manager.saveAll();
                    sender.sendMessage("Save complete!");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    for (Manager manager : managers)
                        manager.onReload();

                    executorManager.onReload();
                    placeholderManager.onReload();

                    sender.sendMessage("Reload Complete!");
                    return true;
                } else if (args[0].equalsIgnoreCase("help")) {
                    int page = 0;
                    if (args.length > 1) {
                        if (!args[1].matches("[0-9]+")) {
                            sender.sendMessage("&c" + args[1] + " is not a valid page number.");
                            return true;
                        }

                        page = Integer.parseInt(args[1]);
                    }

                    showHelp(sender, page);
                    return true;
                } else if (args[0].equalsIgnoreCase("links")) {
                    if (args.length < 2) {
                        return true;
                    }
                    AbstractInventoryEditManager manager = invEditManager;
                    IPlayer player = (IPlayer) sender;
                    switch (args[1]) {
                        case "inveditsave":
                            manager.saveEdit(player);
                            return true;
                        case "inveditcontinue":
                            manager.continueEdit(player);
                            return true;
                        case "inveditdiscard":
                            manager.discardEdit(player);
                            return true;
                    }
                }
            }

            showHelp(sender);
        }

        return true;
    }

    //only for /trg command
    public List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) return Collections.singletonList("permission denied.");

        switch (args.length) {
            case 1:
                return filter(Arrays.asList("area",
                                            "click",
                                            "cmd",
                                            "command",
                                            "custom",
                                            "del",
                                            "delete",
                                            "help",
                                            "inventory",
                                            "item",
                                            "list",
                                            "reload",
                                            "repeat",
                                            "run",
                                            "call",
                                            "saveall",
                                            "search",
                                            "sudo",
                                            "synccustom",
                                            "timings",
                                            "variables",
                                            "version",
                                            "walk"), args[0]);
            case 2:
                switch (args[0].toLowerCase()) {
                    case "area":
                    case "a":
                        List<String> names = triggerNames(areaManager);
                        // /trg area toggle
                        names.add("toggle");
                        return filter(names, args[1]);
                    case "cmd":
                    case "command":
                        return filter(triggerNames(cmdManager), args[1]);
                    case "custom":
                        //event list
                        return filter(new ArrayList<String>(customManager.getAbbreviations()), args[1]);
                    case "delete":
                    case "del":
                        return filter(Arrays.asList("cmd", "command", "custom", "vars", "variables"), args[1]);
                    case "inventory":
                    case "i":
                        return filter(triggerNames(invManager), args[1]);
                    case "item":
                        return filter(Arrays.asList("lore", "title"), args[1]);
                    case "repeat":
                    case "r":
                        return filter(triggerNames(repeatManager), args[1]);
                    case "sudo":
                        return null; //player selection
                    case "synccustom":
                        return filter(triggerNames(customManager), args[1]);
                    case "timings":
                        return filter(Arrays.asList("print", "toggle", "reset"), args[1]);
                    case "call":
                        return filter(triggerNames(namedTriggerManager), args[1]);
                }
            case 3:
                switch (args[0].toLowerCase()) {
                    case "area":
                    case "a":
                        if (!args[1].equalsIgnoreCase("toggle")) {
                            return filter(Arrays.asList("create", "delete", "enter", "exit", "sync"), args[2]);
                        }
                        return EMPTY;
                    case "command":
                    case "cmd":
                        return filter(Arrays.asList("aliases", "permission", "sync", "settab"), args[2]);
                    case "custom":
                        return filter(triggerNames(customManager), args[2]);
                    case "delete":
                    case "del":
                        AbstractTriggerManager manager;
                        switch (args[1]) {
                            case "cmd":
                            case "command":
                                manager = cmdManager;
                                break;
                            case "custom":
                                manager = customManager;
                                break;
                            //"vars" and "variables" also possible, but I won't be offering completions for these
                            default:
                                return EMPTY;
                        }
                        return filter(triggerNames(manager), args[2]);
                    case "inventory":
                    case "i":
                        return filter(Arrays.asList("column",
                                                    "create",
                                                    "delete",
                                                    "edit",
                                                    "edititems",
                                                    "item",
                                                    "open",
                                                    "row",
                                                    "settitle"), args[2]);
                    case "item":
                        if (args[1].equals("lore")) {
                            return filter(Arrays.asList("add", "set", "remove"), args[2]);
                        }
                    case "repeat":
                    case "r":
                        return filter(Arrays.asList("autostart", "delete", "interval", "pause", "status", "toggle"),
                                      args[2]);
                }
            case 4:
                switch (args[0].toLowerCase()) {
                    case "inventory":
                    case "i":
                        if (args[2].equalsIgnoreCase("open")) {
                            return null; //player selection
                        }
                        if (args[2].equalsIgnoreCase("create")) {
                            return filter(Arrays.asList("9", "18", "27", "36", "45", "54"), args[3]);
                        }
                }
        }
        return EMPTY;
    }

    /**
     * Call saveAll() on separated thread. It should also check if a saving task is already
     * happening with the 'manager.' (As it will cause concurrency issue without the proper check up)
     *
     * @param manager
     */
    public boolean saveAsynchronously(final Manager manager) {
        if (savings.contains(manager.getClass())) return false;

        new Thread(() -> {
            try {
                synchronized (savings) {
                    savings.add(manager.getClass());
                }

                logger.info("Saving " + manager.getClass().getSimpleName());
                manager.saveAll();
                logger.info("Saving Done!");
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Failed to save " + manager.getClass().getSimpleName());
            } finally {
                synchronized (savings) {
                    savings.remove(manager.getClass());
                }
            }
        }) {{
            this.setPriority(MIN_PRIORITY);
        }}.start();
        return true;
    }

    /**
     * Send command description.
     *
     * @param sender  sender to show description
     * @param command the command to explain
     * @param desc    description
     */
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        sender.sendMessage("&b" + command + " &8- &7" + desc);
    }

    /**
     * Send detail under the command. It is usually called after {@link #sendCommandDesc(ICommandSender, String, String)}
     * to add more information or example about the command.
     *
     * @param sender sender to show description
     * @param detail detail to show
     */
    protected void sendDetails(ICommandSender sender, String detail) {
        sender.sendMessage("  &7" + detail);
    }

    private void showHelp(ICommandSender sender) {
        showHelp(sender, 1);
    }

    private void showHelp(ICommandSender sender, int page) {
        page = Math.max(1, Math.min(HELP_PAGES.size(), page));

        sender.sendMessage("&7-----     &6" + pluginLifecycleController.getPluginDescription() + "&7    ----");
        HELP_PAGES.get(page - 1).sendParagraph(sender);
        sender.sendMessage("");
        sender.sendMessage("&d" + page + "&8/&4" + (HELP_PAGES.size()) + " &8- &6/trg help <page> &7to see other pages.");
    }

    //get all trigger names for a manager
    private List<String> triggerNames(AbstractTriggerManager<? extends Trigger> manager) {
        List<String> names = new ArrayList<String>();
        for (Trigger trigger : manager.getAllTriggers()) {
            names.add(trigger.getInfo().getTriggerName());
        }
        return names;
    }
    @SuppressWarnings("serial")
    private static final List<Paragraph> HELP_PAGES = new ArrayList<Paragraph>() {{
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] walk[w] [...] &8- &7create a walk trigger.");
            sender.sendMessage("  &7/trg w #MESSAGE \"HEY YOU WALKED!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg w &7without extra parameters.");

            sender.sendMessage("&b/triggerreactor[trg] click[c] [...] &8- &7create a click trigger.");
            sender.sendMessage("  &7/trg c #MESSAGE \"HEY YOU CLICKED!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg c &7without extra parameters.");
        });
        add((sender) -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] command[cmd] <command name> [...] &8- &7create a command trigger.");
            sender.sendMessage("  &7/trg cmd test #MESSAGE \"I'M test COMMAND!\"");
            sender.sendMessage(
                    "  &7To create lines of script, simply type &b/trg cmd <command name> &7without extra parameters.");
            sender.sendMessage("  &7To change sync/async mode, type &b/trg cmd <command name> sync&7.");
            sender.sendMessage(
                    "  &7- To set permissions for this command, type &b/trg cmd <command name> permission[p] x.y x.z y.y ...&7.");
            sender.sendMessage(
                    "  &7- To set aliases for this command, type &b/trg cmd <command name> aliases[a] some thing ...&7.");
            sender.sendMessage("    &6*&7Not providing any permission or aliases will remove them instead.");
            sender.sendMessage("  &7- To add tab-completer, type &b/trg cmd <command name> settab[tab] " + "<a/b/c>:a,b,c <player>:$playerlist this,it,that");
            sender.sendMessage("    &6*&7The parameter has following format: hint:val1,val2,...");
            sender.sendMessage("    &6*&7Not providing any tab-completer will remove it instead.");
            sender.sendMessage(
                    "    &7Hint shows up as simple string when a user is about to type something, and values " + "will start to show up as a form of tab-completers as soon as their first characters matching with " + "the characters typed by the user.");
            sender.sendMessage(
                    "    &7You may omit the hint, yet you cannot omit the values. To use only hint but no values, " + "edit the config file manually.");
        });
        add((sender) -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] inventory[i] <inventory name> &8- &7Create an inventory trigger named <inventory name>");
            sender.sendMessage("  &7/trg i to see more commands...");

            sender.sendMessage("&b/triggerreactor[trg] item &8- &7Item modification. Type it to see the list.");

            sender.sendMessage("&b/triggerreactor[trg] area[a] &8- &7Create an area trigger.");
            sender.sendMessage("  &7/trg a to see more commands...");

            sender.sendMessage("&b/triggerreactor[trg] repeat[r] &8- &7Create an repeating trigger.");
            sender.sendMessage("&b/triggerreactor[trg] version &8- &7Show the plugin version.");
            sender.sendMessage("  &7/trg r to see more commands...");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] custom <event> <name> [...] &8- &7Create a custom trigger.");
            sender.sendMessage("  &7/trg custom onJoin Greet #BROADCAST \"Please welcome \"+player.getName()+\"!\"");
            sender.sendMessage(
                    "&b/triggerreactor[trg] synccustom[sync] <name> &8- &7Toggle Sync/Async mode of custom trigger <name>");
            sender.sendMessage("  &7/trg synccustom Greet");

            sender.sendMessage("&b/triggerreactor[trg] variables[vars] [...] &8- &7set global variables.");
            sender.sendMessage(
                    "  &7&cWarning - This command will delete the previous data associated with the key if exists.");
            sender.sendMessage("  &7/trg vars Location test &8- &7save current location into global variable 'test'");
            sender.sendMessage("  &7/trg vars Item gifts.item1 &8- &7save hand held item into global variable 'test'");
            sender.sendMessage("  &7/trg vars test 13.5 &8- &7save 13.5 into global variable 'test'");

            sender.sendMessage(
                    "&b/triggerreactor[trg] variables[vars] <variable name> &8- &7get the value saved in <variable name>. null if nothing.");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] run [...] &8- &7Run simple script now without making a trigger.");
            sender.sendMessage("  &7/trg run #TP {\"MahPlace\"}");

            sender.sendMessage(
                    "&b/triggerreactor[trg] sudo <player> [...] &8- &7Run simple script now without making a trigger.");
            sender.sendMessage("  &7/trg sudo wysohn #TP {\"MahPlace\"}");

            sender.sendMessage(
                    "&b/triggerreactor[trg] call <named trigger> [codes ...] &8- &7Run Named Trigger directly.");
            sender.sendMessage("  &7/trg call MyNamedTrigger abc = {\"MahPlace\"}");
            sender.sendMessage("  &7the last argument (codes ...) are just like any script, so you can imagine that a" + " temporary trigger will be made, the codes will run, and then the Named Trigger will be" + " called, just like how you do with #CALL. This can be useful if you have variables in the Named Trigger" + " that has to be initialized.");
        });
        add((sender -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] delete[del] <type> <name> &8- &7Delete specific trigger/variable/etc.");
            sender.sendMessage("  &7/trg del vars test &8- &7delete the variable saved in 'test'");
            sender.sendMessage("  &7/trg del cmd test &8- &7delete the command trigger 'test'");
            sender.sendMessage("  &7/trg del custom Greet &8- &7delete the custom trigger 'Greet'");

            sender.sendMessage(
                    "&b/triggerreactor[trg] search &8- &7Show all trigger blocks in this chunk as glowing stones.");

            sender.sendMessage("&b/triggerreactor[trg] list [filter...] &8- &7List all triggers.");
            sender.sendMessage(
                    "  &7/trg list CommandTrigger some &8- &7Show results that contains 'CommandTrigger' and 'some'.");

            sender.sendMessage("&b/triggerreactor[trg] saveall &8- &7Save all scripts, variables, and settings.");

            sender.sendMessage("&b/triggerreactor[trg] reload &8- &7Reload all scripts, variables, and settings.");
        }));
        add((sender -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] timings toggle &8- &7turn on/off timings analysis. Also analysis will be reset.");
            sender.sendMessage(
                    "&b/triggerreactor[trg] timings reset &8- &7turn on/off timings analysis. Also analysis will be reset.");
            sender.sendMessage("&b/triggerreactor[trg] timings print &8- &7Show analysis result.");
            sender.sendMessage("  &b/triggerreactor[trg] timings print xx &8- &7Save analysis to file named xx.timings");
        }));
    }};
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]+.[0-9]{0,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_]+$");
    private static final List<String> EMPTY = new ArrayList<String>();
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
    public static final String PERMISSION = "triggerreactor.admin";

    //returns all strings in completions that start with prefix.
    private static List<String> filter(Collection<String> completions, String prefix) {
        prefix = prefix.trim().toUpperCase();
        List<String> filtered = new ArrayList<String>();
        for (String s : completions) {
            if (s.toUpperCase().startsWith(prefix)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    private interface Paragraph {
        void sendParagraph(ICommandSender sender);
    }
}