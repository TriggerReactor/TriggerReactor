package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.areaselection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.tools.StringUtils;
import io.github.wysohn.triggerreactor.tools.TimeUtil;
import io.github.wysohn.triggerreactor.tools.script.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.stream.SenderOutputStream;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TriggerCommand {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]+.[0-9]{0,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_]+$");

    @Inject
    Logger logger;
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    IGameController gameController;
    @Inject
    ScriptEditManager scriptEditManager;
    @Inject
    GlobalVariableManager globalVariableManager;
    @Inject
    IThrowableHandler throwableHandler;
    @Inject
    InventoryEditManager invEditManager;
    @Inject
    AreaSelectionManager selectionManager;
    @Inject
    ExecutorManager executorManager;
    @Inject
    PlaceholderManager placeholderManager;

    @Inject
    ClickTriggerManager clickManager;
    @Inject
    WalkTriggerManager walkManager;
    @Inject
    CommandTriggerManager cmdManager;
    @Inject
    NamedTriggerManager namedTriggerManager;
    @Inject
    InventoryTriggerManager invManager;
    @Inject
    AreaTriggerManager areaManager;
    @Inject
    CustomTriggerManager customManager;
    @Inject
    RepeatingTriggerManager repeatManager;

    @Inject
    Set<Manager> managers;
    @Inject
    @Named("DataFolder")
    File dataFolder;

    private void showPluginDesc(ICommandSender sender, int spaces) {
        sender.sendMessage("&7-----     &6" + pluginLifecycleController.getPluginDescription() + "&7    ----");
    }

    private final CommandBuilder commandBuilder = CommandBuilder.begin(this::showPluginDesc)
            .leaf("debug", "&b/triggerreactor[trg] debug &8- &7Toggle debug mode to see detailed error messages.",
                    (sender, args) -> {
                        pluginLifecycleController.setDebugging(!pluginLifecycleController.isDebugging());
                        String color = pluginLifecycleController.isDebugging() ? "a" : "c";
                        sender.sendMessage("Debugging is set to &" + color + pluginLifecycleController.isDebugging());

                        logger.info("Debugging state: " + pluginLifecycleController.isDebugging());
                        return true;
                    })
            .leaf("version", "&b/triggerreactor[trg] version &8- &7Check current version of the plugin.", (sender,
                                                                                                           args) -> {
                sender.sendMessage("Current version: " + pluginLifecycleController.getVersion());
                return true;
            })
            .leaf(new String[]{"click", "c"}, (sender, spaces) -> {
                sender.sendMessage(StringUtils.spaces(spaces)
                        + "&b/triggerreactor[trg] click[c] [...] &8- &7create a click trigger.");
                sender.sendMessage(StringUtils.spaces(spaces) + "  &7/trg c #MESSAGE \"HEY YOU CLICKED!\"");
                sender.sendMessage(StringUtils.spaces(spaces)
                        + "  &7To create lines of script, simply type &b/trg c &7without extra parameters.");
                sender.sendMessage(StringUtils.spaces(spaces)
                        + "  &7Later, you can hold &ba bone &7and &6right click to inspect&7, &6left click to "
                        + "delete&7, or &6shift-right click to edit&7.");
            }, (sender, args) -> {
                String script = ITriggerCommand.consumeAllArguments(args);
                scriptEditManager.startEdit(sender, "Click Trigger", script, (ScriptEditor.SaveHandler) edits -> {
                    if (clickManager.startLocationSet((IPlayer) sender, edits)) {
                        sender.sendMessage("&7Now click the block to set click trigger.");
                    } else {
                        sender.sendMessage("&7Already on progress.");
                    }
                }, script.length() > 0);
                return true;
            })
            .leaf(new String[]{"walk", "w"}, (sender, spaces) -> {
                sender.sendMessage(StringUtils.spaces(spaces)
                        + "&b/triggerreactor[trg] walk[w] new [...] &8- &7create a walk trigger.");
                sender.sendMessage(StringUtils.spaces(spaces) + "  &7/trg w #MESSAGE \"HEY YOU WALKED!\"");
                sender.sendMessage(StringUtils.spaces(spaces)
                        + "  &7To create lines of script, simply type &b/trg w &7without extra parameters.");
                sender.sendMessage(StringUtils.spaces(spaces)
                        + "  &7Later, you can hold &ba bone &7and &6right click to inspect&7, &6left click to "
                        + "delete&7, or &6shift-right click to edit&7.");
            }, (sender, args) -> {
                String script = ITriggerCommand.consumeAllArguments(args);
                scriptEditManager.startEdit(sender, "Walk Trigger", script, edits -> {
                    if (walkManager.startLocationSet((IPlayer) sender, edits)) {
                        sender.sendMessage("&7Now click the block to set walk trigger.");
                    } else {
                        sender.sendMessage("&7Already on progress.");
                    }
                }, script.length() > 0);
                return true;
            })
            .composite(new String[]{"command",
                                    "cmd"}, "&b/triggerreactor[trg] command[cmd]", builder -> builder.leaf("new",
                            (sender, spaces) -> {
                                sender.sendMessage(StringUtils.spaces(spaces)
                                        + "&b/triggerreactor[trg] command[cmd] new <command name> [...] &8- &7create a "
                                        + "command trigg&b/triggerreactor[trg] command[cmd] new <command name> [...] &8- " +
                                        "&7er"
                                        + ".");
                                sender.sendMessage(
                                        StringUtils.spaces(spaces) + "  &7/trg cmd new test #MESSAGE \"I'M test COMMAND" +
                                                "!\"");
                                sender.sendMessage(StringUtils.spaces(spaces)
                                        + "  &7To create lines of script, simply type &b/trg cmd new <command name> &7without"
                                        + " extra parameters.");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (cmdManager.has(name)) {
                                    sender.sendMessage("&cCommand is already bound.");
                                    return true;
                                }

                                String script = ITriggerCommand.consumeAllArguments(args);
                                scriptEditManager.startEdit(sender, "Command Trigger", script, edits -> {
                                    if (cmdManager.addCommandTrigger(name, edits)) {
                                        sender.sendMessage("&aCommand trigger is bound!");
                                    } else {
                                        sender.sendMessage("&cCommand is already binded.");
                                    }
                                }, script.length() > 0);
                                return true;
                            })
                    .leaf("edit", "&b/triggerreactor[trg] command[cmd] edit <command name>&8- &7Open the editor and "
                            + "edit the script.", (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        if (!cmdManager.has(name)) {
                            sender.sendMessage("&cCommand not found.");
                            return true;
                        }

                        CommandTrigger trigger = cmdManager.get(name);
                        scriptEditManager.startEdit(sender, "Command Trigger", trigger.getScript(), edits -> {
                            try {
                                trigger.setScript(edits);
                            } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                                throwableHandler.handleException(sender, e);
                            }
                        }, false);
                        return true;
                    })
                    .leaf(new String[]{"delete", "del", "remove"},
                            "&b/triggerreactor[trg] command[cmd] delete <command name>&8- "
                                    + "&7Remove the command trigger.", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                if (cmdManager.remove(name) == null) {
                                    sender.sendMessage("&cCommand not found.");
                                    return true;
                                }

                                sender.sendMessage("&aCommand is deleted.");
                                return true;
                            })
                    .leaf("sync", "&b/trg cmd sync <command name>&8- &7to toggle sync mode.", (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        if (!cmdManager.has(name)) {
                            sender.sendMessage("&cCommand not found.");
                            return true;
                        }

                        CommandTrigger trigger = cmdManager.get(name);
                        trigger.getInfo().setSync(!trigger.getInfo().isSync());
                        sender.sendMessage(
                                "&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c") + trigger.getInfo()
                                        .isSync());

                        return true;
                    })
                    .leaf(new String[]{"permission", "p"}, (sender, lines) -> {
                        sender.sendMessage("&b/trg cmd permission[p] <command name> x.y x.z y.y ...&7.");
                        sender.sendMessage("    &6*&7Not providing any permission will remove them instead.");
                    }, (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        CommandTrigger trigger = cmdManager.get(name);

                        //if no permission is given, delete all permission required
                        String[] permissions = args.size() < 1 ? null : new String[args.size()];
                        int i = 0;
                        while (!args.isEmpty())
                            permissions[i++] = args.poll();

                        trigger.setPermissions(permissions);
                        if (permissions == null) {
                            sender.sendMessage("&7Cleared permissions");
                        } else {
                            sender.sendMessage("&7Set permissions.");
                        }

                        return true;
                    })
                    .leaf(new String[]{"aliases", "a"}, (sender, spaces) -> {
                        sender.sendMessage("&b/trg cmd <command name> aliases[a] alias1 alias2 ...&7.");
                        sender.sendMessage("    &6*&7Not providing any alias will remove them instead.");
                    }, (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        CommandTrigger trigger = cmdManager.get(name);

                        //if no aliases are given, delete all aliases
                        String[] aliases = args.size() < 1 ? null : new String[args.size()];
                        int i = 0;
                        while (!args.isEmpty())
                            aliases[i++] = args.poll();

                        trigger.setAliases(aliases);
                        if (aliases == null) {
                            sender.sendMessage("&7Cleared aliases");
                        } else {
                            sender.sendMessage("&7Set Aliases");
                        }

                        cmdManager.reregisterCommand(name);
                        return true;
                    })
                    .leaf(new String[]{"settab", "tab"}, (sender, spaces) -> {
                        sender.sendMessage("&b/trg cmd settab[tab] <command name> <a/b/c>:a,b,c <player>:$playerlist "
                                + "this,it,that");
                        sender.sendMessage("    &6*&7The parameter has following format&8: &6hint&d:&6val1,val2,...");
                        sender.sendMessage("    &6*&7Not providing any tab-completer will remove it instead.");
                        sender.sendMessage(
                                "    &7Hint shows up as simple string when a user is about to type something, and "
                                        + "values "
                                        + "will start to show up as a form of tab-completers as soon as their first "
                                        + "characters matching with the characters typed by the user.");
                        sender.sendMessage(
                                "    &7You may omit the hint, yet you cannot omit the values. To use only hint but no"
                                        + " values, edit the config file manually.");
                    }, (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        TriggerInfo info = Optional.of(cmdManager)
                                .map(man -> man.get(name))
                                .map(Trigger::getInfo)
                                .orElseThrow(() -> new RuntimeException("Missing TriggerInfo"));

                        List<Map<String, Object>> tabs = new ArrayList<>();
                        while (!args.isEmpty()) {
                            String[] split = args.poll().split(":", 2);
                            String hint = split.length == 2 ? split[0] : null;
                            String values = split.length == 2 ? split[1] : split[0];

                            Map<String, Object> tab = new LinkedHashMap<>();
                            if (hint != null)
                                tab.put(CommandTriggerManager.HINT, hint);
                            tab.put(CommandTriggerManager.CANDIDATES, values);
                            tabs.add(tab);
                        }

                        info.getConfig().put(CommandTriggerManager.TABS, tabs);
                        cmdManager.reload(name);

                        sender.sendMessage("&7Set tab-completer");
                        return true;
                    }))
            .composite(new String[]{"variables", "vars"}, (sender, spaces) -> {
                sender.sendMessage("&b/triggerreactor[trg] variables[vars] [...] &8- &7set global variables.");
                sender.sendMessage("  &7&cWarning - This command will delete the previous data associated with the "
                        + "key if exists.");
            }, builder -> builder.leaf(new String[]{"location", "loc"},
                            "&7/trg vars location[loc] test &8- &7save current "
                                    + "location into global variable 'test'.", (sender, args) -> {
                                String name = args.poll();
                                if (!GlobalVariableManager.isValidName(name)) {
                                    sender.sendMessage("&c" + name + " is not a valid key!");
                                    return true;
                                }

                                SimpleLocation loc = ((IPlayer) sender).getLocation();
                                try {
                                    globalVariableManager.put(name, loc);
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }

                                sender.sendMessage("&aLocation saved!");
                                return true;
                            })
                    .leaf(new String[]{"item", "i"},
                            "&7/trg vars item[i] gifts.item1 &8- &7save hand held item into global "
                                    + "variable 'gifts.item1'.", (sender, args) -> {
                                String name = args.poll();
                                if (!GlobalVariableManager.isValidName(name)) {
                                    sender.sendMessage("&c" + name + " is not a valid key!");
                                    return true;
                                }

                                IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                if (IS == null) {
                                    sender.sendMessage("&cYou are holding nothing on your main hand!");
                                    return true;
                                }

                                try {
                                    globalVariableManager.put(name, IS.get());
                                } catch (Exception e) {
                                    throwableHandler.handleException(sender, e);
                                }

                                sender.sendMessage("&aItem saved!");
                                return true;
                            })
                    .leaf(new String[]{"literal", "lit"},
                            "&7/trg vars literal[lit] test 13.5 &8- &7save 13.5 into global "
                                    + "variable 'test'. &dNumber of booleans will be automatically converted to the "
                                    + "appropriate type, but the other values will be treated as a string.", (sender,
                                                                                                              args) -> {
                                String name = args.poll();
                                String value = args.poll();

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
                                return true;
                            })
                    .leaf(new String[]{"read", "r"}, "&7/trg vars read[r] test &8- &7check what is saved in the global "
                            + "variable 'test'. null if nothing", (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        sender.sendMessage("&7Value of " + name + ": " + globalVariableManager.get(name));
                        return true;
                    })
                    .leaf("delete", "&7/trg vars delete test &8- &7delete global variable 'test'. Nothing happens if "
                            + "it doesn't exist.", (sender, args) -> {
                        String name = args.poll();
                        if (name == null)
                            return false;

                        if (globalVariableManager.has(name)) {
                            globalVariableManager.remove(name);
                            sender.sendMessage("&aDeleted.");
                        } else {
                            sender.sendMessage("&7The variable does not exist.");
                        }

                        return true;
                    })
                    .leaf("run", (sender, spaces) -> {
                        sender.sendMessage(
                                "&b/triggerreactor[trg] run [...] &8- &7Run simple script now without making a "
                                        + "trigger.");
                        sender.sendMessage("  &7/trg run #TP {\"MahPlace\"}");
                    }, (sender, args) -> {
                        String script = ITriggerCommand.consumeAllArguments(args);

                        try {
                            Trigger trigger = cmdManager.createTempCommandTrigger(script);

                            Map<String, Object> varMap = new HashMap<>();
                            varMap.put(Trigger.VAR_NAME_EVENT, gameController.createEmptyPlayerEvent(sender));

                            trigger.activate(varMap);
                        } catch (Exception e) {
                            throwableHandler.handleException(sender, e);
                        }

                        return true;
                    })
                    .leaf("sudo", (sender, args) -> {
                        sender.sendMessage(
                                "&b/triggerreactor[trg] sudo <player> [...] &8- &7Run simple script now without "
                                        + "making a trigger.");
                        sender.sendMessage("  &7/trg sudo wysohn #TP {\"MahPlace\"}");
                    }, (sender, args) -> {
                        String playerName = args.poll();
                        if (playerName == null)
                            return false;

                        String script = ITriggerCommand.consumeAllArguments(args);
                        IPlayer targetPlayer = gameController.getPlayer(playerName);
                        if (targetPlayer == null) {
                            sender.sendMessage("&cNo such player named &6" + playerName + "&c!");
                            return true;
                        }

                        try {
                            Trigger trigger = cmdManager.createTempCommandTrigger(script);

                            Map<String, Object> varMap = new HashMap<>();
                            varMap.put(Trigger.VAR_NAME_EVENT, gameController.createEmptyPlayerEvent(targetPlayer));

                            trigger.activate(varMap);
                        } catch (Exception e) {
                            throwableHandler.handleException(sender, e);
                        }

                        return true;
                    })
                    .leaf("call", (sender, spaces) -> {
                        sender.sendMessage(
                                "&b/triggerreactor[trg] call <named trigger> [codes ...] &8- &7Run Named Trigger "
                                        + "directly.");
                        sender.sendMessage("  &7/trg call MyNamedTrigger abc = {\"MahPlace\"}");
                        sender.sendMessage(
                                "  &7the last argument (codes ...) are just like any script, so you can imagine "
                                        + "that a temporary trigger will be made, the codes will run, and then the "
                                        + "Named Trigger will"
                                        + " be called, just like how you do with #CALL. This can be useful if you "
                                        + "have variables in " + "the Named Trigger that has to be initialized.");
                    }, (sender, args) -> {
                        String namedTriggerName = args.poll();
                        String script = ITriggerCommand.consumeAllArguments(args);

                        try {
                            Trigger trigger = cmdManager.createTempCommandTrigger(script);
                            Trigger targetTrigger = namedTriggerManager.get(namedTriggerName);
                            if (targetTrigger == null) {
                                sender.sendMessage(
                                        "&cCannot find &6" + namedTriggerName + "&c! &7Remember that the folder "
                                                + "hierarchy is represented with ':' sign. (ex. "
                                                + "FolderA:FolderB:Trigger)");
                                return true;
                            }

                            Map<String, Object> variables = new HashMap<>(); // shares same variable space
                            variables.put(Trigger.VAR_NAME_EVENT, gameController.createEmptyPlayerEvent(sender));

                            trigger.activate(variables);
                            targetTrigger.activate(variables);

                        } catch (Exception e) {
                            throwableHandler.handleException(sender, e);
                        }

                        return true;
                    })
                    .composite(new String[]{"inventory",
                                            "i"}, "&b/triggerreactor[trg] inventory[i]", inner -> inner.leaf("create"
                                    , (sender, spaces) -> {
                                        sender.sendMessage(
                                                "&b/triggerreactor[trg] inventory[i] create <inventory name> " + "<size> [...] "
                                                        + "&8- &7create a new inventory. <size> must be multiple of 9. The "
                                                        + "<size> "
                                                        + "cannot be " + "larger than 54");
                                        sender.sendMessage("  &7/trg i MyInventory create 54");
                                    }, (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;
                                        String sizeStr = args.poll();
                                        if (sizeStr == null)
                                            return false;

                                        int size = -1;
                                        try {
                                            size = Integer.parseInt(sizeStr);
                                        } catch (NumberFormatException e) {
                                            sender.sendMessage("&c" + sizeStr + " is not a valid number");
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

                                        String script = ITriggerCommand.consumeAllArguments(args);
                                        int finalSize = size;
                                        scriptEditManager.startEdit(sender, "Inventory Trigger", script, edits -> {
                                            try {
                                                if (invManager.createTrigger(finalSize, name, edits)) {
                                                    sender.sendMessage("&aInventory Trigger created!");
                                                } else {
                                                    sender.sendMessage(
                                                            "&7Another Inventory Trigger with that name already " + "exists");
                                                }
                                            } catch (Exception e) {
                                                throwableHandler.handleException(sender, e);
                                            }
                                        }, script.length() > 0);

                                        return true;
                                    })
                            .leaf("delete", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] inventory[i] delete <inventory name>&8- &7"
                                        + "delete this inventory");
                                sender.sendMessage("  &7/trg i delete MyInventory");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                if (invManager.remove(name) != null) {
                                    sender.sendMessage("&aDeleted!");
                                } else {
                                    sender.sendMessage("&7No such inventory trigger found.");
                                }
                                return true;
                            })
                            .leaf("item", (sender, spaces) -> {
                                sender.sendMessage(
                                        "&b/triggerreactor[trg] inventory[i] item <inventory name> <index> &8- "
                                                + "&7sets item of inventory to the held item. Clears the slot if you "
                                                + "are holding " + "nothing.");
                                sender.sendMessage("  &7/trg i item MyInventory 0");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;
                                String indexStr = args.poll();
                                if (indexStr == null)
                                    return false;

                                IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                IS = IS == null ? null : IS.clone();

                                int index;
                                try {
                                    index = Integer.parseInt(indexStr);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("&c" + indexStr + " is not a valid number.");
                                    return true;
                                }

                                InventoryTrigger trigger = invManager.get(name);
                                if (trigger == null) {
                                    sender.sendMessage("&7No such Inventory Trigger named " + name);
                                    return true;
                                }

                                if (index > trigger.getItems().length || index < 1) {
                                    sender.sendMessage(
                                            "&c" + index + " is out of bounds. (Size: " + (trigger.getItems().length)
                                                    + ")");
                                    return true;
                                }

                                trigger.getItems()[index - 1] = IS;

                                sender.sendMessage("Successfully set item " + index);
                                return true;
                            })
                            .leaf("column",
                                    "&b/triggerreactor[trg] inventory[i] column <inventory name> <index> &8- &7same "
                                            + "as the item subcommand, but applied to an entire column.Clears the "
                                            + "slot if you are " + "holding nothing.", (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;
                                        String indexStr = args.poll();
                                        if (indexStr == null)
                                            return false;

                                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                        IS = IS == null ? null : IS.clone();

                                        int index;
                                        try {
                                            index = Integer.parseInt(indexStr);
                                        } catch (NumberFormatException e) {
                                            sender.sendMessage("&c" + indexStr + " is not a valid number.");
                                            return true;
                                        }

                                        InventoryTrigger trigger = invManager.get(name);
                                        if (trigger == null) {
                                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                                            return true;
                                        }

                                        int rows = trigger.getItems().length / 9;
                                        if (index > 9 || index < 1) {
                                            sender.sendMessage("&c" + index + " is out of bounds. (Maximum: 9)");
                                            return true;
                                        }

                                        for (int i = 0; i < rows; i++) {
                                            trigger.getItems()[index - 1 + i * 9] = IS;
                                        }

                                        sender.sendMessage("Successfully filled column " + index);
                                        return true;
                                    })
                            .leaf("row",
                                    "&b/triggerreactor[trg] inventory[i] row <inventory name> <index> &8- &7same as "
                                            + "the "
                                            + "item subcommand, but applied to an entire row.", (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;
                                        String indexStr = args.poll();
                                        if (indexStr == null)
                                            return false;

                                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                        IS = IS == null ? null : IS.clone();

                                        int index;
                                        try {
                                            index = Integer.parseInt(indexStr);
                                        } catch (NumberFormatException e) {
                                            sender.sendMessage("&c" + indexStr + " is not a valid number.");
                                            return true;
                                        }

                                        InventoryTrigger trigger = invManager.get(name);
                                        if (trigger == null) {
                                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                                            return true;
                                        }

                                        int rows = trigger.getItems().length / 9;
                                        if (index > rows || index < 1) {
                                            sender.sendMessage(
                                                    "&c" + index + " is out of bounds. (Maximum: " + rows + ")");
                                            return true;
                                        }

                                        for (int i = 0; i < 9; i++) {
                                            trigger.getItems()[(index - 1) * 9 + i] = IS;
                                        }

                                        sender.sendMessage("Successfully filled row " + index);
                                        return true;
                                    })
                            .leaf("open", (sender, spaces) -> {
                                sender.sendMessage(
                                        "&b/triggerreactor[trg] inventory[i] open <inventory name>&8- &7Preview "
                                                + "the inventory");
                                sender.sendMessage(
                                        "&b/triggerreactor[trg] inventory[i] open <inventory name> <player name>"
                                                + " &8- &7Send <player name> a preview of the inventory");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                String targetName = args.poll();

                                IPlayer forWhom = null;
                                if (targetName == null) {
                                    forWhom = (IPlayer) sender;
                                } else {
                                    IPlayer p = gameController.getPlayer(targetName);
                                    if (p != null)
                                        forWhom = p;
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
                                return true;
                            })
                            .leaf("edit",
                                    "&b/triggerreactor[trg] inventory[i] edit <inventory name>&8- &7Edit the inventory"
                                            + " trigger.", (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;

                                        InventoryTrigger trigger = invManager.get(name);
                                        if (trigger == null) {
                                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                                            return true;
                                        }

                                        scriptEditManager.startEdit(sender, trigger.getInfo()
                                                .getTriggerName(), trigger.getScript(), edits -> {
                                            try {
                                                trigger.setScript(edits);
                                            } catch (Exception e) {
                                                throwableHandler.handleException(sender, e);
                                            }

                                            sender.sendMessage("&aScript is updated!");
                                        }, false);

                                        return true;
                                    })
                            .leaf("edititems",
                                    "&b/triggerreactor[trg] inventory[i] edititems <inventory name>&8- &7open an "
                                            + "interactable GUI where you can edit the contents of the inventory "
                                            + "trigger.", (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;

                                        InventoryTrigger trigger = invManager.get(name);
                                        if (trigger == null) {
                                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                                            return true;
                                        }

                                        invEditManager.onStartEdit((IPlayer) sender, trigger);
                                        return true;
                                    })
                            .leaf("settitle",
                                    "&b/triggerreactor[trg] inventory[i] settitle <inventory name> <title> &8- "
                                            + "&7set title of inventory", (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;

                                        String title = ITriggerCommand.consumeAllArguments(args);

                                        InventoryTrigger trigger = invManager.get(name);
                                        if (trigger == null) {
                                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                                            return true;
                                        }

                                        TriggerInfo info = trigger.getInfo();
                                        info.getConfig().put(InventoryTriggerManager.TITLE, title);

                                        invManager.reload(name);

                                        sender.sendMessage("Successfully changed title");

                                        return true;
                                    }))
                    .composite("item", "&b/triggerreactor[trg] item", inner -> inner.leaf("title",
                                    "&b/triggerreactor[trg] item title <item title> &8- &7"
                                            + "Change the title of holding item", (sender, args) -> {
                                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                        if (IS == null) {
                                            sender.sendMessage("&cYou are holding nothing.");
                                            return true;
                                        }

                                        String title = ITriggerCommand.consumeAllArguments(args);
                                        gameController.setItemTitle(IS, title);

                                        ((IPlayer) sender).setItemInMainHand(IS);
                                        return true;
                                    })
                            .composite("lore", "&b/triggerreactor[trg] item lore", inner2 -> inner2.leaf("add",
                                            "&b/triggerreactor[trg] item lore add <line> &8- &7"
                                                    + "Append lore to the holding item", (sender, args) -> {
                                                IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                                if (IS == null) {
                                                    sender.sendMessage("&cYou are holding nothing.");
                                                    return true;
                                                }

                                                String lore = ITriggerCommand.consumeAllArguments(args);
                                                gameController.addItemLore(IS, lore);

                                                ((IPlayer) sender).setItemInMainHand(IS);
                                                return true;
                                            })
                                    .leaf("set", "&b/triggerreactor[trg] item lore set <index> <line> &8- &7"
                                            + "Replace lore at the specified index.(Index start from 0)", (sender,
                                                                                                           args) -> {
                                        String indexStr = args.poll();
                                        if (indexStr == null)
                                            return false;

                                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                        if (IS == null) {
                                            sender.sendMessage("&cYou are holding nothing.");
                                            return true;
                                        }

                                        int index = -1;
                                        try {
                                            index = Integer.parseInt(indexStr);
                                        } catch (NumberFormatException e) {
                                            sender.sendMessage("&c" + indexStr + " is not a valid number");
                                            return true;
                                        }

                                        String lore = ITriggerCommand.consumeAllArguments(args);

                                        if (!gameController.setLore(IS, index - 1, lore)) {
                                            sender.sendMessage("&c" + index + " is out of bounds.");
                                            return true;
                                        }

                                        ((IPlayer) sender).setItemInMainHand(IS);
                                        return true;
                                    })
                                    .leaf("remove", "&b/triggerreactor[trg] item lore remove <index> &8- &7"
                                            + "Delete lore at the specified index.", (sender, args) -> {
                                        String indexStr = args.poll();
                                        if (indexStr == null)
                                            return false;

                                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                                        if (IS == null) {
                                            sender.sendMessage("&cYou are holding nothing.");
                                            return true;
                                        }

                                        int index = -1;
                                        try {
                                            index = Integer.parseInt(indexStr);
                                        } catch (NumberFormatException e) {
                                            sender.sendMessage("&c" + index + " is not a valid number");
                                            return true;
                                        }

                                        if (!gameController.removeLore(IS, index - 1)) {
                                            sender.sendMessage("&7No lore at index " + index);
                                            return true;
                                        }

                                        ((IPlayer) sender).setItemInMainHand(IS);
                                        return true;
                                    })))
                    .composite(new String[]{"area",
                                            "a"}, "&b/triggerreactor[trg] area[a]", inner -> inner.leaf("toggle",
                                    "&b/triggerreactor[trg] area[a] toggle &8- &7"
                                            + "Enable/Disable area selection mode.", (sender, args) -> {
                                        boolean result =
                                                selectionManager.toggleSelection(((IPlayer) sender).getUniqueId());

                                        sender.sendMessage("&7Area selection mode enabled: &6" + result);
                                        return true;
                                    })
                            .leaf("create", "&b/triggerreactor[trg] area[a] create <name> &8- &7"
                                    + "Create area trigger out of selected region.", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                // validate the name
                                if (!NAME_PATTERN.matcher(name).matches()) {
                                    sender.sendMessage("&cThe name " + name + " has not allowed character!");
                                    sender.sendMessage("&7Use only character, number, and underscore(_).");
                                    return true;
                                }

                                AreaTrigger trigger = areaManager.get(name);
                                if (trigger != null) {
                                    sender.sendMessage("&cArea Trigger " + name + " is already exists!");
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


                                    selectionManager.resetSelections(((IPlayer) sender).getUniqueId());
                                } else {
                                    sender.sendMessage("&7Area Trigger " + name + " already exists.");
                                }

                                return true;
                            })
                            .leaf("delete", "&b/triggerreactor[trg] area[a] delete <name> &8- &7"
                                    + "Delete area trigger. BE CAREFUL!", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                if (areaManager.remove(name) != null) {
                                    sender.sendMessage("&aArea Trigger deleted");


                                    selectionManager.resetSelections(((IPlayer) sender).getUniqueId());
                                } else {
                                    sender.sendMessage("&7Area Trigger " + name + " does not exist.");
                                }
                                return true;
                            })
                            .leaf("enter", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] area[a] <name> enter [...] &8- &7"
                                        + "Enable/Disable area selection mode.");
                                sender.sendMessage("  &7/trg a TestingArea enter #MESSAGE \"Welcome\"");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                AreaTrigger trigger = areaManager.get(name);
                                if (trigger == null) {
                                    sender.sendMessage("&7No Area Trigger named " + name + ".");
                                    return true;
                                }

                                String script = trigger.getEnterTrigger() == null
                                        ? ITriggerCommand.consumeAllArguments(args)
                                        : trigger.getEnterTrigger().getScript();
                                scriptEditManager.startEdit(sender, "Area Trigger [Enter]", script, edits -> {
                                    try {
                                        trigger.setEnterTrigger(edits);

                                        sender.sendMessage("&aScript is updated!");
                                    } catch (Exception e) {
                                        throwableHandler.handleException(sender, e);
                                    }
                                }, script.length() > 0);

                                return true;
                            })
                            .leaf("exit", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] area[a] <name> exit [...] &8- &7"
                                        + "Enable/Disable area selection mode.");
                                sender.sendMessage("  &7/trg a TestingArea exit #MESSAGE \"Bye\"");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                AreaTrigger trigger = areaManager.get(name);
                                if (trigger == null) {
                                    sender.sendMessage("&7No Area Trigger named " + name + ".");
                                    return true;
                                }

                                String script = trigger.getExitTrigger() == null
                                        ? ITriggerCommand.consumeAllArguments(args)
                                        : trigger.getExitTrigger().getScript();
                                scriptEditManager.startEdit(sender, "Area Trigger [Exit]", script, edits -> {
                                    try {
                                        trigger.setExitTrigger(edits);

                                        sender.sendMessage("&aScript is updated!");
                                    } catch (Exception e) {
                                        throwableHandler.handleException(sender, e);
                                    }
                                }, script.length() > 0);

                                return true;
                            })
                            .leaf("sync", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] area[a] <name> sync &8- &7"
                                        + "Enable/Disable sync mode.");
                                sender.sendMessage("  &7"
                                        + "Setting it to true when you want to cancel event (with #CANCELEVENT). "
                                        + "However, "
                                        + "setting sync mode will make the trigger run on server thread; keep in mind"
                                        + " that it can lag the server if you have too much things going on within "
                                        + "the code. Set it to false always if you are not sure.");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                AreaTrigger trigger = areaManager.get(name);
                                if (trigger == null) {
                                    sender.sendMessage("&7No Area Trigger found with that name.");
                                    return true;
                                }

                                trigger.getInfo().setSync(!trigger.getInfo().isSync());

                                sender.sendMessage(
                                        "&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c") + trigger.getInfo()
                                                .isSync());

                                return true;
                            }))
                    .composite("custom", "&b/triggerreactor[trg] custom", inner -> inner.leaf(new String[]{"create",
                                                                                                           "edit"},
                                    (sender, spaces) -> {
                                        sender.sendMessage(
                                                "&b/triggerreactor[trg] custom create <event> <name> [...] &8- " + "&7Create a "
                                                        + "custom trigger.");
                                        sender.sendMessage(
                                                "&b/triggerreactor[trg] custom edit <event> <name> &8- &Edit a custom " +
                                                        "trigger.");
                                        sender.sendMessage(
                                                "  &7/trg custom create onJoin Greet #BROADCAST \"Please welcome " + "\"+player"
                                                        + ".getName()+\"!\"");
                                    }, (sender, args) -> {
                                        String eventName = args.poll();
                                        if (eventName == null)
                                            return false;
                                        String name = args.poll();
                                        if (name == null)
                                            return false;

                                        CustomTrigger trigger = customManager.get(name);
                                        String script =
                                                trigger == null ? ITriggerCommand.consumeAllArguments(args) :
                                                        trigger.getScript();
                                        scriptEditManager.startEdit(sender,
                                                "Custom Trigger[" + eventName.substring(Math.max(0, eventName.length() - 10))
                                                        + "]", script, edits -> {
                                                    if (trigger == null) {
                                                        try {
                                                            customManager.createCustomTrigger(eventName, name, script);

                                                            sender.sendMessage("&aCustom Trigger created!");
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                            sender.sendMessage("&cCould not save! " + e.getMessage());
                                                            sender.sendMessage("&cSee console for detailed messages.");
                                                        }
                                                    } else {
                                                        try {
                                                            trigger.setScript(edits);
                                                        } catch (Exception e) {
                                                            throwableHandler.handleException(sender, e);
                                                        }

                                                        sender.sendMessage("&aScript is updated!");
                                                    }
                                                }, trigger == null);

                                        return true;
                                    })
                            .leaf("delete", "&b/triggerreactor[trg] custom delete <name> &8- &7Delete a custom "
                                    + "trigger.", (sender, args) -> {
                                String key = args.poll();
                                if (key == null)
                                    return false;

                                if (customManager.remove(key) != null) {
                                    sender.sendMessage("&aRemoved the custom trigger &6" + key);
                                } else {
                                    sender.sendMessage("&7Custom Trigger &6" + key + "&7 does not exist");
                                }
                                return true;
                            })
                            .leaf("sync",
                                    "&b/triggerreactor[trg] custom sync <name> &8- &7Toggle Sync/Async mode of custom"
                                            + " trigger <name>", (sender, args) -> {
                                        String name = args.poll();
                                        if (name == null)
                                            return false;

                                        CustomTrigger trigger = customManager.get(name);
                                        if (trigger == null) {
                                            sender.sendMessage("&7No Custom Trigger found with that name.");
                                            return true;
                                        }

                                        trigger.getInfo().setSync(!trigger.getInfo().isSync());

                                        sender.sendMessage("&7Sync mode: " + (trigger.getInfo().isSync() ? "&a" : "&c")
                                                + trigger.getInfo().isSync());
                                        return true;
                                    }))
                    .composite(new String[]{"repeat", "r"}, "", inner -> inner.leaf(new String[]{"create",
                                                                                                 "edit"}, (sender,
                                                                                                           spaces) -> {
                                sender.sendMessage(
                                        "&b/triggerreactor[trg] repeat[r] create <name> &8- &7Create a Repeating " +
                                                "Trigger"
                                                + ".");
                                sender.sendMessage(
                                        "&b/triggerreactor[trg] repeat[r] edit <name> &8- &Edit a Repeating " +
                                                "Trigger.");
                                sender.sendMessage("  &7This creates a Repeating Trigger with default settings. You "
                                        + "probably "
                                        + "will want to " + "change default values using other commands below. Also, "
                                        + "creating "
                                        + "Repeating" + " Trigger doesn't start it automatically.");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                Trigger trigger = repeatManager.get(name);
                                String script =
                                        trigger == null ? ITriggerCommand.consumeAllArguments(args) :
                                                trigger.getScript();

                                scriptEditManager.startEdit(sender, "Repeating Trigger", script, edits -> {
                                    if (trigger != null) {
                                        try {
                                            trigger.setScript(edits);
                                        } catch (Exception e) {
                                            throwableHandler.handleException(sender, e);
                                        }

                                        sender.sendMessage("&aScript is updated!");
                                    } else {
                                        try {
                                            repeatManager.createTrigger(name, edits);
                                        } catch (Exception e) {
                                            throwableHandler.handleException(sender, e);
                                        }
                                    }
                                }, trigger == null);

                                return true;
                            })
                            .leaf("delete", "&b/triggerreactor[trg] repeat[r] delete <name> &8- &7"
                                    + "Delete repeating trigger.", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                RepeatingTrigger trigger = repeatManager.get(name);

                                if (trigger == null) {
                                    sender.sendMessage("&7No Repeating Trigger with name " + name);
                                    return true;
                                }

                                repeatManager.remove(name);
                                return true;
                            })
                            .leaf("interval", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] repeat[r] <name> interval <time format> &8- "
                                        + "&7Change the interval of this trigger.");
                                sender.sendMessage("  &7"
                                        + "Notice the <time format> is not just a number but has specific format for "
                                        + "it. For "
                                        + "example, you first type what number you want to set and also define the "
                                        + "unit of it. If you want it to repeat it every 1 hour, 20 minutes, "
                                        + "50seconds, and 10ticks, then it will be &6/trg r BlahBlah interval "
                                        + "1h20m50s10t.&7 Currently only h, m, s, and t are supported for this format"
                                        + ". Also notice that if you have two numbers with same format, they will add"
                                        + " up as well. For example,&6 /trg r BlahBlah interval 30s40s&7 will be "
                                        + "added up to 70seconds total. All units other than h, m, s, or t will be "
                                        + "ignored.");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                String intervalValue = args.poll();
                                if (intervalValue == null)
                                    return false;

                                RepeatingTrigger trigger = repeatManager.get(name);

                                if (trigger == null) {
                                    sender.sendMessage("&7No Repeating Trigger with name " + name);
                                    return true;
                                }

                                long interval = TimeUtil.parseTime(intervalValue);

                                trigger.setInterval(interval);

                                sender.sendMessage("&aNow &6[" + name + "]&a will run every &6["
                                        + TimeUtil.milliSecondsToString(interval) + "]");

                                return true;
                            })
                            .leaf("autostart", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] repeat[r] <name> autostart &8- &7"
                                        + "Enable/Disable automatic start for this trigger.");
                                sender.sendMessage("  &7"
                                        + "By setting this to &atrue&7, this trigger will start on plugin enables "
                                        + "itself. Otherwise, you have to start it yourself every time.");
                            }, (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                RepeatingTrigger trigger = repeatManager.get(name);

                                if (trigger == null) {
                                    sender.sendMessage("&7No Repeating Trigger with name " + name);
                                    return true;
                                }

                                trigger.setAutoStart(!trigger.isAutoStart());

                                sender.sendMessage(
                                        "Auto start: " + (trigger.isAutoStart() ? "&a" : "&c") + trigger.isAutoStart());

                                return true;
                            })
                            .leaf("toggle", "&b/triggerreactor[trg] repeat[r] <name> toggle &8- &7"
                                    + "Start or stop the Repeating Trigger.", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

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
                                    sender.sendMessage(
                                            "&aScheduled start up. It may take some time depends on CPU " + "usage.");
                                }

                                return true;
                            })
                            .leaf("pause", "&b/triggerreactor[trg] repeat[r] <name> pause &8- &7"
                                    + "Pause or unpause the Repeating Trigger.", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                RepeatingTrigger trigger = repeatManager.get(name);

                                if (trigger == null) {
                                    sender.sendMessage("&7No Repeating Trigger with name " + name);
                                    return true;
                                }

                                trigger.setPaused(!trigger.isPaused());

                                sender.sendMessage(
                                        "Paused: " + (trigger.isPaused() ? "&a" : "&c") + trigger.isPaused());

                                return true;
                            })
                            .leaf("status", "&b/triggerreactor[trg] repeat[r] <name> status &8- &7"
                                    + "See brief information about this trigger.", (sender, args) -> {
                                String name = args.poll();
                                if (name == null)
                                    return false;

                                RepeatingTrigger trigger = repeatManager.get(name);

                                if (trigger == null) {
                                    sender.sendMessage("&7No Repeating Trigger with name " + name);
                                    return true;
                                }

                                repeatManager.showTriggerInfo(sender, trigger);

                                return true;
                            }))
                    .leaf("search",
                            "&b/triggerreactor[trg] search &8- &7Show all trigger blocks in this chunk as glowing "
                                    + "stones.", (sender, args) -> {
                                SimpleChunkLocation scloc = ((IPlayer) sender).getChunk();
                                gameController.showGlowStones(sender, clickManager.getTriggersInChunk(scloc));
                                gameController.showGlowStones(sender, walkManager.getTriggersInChunk(scloc));
                                sender.sendMessage("&7Now trigger blocks will be shown as &6glowstone");

                                return true;
                            })
                    .leaf("list", "", (sender, args) -> {
                        Set<String> filterSet = new HashSet<>(args);

                        sender.sendMessage("- - - - - Result - - - - ");
                        managers.stream()
                                .filter(AbstractTriggerManager.class::isInstance)
                                .map(AbstractTriggerManager.class::cast)
                                .forEach(manager -> {
                                    for (Object val : manager.getTriggerList(filterSet::contains)) {
                                        sender.sendMessage("&d" + val);
                                    }
                                });
                        sender.sendMessage(" ");

                        return true;
                    })
                    .composite("timings", "&b/triggerreactor[trg] timings", inner -> inner.leaf("toggle",
                                    "&b/triggerreactor[trg] timings toggle &8- &7"
                                            + "turn on/off timings analysis. Also analysis will be reset.", (sender,
                                                                                                             args) -> {
                                        Timings.on = !Timings.on;

                                        if (Timings.on) {
                                            sender.sendMessage("&aEnabled");
                                        } else {
                                            sender.sendMessage("&cDisabled");
                                        }

                                        return true;
                                    })
                            .leaf("reset", "&b/triggerreactor[trg] timings reset &8- &7"
                                    + "turn on/off timings analysis. Also analysis will be reset.", (sender, args) -> {
                                Timings.reset();

                                sender.sendMessage("&aReset Complete.");

                                return true;
                            })
                            .leaf("print", (sender, spaces) -> {
                                sender.sendMessage("&b/triggerreactor[trg] timings print &8- &7Show analysis result.");
                                sender.sendMessage("&b/triggerreactor[trg] timings print xx &8- &7"
                                        + "Save analysis to file named xx.timings");
                            }, (sender, args) -> {
                                OutputStream os;

                                //TODO I/O
                                String fileName = args.poll();
                                if (fileName != null) {
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

                                return true;
                            }))
                    .leaf("reload", "&b/triggerreactor[trg] reload &8- &7Reload all scripts, variables, and settings"
                            + ".", (sender, args) -> {
                        for (Manager manager : managers)
                            manager.onReload();

                        executorManager.onReload();
                        placeholderManager.onReload();

                        sender.sendMessage("Reload Complete!");

                        return true;
                    })
                    .composite("links", "", inner -> inner.leaf("inveditsave", "", (sender, args) -> {
                        invEditManager.onSaveEdit((IPlayer) sender);
                        return true;
                    }).leaf("inveditcontinue", "", (sender, args) -> {
                        invEditManager.onContinueEdit((IPlayer) sender);
                        return true;
                    }).leaf("inveditdiscard", "", (sender, args) -> {
                        invEditManager.onDiscardEdit((IPlayer) sender);
                        return true;
                    })));

    @Inject
    TriggerCommand(){

    }

    public ITriggerCommand createCommand(){
        return commandBuilder.build();
    }
}
