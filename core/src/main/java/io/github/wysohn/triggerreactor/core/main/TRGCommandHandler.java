/*
 * Copyright (C) 2022. TriggerReactor Team
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

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.js.executor.ExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.js.placeholder.PlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.tools.ArgumentUtil;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.StringUtils;
import io.github.wysohn.triggerreactor.tools.TimeUtil;
import io.github.wysohn.triggerreactor.tools.stream.SenderOutputStream;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class TRGCommandHandler {
    public static final String PERMISSION = "triggerreactor.admin";

    @Inject
    private Logger logger;
    @Inject
    @Named("DataFolder")
    private File dataFolder;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private IExceptionHandle exceptionHandle;
    @Inject
    private IGameManagement gameManagement;
    @Inject
    private IEventManagement eventManagement;

    @Inject
    private ClickTriggerManager clickTriggerManager;
    @Inject
    private WalkTriggerManager walkTriggerManager;
    @Inject
    private CommandTriggerManager commandTriggerManager;
    @Inject
    private AreaTriggerManager areaTriggerManager;
    @Inject
    private NamedTriggerManager namedTriggerManager;
    @Inject
    private CustomTriggerManager customTriggerManager;
    @Inject
    private InventoryTriggerManager inventoryTriggerManager;
    @Inject
    private RepeatingTriggerManager repeatingTriggerManager;

    @Inject
    private ExecutorManager executorManager;
    @Inject
    private PlaceholderManager placeholderManager;

    @Inject
    private PluginConfigManager pluginConfigManager;
    @Inject
    private GlobalVariableManager globalVariableManager;
    @Inject
    private ScriptEditManager scriptEditManager;
    @Inject
    private InventoryEditManager<?> inventoryEditManager;
    @Inject
    private AreaSelectionManager areaSelectionManager;

    @Inject
    private IEventRegistry eventRegistry;
    @Inject
    private IInventoryHandle inventoryHandle;

    @Inject
    private Set<Manager> managers;

    @Inject
    private TRGCommandHandler() {

    }

    /**
     * Send command description.
     *
     * @param sender  sender to show description
     * @param command the command to explain
     * @param desc    description
     * @deprecated no longer used
     */
    @Deprecated
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        sender.sendMessage("&b" + command + " &8- &7" + desc);
    }

    /**
     * Send detail under the command. It is usually called after
     * {@link #sendCommandDesc(ICommandSender, String, String)}
     * to add more information or example about the command.
     *
     * @param sender sender to show description
     * @param detail detail to show
     * @deprecated no longer used
     */
    @Deprecated
    protected void sendDetails(ICommandSender sender, String detail) {
        sender.sendMessage("  &7" + detail);
    }

    private void showHelp(ICommandSender sender) {
        showHelp(sender, 1);
    }

    private void showHelp(ICommandSender sender, int page) {
        page = Math.max(1, Math.min(HELP_PAGES.size(), page));

        sender.sendMessage("&7-----     &6" + pluginManagement.getPluginDescription() + "&7    ----");
        HELP_PAGES.get(page - 1).sendParagraph(sender);
        sender.sendMessage("");
        sender.sendMessage(
                "&d" + page + "&8/&4" + (HELP_PAGES.size()) + " &8- &6/trg help <page> &7to see other pages.");
    }

    public boolean onCommand(ICommandSender sender, String command, String[] args) {
        if (command.equalsIgnoreCase("triggerreactor")) {
            if (!sender.hasPermission(PERMISSION))
                return true;

            if (!pluginManagement.isEnabled()) {
                sender.sendMessage("&cTriggerReactor is disabled. Check your latest.log to see why the plugin is not" +
                        " loaded properly. If there was an error while loading, please report it "
                        + "through github issue"
                        +
                        " or our discord channel.");
                return true;
            }

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("debug")) {
                    pluginManagement.setDebugging(!pluginManagement.isDebugging());
                    String color;
                    if (pluginManagement.isDebugging()) {
                        color = "a";
                    } else {
                        color = "c";
                    }
                    sender.sendMessage("Debugging is set to &" + color + pluginManagement.isDebugging());

                    logger.info("Debugging state: " + pluginManagement.isDebugging());
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage("Current version: " + pluginManagement.getVersion());
                    return true;
                } else if (args[0].equalsIgnoreCase("click") || args[0].equalsIgnoreCase("c")) {
                    if (args.length == 1) {
                        scriptEditManager.startEdit(sender, "Click Trigger", "", new ScriptEditor.SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                if (clickTriggerManager.startLocationSet((IPlayer) sender, script)) {
                                    sender.sendMessage("&7Now click the block to set click trigger.");
                                } else {
                                    sender.sendMessage("&7Already on progress.");
                                }
                            }
                        });
                    } else {
                        if (clickTriggerManager.startLocationSet((IPlayer) sender, ArgumentUtil.mergeArguments(args, 1))) {
                            sender.sendMessage("&7Now click the block to set click trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("w")) {
                    if (args.length == 1) {
                        scriptEditManager.startEdit(sender, "Walk Trigger", "", new ScriptEditor.SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                if (walkTriggerManager.startLocationSet((IPlayer) sender, script)) {
                                    sender.sendMessage("&7Now click the block to set walk trigger.");
                                } else {
                                    sender.sendMessage("&7Already on progress.");
                                }
                            }
                        });
                    } else {
                        if (walkTriggerManager.startLocationSet((IPlayer) sender, ArgumentUtil.mergeArguments(args, 1))) {
                            sender.sendMessage("&7Now click the block to set walk trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args.length > 1 && (args[0].equalsIgnoreCase("command")
                        || args[0].equalsIgnoreCase("cmd"))) {
                    if (args.length == 3 && commandTriggerManager.has(args[1]) && args[2].equals("sync")) {
                        CommandTrigger trigger = commandTriggerManager.get(args[1]);

                        trigger.getInfo().setSync(!trigger.getInfo().isSync());

                        sender.sendMessage(
                                "&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c") + trigger.getInfo()
                                        .isSync());
                    } else if (args.length > 2 && commandTriggerManager.has(args[1])
                            && (args[2].equals("p") || args[2].equals("permission"))) {
                        CommandTrigger trigger = commandTriggerManager.get(args[1]);

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
                    } else if (args.length > 2 && commandTriggerManager.has(args[1])
                            && (args[2].equals("a") || args[2].equals("aliases"))) {
                        CommandTrigger trigger = commandTriggerManager.get(args[1]);

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

                        commandTriggerManager.reregisterCommand(args[1]);
                    } else if (args.length > 2 && commandTriggerManager.has(args[1])
                            && (args[2].equals("tab") || args[2].equals("settab"))) {
                        TriggerInfo info = Optional.of(commandTriggerManager)
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
                            if (hint != null)
                                tab.put(CommandTriggerManager.TAB_HINT, hint);
                            tab.put(CommandTriggerManager.TAB_CANDIDATES, values);
                            tabs.add(tab);
                        }

                        info.put(TriggerConfigKey.KEY_TRIGGER_COMMAND_TABS, tabs);

                        sender.sendMessage("&7Set tab-completer");
                    } else if (commandTriggerManager.has(args[1])) {
                        CommandTrigger trigger = commandTriggerManager.get(args[1]);

                        scriptEditManager.startEdit(sender,
                                trigger.getInfo().getTriggerName(),
                                trigger.getScript(),
                                new ScriptEditor.SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setScript(script);
                                        } catch (Exception e) {
                                            exceptionHandle.handleException(sender, e);
                                        }

                                        sender.sendMessage("&aScript is updated!");
                                    }
                                });
                    } else {
                        if (StringUtils.hasUpperCase(args[1])) {
                            sender.sendMessage("&cWARNING: It is reported that commands with uppercase makes it not "
                                    + "recognized by some higher version of Minecraft.");
                        }

                        if (args.length == 2) {
                            scriptEditManager.startEdit(sender, "Command Trigger", "", new ScriptEditor.SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    if (!commandTriggerManager.addCommandTrigger(sender, args[1], script))
                                        sender.sendMessage("&cCommand could not be binded." +
                                                " Possibly already being used by another plugin or another CommandTrigger.");

                                    sender.sendMessage("&aCommand trigger is binded!");
                                }
                            });
                        } else {
                            if (!commandTriggerManager.addCommandTrigger(sender, args[1], ArgumentUtil.mergeArguments(args, 2)))
                                sender.sendMessage("&cCommand could not be binded." + " Possibly already being used by another plugin or another CommandTrigger.");

                            sender.sendMessage("&aCommand trigger is binded!");
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
                                this.exceptionHandle.handleException(sender, e);
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
                                this.exceptionHandle.handleException(sender, e);
                            }

                            sender.sendMessage("&aLocation saved!");
                        } else {
                            String name = args[1];
                            String value = args[2];

                            if (!GlobalVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            if (StringUtils.INTEGER_PATTERN.matcher(value).matches()) {
                                try {
                                    globalVariableManager.put(name, Integer.parseInt(value));
                                } catch (Exception e) {
                                    this.exceptionHandle.handleException(sender, e);
                                }
                            } else if (StringUtils.DECIMAL_PATTERN.matcher(value).matches()) {
                                try {
                                    globalVariableManager.put(name, Double.parseDouble(value));
                                } catch (Exception e) {
                                    this.exceptionHandle.handleException(sender, e);
                                }
                            } else if (value.equals("true") || value.equals("false")) {
                                try {
                                    globalVariableManager.put(name, Boolean.parseBoolean(value));
                                } catch (Exception e) {
                                    this.exceptionHandle.handleException(sender, e);
                                }
                            } else {
                                try {
                                    globalVariableManager.put(name, value);
                                } catch (Exception e) {
                                    this.exceptionHandle.handleException(sender, e);
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
                    String script = ArgumentUtil.mergeArguments(args, 1, args.length - 1);

                    try {
                        Trigger trigger = commandTriggerManager.createTempCommandTrigger(script);

                        trigger.activate(eventManagement.createEmptyPlayerEvent(sender), new HashMap<>());

                    } catch (Exception e) {
                        exceptionHandle.handleException(sender, e);
                    }

                    return true;
                } else if (args.length > 2 && args[0].equalsIgnoreCase("sudo")) {
                    String playerName = args[1];
                    String script = ArgumentUtil.mergeArguments(args, 2, args.length - 1);

                    IPlayer targetPlayer = gameManagement.getPlayer(playerName);
                    if (targetPlayer == null) {
                        sender.sendMessage("&cNo such player named &6" + playerName + "&c!");
                        return true;
                    }

                    try {
                        Trigger trigger = commandTriggerManager.createTempCommandTrigger(script);

                        trigger.activate(eventManagement.createEmptyPlayerEvent(targetPlayer), new HashMap<>());

                    } catch (Exception e) {
                        exceptionHandle.handleException(sender, e);
                    }

                    return true;
                } else if (args.length > 1 && args[0].equalsIgnoreCase("call")) {
                    String namedTriggerName = args[1];
                    String script = args.length > 2 ? ArgumentUtil.mergeArguments(args, 2, args.length - 1) : "";

                    try {
                        Trigger trigger = commandTriggerManager.createTempCommandTrigger(namedTriggerName);
                        Trigger targetTrigger = namedTriggerManager.get(namedTriggerName);
                        if (!script.isEmpty() || targetTrigger == null) {
                            scriptEditManager.startEdit(sender, "Named Trigger", script, new ScriptEditor.SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        if (namedTriggerManager.createTrigger(namedTriggerName, script)) {
                                            sender.sendMessage("&aNamed Trigger saved!");
                                        } else {
                                            sender.sendMessage(
                                                    "&7Failed to save Named Trigger");
                                        }
                                    } catch (Exception e) {
                                        exceptionHandle.handleException(sender, e);
                                    }
                                }
                            });
                            return true;
                        }

                        Map<String, Object> variables = new HashMap<>(); // shares same variable space
                        trigger.activate(eventManagement.createEmptyPlayerEvent(sender), variables, true);
                        Optional.of(trigger)
                                .map(Trigger::getVarCopy)
                                .ifPresent(variables::putAll);
                        targetTrigger.activate(eventManagement.createEmptyPlayerEvent(sender), variables);

                    } catch (Exception e) {
                        exceptionHandle.handleException(sender, e);
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
                            scriptEditManager.startEdit(sender, "Inventory Trigger", "", new ScriptEditor.SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        if (inventoryTriggerManager.createTrigger(sizeCopy, name, script)) {
                                            sender.sendMessage("&aInventory Trigger created!");
                                        } else {
                                            sender.sendMessage(
                                                    "&7Another Inventory Trigger with that name already exists");
                                        }
                                    } catch (Exception e) {
                                        exceptionHandle.handleException(sender, e);
                                    }
                                }
                            });
                        } else {
                            String script = ArgumentUtil.mergeArguments(args, 4, args.length - 1);

                            try {
                                if (inventoryTriggerManager.createTrigger(size, name, script)) {
                                    sender.sendMessage("&aInventory Trigger created!");
                                } else {
                                    sender.sendMessage("&7Another Inventory Trigger with that name already exists");
                                }
                            } catch (Exception e) {
                                exceptionHandle.handleException(sender, e);
                            }
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        if (inventoryTriggerManager.remove(name) != null) {
                            sender.sendMessage("&aDeleted!");
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

                        InventoryTrigger trigger = inventoryTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        if (index > trigger.getItems().length || index < 1) {
                            sender.sendMessage(
                                    "&c" + "" + index + " is out of bounds. (Size: " + (trigger.getItems().length)
                                            + ")");
                            return true;
                        }

                        trigger.getItems()[index - 1] = IS;
                        inventoryTriggerManager.put(name, trigger); // TODO think of better way to do this

                        sender.sendMessage("Successfully set item " + index);

                    } else if (args.length > 2 && args[2].equalsIgnoreCase("open")) {
                        String name = args[1];
                        IPlayer forWhom = null;
                        if (args.length == 3) {
                            forWhom = (IPlayer) sender;
                        } else {
                            IPlayer p = gameManagement.getPlayer(args[3]);
                            if (p != null)
                                forWhom = p;
                        }

                        if (forWhom == null) {
                            sender.sendMessage("&7Can't find that player.");
                            return true;
                        }

                        IInventory opened = inventoryTriggerManager.openGUI(forWhom, name);
                        if (opened == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("edit")) {
                        String name = args[1];

                        InventoryTrigger trigger = inventoryTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        scriptEditManager.startEdit(sender,
                                trigger.getInfo().getTriggerName(),
                                trigger.getScript(),
                                new ScriptEditor.SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setScript(script);
                                        } catch (Exception e) {
                                            exceptionHandle.handleException(sender, e);
                                        }

                                        sender.sendMessage("&aScript is updated!");

                                        //TODO not quite intuitive. Think of a better way
                                        inventoryTriggerManager.put(name, trigger);
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

                        InventoryTrigger trigger = inventoryTriggerManager.get(name);
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

                        //TODO not quite intuitive. Think of a better way
                        inventoryTriggerManager.put(name, trigger);
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

                        InventoryTrigger trigger = inventoryTriggerManager.get(name);
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

                        //TODO not quite intuitive. Think of a better way
                        inventoryTriggerManager.put(name, trigger);
                        sender.sendMessage("Successfully filled column " + index);

                    } else if (args.length == 3 && args[2].equalsIgnoreCase("edititems")) {
                        String name = args[1];

                        if (!inventoryTriggerManager.has(name)) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        inventoryEditManager.startEdit((IPlayer) sender, name);
                        return true;
                    } else if (args.length > 3 && args[2].equalsIgnoreCase("settitle")) {
                        String name = args[1];
                        String title = ArgumentUtil.mergeArguments(args, 3, args.length - 1);

                        InventoryTrigger trigger = inventoryTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        TriggerInfo info = trigger.getInfo();
                        info.put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, title);

                        sender.sendMessage("Successfully changed title");

                        return true;
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("pickup")) {
                        String name = args[1];
                        InventoryTrigger trigger = inventoryTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        TriggerInfo info = trigger.getInfo();
                        info.put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_PICKUP, !trigger.canPickup());

                        sender.sendMessage(
                                "Successfully changed pickup : " + (trigger.canPickup() ? "&a" : "&c") + trigger.canPickup());
                    } else {
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] inventory[i] <inventory name> create <size> [...]",
                                "create a new inventory. <size> must be multiple of 9."
                                        + " The <size> cannot be larger than 54");
                        sendDetails(sender, "/trg i MyInventory create 54");
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] inventory[i] <inventory name> delete",
                                "delete this inventory");
                        sendDetails(sender, "/trg i MyInventory delete");
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] inventory[i] <inventory name> item <index>",
                                "sets item of inventory to the held item. "
                                        + "Clears the slot if you are holding nothing.");
                        sendDetails(sender, "/trg i MyInventory item 0");
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] inventory[i] <inventory name> column <index>",
                                "same as the item subcommand, but applied to an entire column."
                                        + "Clears the slot if you are holding nothing.");
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
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] inventory[i] <inventory name> pickup",
                                "set pickupable of inventory");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("item")) {
                    if (args.length > 2 && args[1].equalsIgnoreCase("title")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        String title = ArgumentUtil.mergeArguments(args, 2, args.length - 1);
                        inventoryHandle.setItemTitle(IS, title);

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length > 3 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("add")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        String lore = ArgumentUtil.mergeArguments(args, 3, args.length - 1);
                        inventoryHandle.addItemLore(IS, lore);

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

                        String lore = ArgumentUtil.mergeArguments(args, 4, args.length - 1);

                        if (!inventoryHandle.setLore(IS, index - 1, lore)) {
                            sender.sendMessage("&c" + "" + index + " is out of bounds.");
                            return true;
                        }

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("lore")
                            && args[2].equalsIgnoreCase("remove")) {
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

                        if (!inventoryHandle.removeLore(IS, index - 1)) {
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
                                "Replace lore at the specified index."
                                        + "(Index start from 0)");
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] item lore remove <index>",
                                "Delete lore at the specified index.");
                    }

                    return true;
                } else if (args.length > 0 && (args[0].equalsIgnoreCase("area") || args[0].equalsIgnoreCase("a"))) {
                    if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
                        boolean result = areaSelectionManager.toggleSelection(((IPlayer) sender).getUniqueId());

                        sender.sendMessage("&7Area selection mode enabled: &6" + result);
                    } else if (args.length == 3 && args[2].equals("create")) {
                        String name = args[1];

                        // validate the name
                        if (!StringUtils.NAME_PATTERN.matcher(name).matches()) {
                            sender.sendMessage("&cThe name " + name + " has not allowed character!");
                            sender.sendMessage("&7Use only character, number, and underscore(_).");
                            return true;
                        }

                        AreaTrigger trigger = areaTriggerManager.get(name);
                        if (trigger != null) {
                            sender.sendMessage("&c" + "Area Trigger " + name + " is already exists!");
                            return true;
                        }

                        Area selected = areaSelectionManager.getSelection(((IPlayer) sender).getUniqueId());
                        if (selected == null) {
                            sender.sendMessage("&7Invalid or incomplete area selection.");
                            return true;
                        }

                        Set<Area> conflicts = areaTriggerManager.getConflictingAreas(selected, selected::equals);
                        if (!conflicts.isEmpty()) {
                            sender.sendMessage("&7Found [" + conflicts.size() + "] conflicting areas:");
                            for (Area conflict : conflicts) {
                                sender.sendMessage("&d  " + conflict);
                            }
                            return true;
                        }

                        if (areaTriggerManager.createArea(name, selected.getSmallest(), selected.getLargest())) {
                            sender.sendMessage("&aCreated area trigger: " + name);

                            areaSelectionManager.resetSelections(((IPlayer) sender).getUniqueId());
                        } else {
                            sender.sendMessage("&7Area Trigger " + name + " already exists.");
                        }
                    } else if (args.length == 3 && args[2].equals("delete")) {
                        String name = args[1];

                        if (areaTriggerManager.remove(name) != null) {
                            sender.sendMessage("&aArea Trigger deleted");

                            areaSelectionManager.resetSelections(((IPlayer) sender).getUniqueId());
                        } else {
                            sender.sendMessage("&7Area Trigger " + name + " does not exist.");
                        }
                    } else if (args.length > 2 && args[2].equals("enter")) {
                        String name = args[1];

                        AreaTrigger trigger = areaTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if (trigger.getEnterTrigger() != null) {
                            scriptEditManager.startEdit(sender,
                                    trigger.getInfo().getTriggerName(),
                                    trigger.getEnterTrigger().getScript(),
                                    new ScriptEditor.SaveHandler() {
                                        @Override
                                        public void onSave(String script) {
                                            try {
                                                trigger.setEnterTrigger(script);

                                                //TODO not quite intuitive. Think of a better way
                                                areaTriggerManager.put(trigger.getInfo().getTriggerName(), trigger);

                                                sender.sendMessage("&aScript is updated!");
                                            } catch (Exception e) {
                                                exceptionHandle.handleException(sender, e);
                                            }
                                        }
                                    });
                        } else {
                            if (args.length == 3) {
                                scriptEditManager.startEdit(sender, "Area Trigger [Enter]", "", new ScriptEditor.SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setEnterTrigger(script);

                                            //TODO not quite intuitive. Think of a better way
                                            areaTriggerManager.put(trigger.getInfo().getTriggerName(), trigger);

                                            sender.sendMessage("&aScript is updated!");
                                        } catch (Exception e) {
                                            exceptionHandle.handleException(sender, e);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    trigger.setEnterTrigger(ArgumentUtil.mergeArguments(args, 3, args.length - 1));

                                    //TODO not quite intuitive. Think of a better way
                                    areaTriggerManager.put(trigger.getInfo().getTriggerName(), trigger);

                                    sender.sendMessage("&aScript is updated!");
                                } catch (Exception e) {
                                    exceptionHandle.handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length > 2 && args[2].equals("exit")) {
                        String name = args[1];

                        AreaTrigger trigger = areaTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if (trigger.getExitTrigger() != null) {
                            scriptEditManager.startEdit(sender,
                                    trigger.getInfo().getTriggerName(),
                                    trigger.getExitTrigger().getScript(),
                                    new ScriptEditor.SaveHandler() {
                                        @Override
                                        public void onSave(String script) {
                                            try {
                                                trigger.setExitTrigger(script);

                                                //TODO not quite intuitive. Think of a better way
                                                areaTriggerManager.put(trigger.getInfo().getTriggerName(), trigger);

                                                sender.sendMessage("&aScript is updated!");
                                            } catch (Exception e) {
                                                exceptionHandle.handleException(sender, e);
                                            }
                                        }
                                    });
                        } else {
                            if (args.length == 3) {
                                scriptEditManager.startEdit(sender, "Area Trigger [Exit]", "", new ScriptEditor.SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setExitTrigger(script);

                                            //TODO not quite intuitive. Think of a better way
                                            areaTriggerManager.put(trigger.getInfo().getTriggerName(), trigger);

                                            sender.sendMessage("&aScript is updated!");
                                        } catch (Exception e) {
                                            exceptionHandle.handleException(sender, e);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    trigger.setExitTrigger(ArgumentUtil.mergeArguments(args, 3, args.length - 1));

                                    //TODO not quite intuitive. Think of a better way
                                    areaTriggerManager.put(trigger.getInfo().getTriggerName(), trigger);

                                    sender.sendMessage("&aScript is updated!");
                                } catch (Exception e) {
                                    exceptionHandle.handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length == 3 && args[2].equals("sync")) {
                        String name = args[1];

                        AreaTrigger trigger = areaTriggerManager.get(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        trigger.getInfo().setSync(!trigger.getInfo().isSync());

                        sender.sendMessage(
                                "&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c") + trigger.getInfo()
                                        .isSync());
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
                        sendDetails(sender, "Setting it to true when you want to cancel event (with #CANCELEVENT)."
                                + " However, setting sync mode will make the trigger run on server thread; keep in "
                                + "mind that"
                                + " it can lag the server if you have too much things going on within the code."
                                + " Set it to false always if you are not sure.");
                    }
                    return true;
                } else if (args.length > 2 && args[0].equalsIgnoreCase("custom")) {
                    String eventName = args[1];
                    String name = args[2];

                    CustomTrigger trigger = customTriggerManager.get(name);
                    if (trigger != null) {
                        scriptEditManager.startEdit(sender,
                                trigger.getInfo().getTriggerName(),
                                trigger.getScript(),
                                new ScriptEditor.SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setScript(script);
                                        } catch (Exception e) {
                                            exceptionHandle.handleException(sender, e);
                                        }

                                        sender.sendMessage("&aScript is updated!");

                                        //TODO not quite intuitive. Think of a better way
                                        customTriggerManager.put(name, trigger);
                                    }
                                });
                    } else {
                        if (args.length == 3) {
                            scriptEditManager.startEdit(sender,
                                    "Custom Trigger[" + eventName.substring(Math.max(0,
                                            eventName.length()
                                                    - 10))
                                            + "]", "",
                                    new ScriptEditor.SaveHandler() {
                                        @Override
                                        public void onSave(String script) {
                                            try {
                                                customTriggerManager.createCustomTrigger(
                                                        eventName,
                                                        name,
                                                        script);

                                                sender.sendMessage("&aCustom Trigger "
                                                        + "created!");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                sender.sendMessage("&c" + "Could not save! "
                                                        + e.getMessage());
                                                sender.sendMessage("&c"
                                                        + "See console "
                                                        + "for detailed "
                                                        + "messages.");
                                            }
                                        }
                                    });
                        } else {
                            String script = ArgumentUtil.mergeArguments(args, 3, args.length - 1);

                            try {
                                customTriggerManager.createCustomTrigger(eventName, name, script);

                                sender.sendMessage("&aCustom Trigger created!");
                            } catch (ClassNotFoundException e2) {
                                sender.sendMessage("&c" + "Could not save! " + e2.getMessage());
                                sender.sendMessage("&c" + "Provided event name is not valid.");
                            } catch (Exception e) {
                                exceptionHandle.handleException(sender, e);
                            }
                        }
                    }
                    return true;
                } else if (args.length > 0 && (args[0].equalsIgnoreCase("repeat") || args[0].equalsIgnoreCase("r"))) {
                    if (args.length == 2) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);
                        if (trigger != null) {
                            scriptEditManager.startEdit(sender,
                                    trigger.getInfo().getTriggerName(),
                                    trigger.getScript(),
                                    new ScriptEditor.SaveHandler() {
                                        @Override
                                        public void onSave(String script) {
                                            try {
                                                trigger.setScript(script);
                                            } catch (Exception e) {
                                                exceptionHandle.handleException(sender, e);
                                            }

                                            sender.sendMessage("&aScript is updated!");

                                            //TODO not quite intuitive. Think of a better way
                                            repeatingTriggerManager.put(name, trigger);
                                        }
                                    });
                        } else {
                            this.scriptEditManager.startEdit(sender, "Repeating Trigger", "", new ScriptEditor.SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        repeatingTriggerManager.createTrigger(name, script);
                                    } catch (Exception e) {
                                        exceptionHandle.handleException(sender, e);
                                    }
                                }
                            });
                        }
                    } else if (args.length == 4 && args[2].equalsIgnoreCase("interval")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        String intervalValue = args[3];
                        long interval = TimeUtil.parseTime(intervalValue);

                        trigger.setInterval(interval);

                        sender.sendMessage("&aNow " +
                                "&6[" + name + "]" +
                                "&a will run every " +
                                "&6[" + TimeUtil.milliSecondsToString(interval) + "]");
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("autostart")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        trigger.setAutoStart(!trigger.isAutoStart());

                        sender.sendMessage(
                                "Auto start: " + (trigger.isAutoStart() ? "&a" : "&c") + trigger.isAutoStart());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("toggle")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        if (repeatingTriggerManager.isRunning(name)) {
                            repeatingTriggerManager.stopTrigger(name);
                            sender.sendMessage("&aScheduled stop. It may take some time depends on CPU usage.");
                        } else {
                            repeatingTriggerManager.startTrigger(name);
                            sender.sendMessage("&aScheduled start up. It may take some time depends on CPU usage.");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("pause")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        trigger.setPaused(!trigger.isPaused());

                        sender.sendMessage("Paused: " + (trigger.isPaused() ? "&a" : "&c") + trigger.isPaused());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("status")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        repeatingTriggerManager.showTriggerInfo(sender, trigger);
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatingTriggerManager.get(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        repeatingTriggerManager.remove(name);
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name>", "Create Repeating Trigger.");
                        sendDetails(sender, "&4Quick create is not supported.");
                        sendDetails(sender,
                                "This creates a Repeating Trigger with default settings. You probably will want "
                                        + "to change default values"
                                        + " using other commands below. Also, creating Repeating Trigger doesn't "
                                        + "start it automatically.");
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] repeat[r] <name> interval <time format>",
                                "Change the interval of this trigger.");
                        sendDetails(sender,
                                "Notice the <time format> is not just a number but has specific format for it. "
                                        + "For example, you first"
                                        + " type what number you want to set and also define the unit of it. If "
                                        + "you want it to repeat it every 1 hour, 20 minutes,"
                                        + " 50seconds, and 10ticks, then it will be &6"
                                        + "/trg r BlahBlah interval 1h20m50s10t." + "&7 Currently only h, m,"
                                        + " s, and t are supported for this format. Also notice that if you have "
                                        + "two numbers with same format, they will add up as well. For example,"
                                        + "&6 /trg r BlahBlah interval 30s40s"
                                        + "&7 will be added up to 70seconds total. All units other than"
                                        + " h, m, s, or t will be ignored.");
                        sendCommandDesc(sender,
                                "/triggerreactor[trg] repeat[r] <name> autostart",
                                "Enable/Disable automatic start for this trigger.");
                        sendDetails(sender,
                                "By setting this to " + "&atrue"
                                        + "&7, this trigger will start on plugin enables itself. "
                                        + "Otherwise, you have to start it yourself every time.");
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

                    CustomTrigger trigger = customTriggerManager.get(name);
                    if (trigger == null) {
                        sender.sendMessage("&7No Custom Trigger found with that name.");
                        return true;
                    }

                    trigger.getInfo().setSync(!trigger.getInfo().isSync());

                    sender.sendMessage(
                            "&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c") + trigger.getInfo().isSync());
                    return true;
                } else if (args.length == 3 && (args[0].equalsIgnoreCase("delete")
                        || args[0].equalsIgnoreCase("del"))) {
                    String key = args[2];
                    switch (args[1]) {
                        case "vars":
                        case "variables":
                            globalVariableManager.remove(key);
                            sender.sendMessage("&aRemoved the variable &6" + key);
                            break;
                        case "cmd":
                        case "command":
                            if (commandTriggerManager.remove(key) != null) {
                                sender.sendMessage("&aRemoved the command trigger &6" + key);
                            } else {
                                sender.sendMessage("&7Command trigger &6" + key + "&7 does not exist");
                            }
                            break;
                        case "custom":
                            if (customTriggerManager.remove(key) != null) {
                                sender.sendMessage("&aRemoved the custom trigger &6" + key);
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
                    gameManagement.showGlowStones(sender, clickTriggerManager.getTriggersInChunk(scloc));
                    gameManagement.showGlowStones(sender, walkTriggerManager.getTriggersInChunk(scloc));
                    sender.sendMessage("&7Now trigger blocks will be shown as &6" + "glowstone");
                    return true;
                } else if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("- - - - - Result - - - - ");
                    for (Manager manager : managers) {
                        if (!(manager instanceof AbstractTriggerManager<?>))
                            continue;

                        for (String val :
                                ((AbstractTriggerManager<? extends Trigger>) manager).getTriggerList((name) -> {
                                    for (int i = 1; i < args.length; i++) {
                                        if (!name.contains(args[i]))
                                            return false;
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
                            if (!folder.exists())
                                folder.mkdirs();
                            File file = new File(folder, fileName + ".timings");
                            if (file.exists())
                                file.delete();
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
                    managers.stream()
                            .filter(StatefulObject.class::isInstance)
                            .map(StatefulObject.class::cast)
                            .collect(Collectors.toList())
                            .forEach(StatefulObject::saveAll);
                    sender.sendMessage("Save complete!");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                        sender.sendMessage("&7Are you sure you want to reload? Any changes you made &ccan be lost&7.");
                        sender.sendMessage("&7It is recommended to do &6/trg saveall &7before reloading.");
                        sender.sendMessage("&7If you are sure, type &6/trg reload confirm");
                        return true;
                    }

                    for (Manager manager : managers)
                        manager.reload();

                    executorManager.reload();
                    placeholderManager.reload();

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
                    InventoryEditManager manager = inventoryEditManager;
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

    //get all trigger names for a manager
    private static List<String> triggerNames(AbstractTriggerManager<? extends Trigger> manager) {
        List<String> names = new ArrayList<String>();
        for (Trigger trigger : manager.getAllTriggers()) {
            names.add(trigger.getInfo().getTriggerName());
        }
        return names;
    }

    //only for /trg command
    public List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION))
            return Collections.singletonList("permission denied.");

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
                        List<String> names = triggerNames(areaTriggerManager);
                        // /trg area toggle
                        names.add("toggle");
                        return filter(names, args[1]);
                    case "cmd":
                    case "command":
                        return filter(triggerNames(commandTriggerManager), args[1]);
                    case "custom":
                        //event list
                        return filter(new ArrayList<>(eventRegistry.getAbbreviations()), args[1]);
                    case "delete":
                    case "del":
                        return filter(Arrays.asList("cmd", "command", "custom", "vars", "variables"), args[1]);
                    case "inventory":
                    case "i":
                        return filter(triggerNames(inventoryTriggerManager), args[1]);
                    case "item":
                        return filter(Arrays.asList("lore", "title"), args[1]);
                    case "repeat":
                    case "r":
                        return filter(triggerNames(repeatingTriggerManager), args[1]);
                    case "sudo":
                        return null; //player selection
                    case "synccustom":
                        return filter(triggerNames(customTriggerManager), args[1]);
                    case "timings":
                        return filter(Arrays.asList("print", "toggle", "reset"), args[1]);
                    case "call":
                        return filter(triggerNames(namedTriggerManager), args[1]);
                    case "reload":
                        return filter(Collections.singletonList("confirm"), args[1]);
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
                        return filter(triggerNames(customTriggerManager), args[2]);
                    case "delete":
                    case "del":
                        AbstractTriggerManager manager;
                        switch (args[1]) {
                            case "cmd":
                            case "command":
                                manager = commandTriggerManager;
                                break;
                            case "custom":
                                manager = customTriggerManager;
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
                                "settitle",
                                "pickup"), args[2]);
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
                    "  &7- To set permissions for this command, type &b/trg cmd <command name> permission[p] x.y x.z "
                            + "y.y ...&7.");
            sender.sendMessage(
                    "  &7- To set aliases for this command, type &b/trg cmd <command name> aliases[a] some thing .."
                            + ".&7.");
            sender.sendMessage("    &6*&7Not providing any permission or aliases will remove them instead.");
            sender.sendMessage("  &7- To add tab-completer, type &b/trg cmd <command name> settab[tab] " +
                    "<a/b/c>:a,b,c <player>:$playerlist this,it,that");
            sender.sendMessage("    &6*&7The parameter has following format: hint:val1,val2,...");
            sender.sendMessage("    &6*&7Not providing any tab-completer will remove it instead.");
            sender.sendMessage(
                    "    &7Hint shows up as simple string when a user is about to type something, and values " +
                            "will start to show up as a form of tab-completers as soon as their first characters "
                            + "matching with "
                            +
                            "the characters typed by the user.");
            sender.sendMessage(
                    "    &7You may omit the hint, yet you cannot omit the values. To use only hint but no values, " +
                            "edit the config file manually.");
        });
        add((sender) -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] inventory[i] <inventory name> &8- &7Create an inventory trigger named "
                            + "<inventory name>");
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
                    "&b/triggerreactor[trg] synccustom[sync] <name> &8- &7Toggle Sync/Async mode of custom trigger "
                            + "<name>");
            sender.sendMessage("  &7/trg synccustom Greet");

            sender.sendMessage("&b/triggerreactor[trg] variables[vars] [...] &8- &7set global variables.");
            sender.sendMessage(
                    "  &7&cWarning - This command will delete the previous data associated with the key if exists.");
            sender.sendMessage("  &7/trg vars Location test &8- &7save current location into global variable 'test'");
            sender.sendMessage("  &7/trg vars Item gifts.item1 &8- &7save hand held item into global variable 'test'");
            sender.sendMessage("  &7/trg vars test 13.5 &8- &7save 13.5 into global variable 'test'");

            sender.sendMessage(
                    "&b/triggerreactor[trg] variables[vars] <variable name> &8- &7get the value saved in <variable "
                            + "name>. null if nothing.");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] run [...] &8- &7Run simple script now without making a trigger"
                    + ".");
            sender.sendMessage("  &7/trg run #TP {\"MahPlace\"}");

            sender.sendMessage(
                    "&b/triggerreactor[trg] sudo <player> [...] &8- &7Run simple script now without making a trigger.");
            sender.sendMessage("  &7/trg sudo wysohn #TP {\"MahPlace\"}");

            sender.sendMessage(
                    "&b/triggerreactor[trg] call <named trigger> [codes ...] &8- &7Run Named Trigger directly.");
            sender.sendMessage("  &7/trg call MyNamedTrigger abc = {\"MahPlace\"}");
            sender.sendMessage("  &7the last argument (codes ...) are just like any script, so you can imagine that a" +
                    " temporary trigger will be made, the codes will run, and then the Named "
                    + "Trigger will be"
                    +
                    " called, just like how you do with #CALL. This can be useful if you have "
                    + "variables in the Named Trigger"
                    +
                    " that has to be initialized.");
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
                    "&b/triggerreactor[trg] timings toggle &8- &7turn on/off timings analysis. Also analysis will be "
                            + "reset.");
            sender.sendMessage(
                    "&b/triggerreactor[trg] timings reset &8- &7turn on/off timings analysis. Also analysis will be "
                            + "reset.");
            sender.sendMessage("&b/triggerreactor[trg] timings print &8- &7Show analysis result.");
            sender.sendMessage("  &b/triggerreactor[trg] timings print xx &8- &7Save analysis to file named xx"
                    + ".timings");
        }));
    }};

    private static final List<String> EMPTY = new ArrayList<String>();

    private interface Paragraph {
        void sendParagraph(ICommandSender sender);
    }
}
