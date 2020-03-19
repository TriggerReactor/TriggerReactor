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

import io.github.wysohn.triggerreactor.core.bridge.*;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.config.ConfigManager;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import io.github.wysohn.triggerreactor.tools.TimeUtil;
import io.github.wysohn.triggerreactor.tools.stream.SenderOutputStream;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The main abstract class of TriggerReactor. Interacting with any platform should extends this class to
 * create important internal components.
 *
 * @author wysohn
 */
public abstract class TriggerReactorCore implements TaskSupervisor {
    /**
     * Cached Pool for thread execution.
     */
    protected static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread() {{
                this.setPriority(MIN_PRIORITY);
            }};
        }
    });

    private static TriggerReactorCore instance;

    /**
     * get instance of this class.
     *
     * @return
     */
    public static TriggerReactorCore getInstance() {
        return instance;
    }
    
    private ConfigManager configManager;
    protected Map<String, AbstractAPISupport> sharedVars = new HashMap<>();

    protected TriggerReactorCore() {
        instance = this;
    }

    public Map<String, AbstractAPISupport> getSharedVars() {
        return sharedVars;
    }
    
    public abstract SelfReference getSelfReference();
    
    public abstract IWrapper getWrapper();

    public abstract AbstractExecutorManager getExecutorManager();

    public abstract AbstractPlaceholderManager getPlaceholderManager();

    public abstract AbstractVariableManager getVariableManager();

    public abstract AbstractScriptEditManager getScriptEditManager();

    public abstract AbstractPlayerLocationManager getLocationManager();

    public abstract AbstractPermissionManager getPermissionManager();

    public abstract AbstractAreaSelectionManager getSelectionManager();

    public abstract AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> getClickManager();

    public abstract AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> getWalkManager();

    public abstract AbstractCommandTriggerManager getCmdManager();

    public abstract AbstractInventoryTriggerManager getInvManager();

    public abstract AbstractInventoryEditManager getInvEditManager();

    public abstract AbstractAreaTriggerManager getAreaManager();

    public abstract AbstractCustomTriggerManager getCustomManager();

    public abstract AbstractRepeatingTriggerManager getRepeatManager();

    public abstract AbstractNamedTriggerManager getNamedTriggerManager();

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]+.[0-9]{0,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_]+$");
    private boolean debugging = false;

    public void onCoreEnable() {
        configManager = new ConfigManager(this, new File(getDataFolder(), "config.json"));
    }

    public void onCoreDisable() {
        Manager.getManagers().forEach(Manager::disable);
    }

    public boolean onCommand(ICommandSender sender, String command, String[] args) {
        if (command.equalsIgnoreCase("triggerreactor")) {
            if (!sender.hasPermission("triggerreactor.admin"))
                return true;

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

                    getLogger().info("Debugging state: " + debugging);
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    sender.sendMessage("Current version: " + getVersion());
                    return true;
                } else if (args[0].equalsIgnoreCase("click") || args[0].equalsIgnoreCase("c")) {
                    if (args.length == 1) {
                        getScriptEditManager().startEdit(sender, "Click Trigger", "", new SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                if (getClickManager().startLocationSet((IPlayer) sender, script)) {
                                    sender.sendMessage("&7Now click the block to set click trigger.");
                                } else {
                                    sender.sendMessage("&7Already on progress.");
                                }
                            }
                        });
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++)
                            builder.append(args[i]).append(" ");
                        if (getClickManager().startLocationSet((IPlayer) sender, builder.toString())) {
                            sender.sendMessage("&7Now click the block to set click trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("w")) {
                    if (args.length == 1) {
                        getScriptEditManager().startEdit(sender, "Walk Trigger", "", new SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                if (getWalkManager().startLocationSet((IPlayer) sender, script)) {
                                    sender.sendMessage("&7Now click the block to set walk trigger.");
                                } else {
                                    sender.sendMessage("&7Already on progress.");
                                }
                            }
                        });
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++)
                            builder.append(args[i]).append(" ");
                        if (getWalkManager().startLocationSet((IPlayer) sender, builder.toString())) {
                            sender.sendMessage("&7Now click the block to set walk trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args.length > 1 && (args[0].equalsIgnoreCase("command") || args[0].equalsIgnoreCase("cmd"))) {
                    if (args.length == 3 && getCmdManager().hasCommandTrigger(args[1]) && args[2].equals("sync")) {
                        Trigger trigger = getCmdManager().getCommandTrigger(args[1]);

                        trigger.setSync(!trigger.isSync());

                        sender.sendMessage("&7Sync mode: " + (trigger.isSync() ? "&a" : "&c") + trigger.isSync());
                        saveAsynchronously(getCmdManager());
                    } else if (args.length > 2 && getCmdManager().hasCommandTrigger(args[1])
                            && (args[2].equals("p") || args[2].equals("permission"))) {
                        CommandTrigger trigger = getCmdManager().getCommandTrigger(args[1]);

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
                        saveAsynchronously(getCmdManager());
                    } else if (args.length > 2 && getCmdManager().hasCommandTrigger(args[1])
                            && (args[2].equals("a") || args[2].equals("aliases"))) {
                        CommandTrigger trigger = getCmdManager().getCommandTrigger(args[1]);

                        //first, clean up all aliases
                        getCmdManager().removeAliases(trigger);

                        //if no aliases is given, delete all aliases
                        String[] aliases = null;
                        if (args.length == 3) {
                            trigger.setAliases(null);
                        } else {
                            aliases = new String[args.length - 3];
                            for (int i = 3; i < args.length; i++) {
                                aliases[i - 3] = args[i];
                            }
                            trigger.setAliases(aliases);
                            getCmdManager().registerAliases(trigger);
                        }
                        if (aliases == null) {
                            sender.sendMessage("&7Cleared aliases");
                        } else {
                            sender.sendMessage("&7Set Aliases");
                        }
                        saveAsynchronously(getCmdManager());
                    } else if (getCmdManager().hasCommandTrigger(args[1])) {
                        Trigger trigger = getCmdManager().getCommandTrigger(args[1]);

                        getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                try {
                                    trigger.setScript(script);
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }

                                sender.sendMessage("&aScript is updated!");

                                saveAsynchronously(getCmdManager());
                            }
                        });
                    } else {
                        if (args.length == 2) {
                            getScriptEditManager().startEdit(sender, "Command Trigger", "", new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    getCmdManager().addCommandTrigger(sender, args[1], script);

                                    sender.sendMessage("&aCommand trigger is binded!");

                                    saveAsynchronously(getCmdManager());
                                }
                            });
                        } else {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 2; i < args.length; i++)
                                builder.append(args[i] + " ");

                            getCmdManager().addCommandTrigger(sender, args[1], builder.toString());

                            sender.sendMessage("&aCommand trigger is binded!");

                            saveAsynchronously(getCmdManager());
                        }
                    }
                    return true;
                } else if ((args[0].equalsIgnoreCase("variables") || args[0].equalsIgnoreCase("vars"))) {
                    if (args.length == 3) {
                        if (args[1].equalsIgnoreCase("Item")) {
                            String name = args[2];
                            if (!AbstractVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                            if (IS == null) {
                                sender.sendMessage("&c" + "You are holding nothing on your main hand!");
                                return true;
                            }

                            try {
                                getVariableManager().put(name, IS.get());
                            } catch (Exception e) {
                                this.handleException(sender, e);
                            }

                            sender.sendMessage("&aItem saved!");
                        } else if (args[1].equalsIgnoreCase("Location")) {
                            String name = args[2];
                            if (!AbstractVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            ILocation loc = ((IPlayer) sender).getLocation();
                            try {
                                getVariableManager().put(name, loc.get());
                            } catch (Exception e) {
                                this.handleException(sender, e);
                            }

                            sender.sendMessage("&aLocation saved!");
                        } else {
                            String name = args[1];
                            String value = args[2];

                            if (!AbstractVariableManager.isValidName(name)) {
                                sender.sendMessage("&c" + name + " is not a valid key!");
                                return true;
                            }

                            if (INTEGER_PATTERN.matcher(value).matches()) {
                                try {
                                    getVariableManager().put(name, Integer.parseInt(value));
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            } else if (DECIMAL_PATTERN.matcher(value).matches()) {
                                try {
                                    getVariableManager().put(name, Double.parseDouble(value));
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            } else if (value.equals("true") || value.equals("false")) {
                                try {
                                    getVariableManager().put(name, Boolean.parseBoolean(value));
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            } else {
                                try {
                                    getVariableManager().put(name, value);
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            }

                            sender.sendMessage("&aVariable saved!");
                        }
                        return true;
                    } else if (args.length == 2) {
                        String name = args[1];
                        sender.sendMessage("&7Value of " + name + ": " + getVariableManager().get(name));

                        return true;
                    }
                } else if (args.length > 1 && args[0].equalsIgnoreCase("run")) {
                    String script = mergeArguments(args, 1, args.length - 1);

                    try {
                        Trigger trigger = getCmdManager().createTempCommandTrigger(script);

                        trigger.activate(createEmptyPlayerEvent(sender), new HashMap<>());

                    } catch (Exception e) {
                        handleException(sender, e);
                    }

                    return true;
                } else if (args.length > 2 && args[0].equalsIgnoreCase("sudo")) {
                    String playerName = args[1];
                    String script = mergeArguments(args, 2, args.length - 1);

                    IPlayer targetPlayer = getPlayer(playerName);
                    if (targetPlayer == null) {
                        sender.sendMessage("&cNo such player named &6" + playerName + "&c!");
                        return true;
                    }

                    try {
                        Trigger trigger = getCmdManager().createTempCommandTrigger(script);

                        trigger.activate(createEmptyPlayerEvent(targetPlayer), new HashMap<>());

                    } catch (Exception e) {
                        handleException(sender, e);
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
                            getScriptEditManager().startEdit(sender, "Inventory Trigger", "", new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        if (getInvManager().createTrigger(sizeCopy, name, script)) {
                                            sender.sendMessage("&aInventory Trigger created!");

                                            saveAsynchronously(getInvManager());
                                        } else {
                                            sender.sendMessage("&7Another Inventory Trigger with that name already exists");
                                        }
                                    } catch (Exception e) {
                                        handleException(sender, e);
                                    }
                                }
                            });
                        } else {
                            String script = mergeArguments(args, 4, args.length - 1);

                            try {
                                if (getInvManager().createTrigger(size, name, script)) {
                                    sender.sendMessage("&aInventory Trigger created!");

                                    saveAsynchronously(getInvManager());
                                } else {
                                    sender.sendMessage("&7Another Inventory Trigger with that name already exists");
                                }
                            } catch (Exception e) {
                                handleException(sender, e);
                            }
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        if (getInvManager().deleteTrigger(name)) {
                            sender.sendMessage("&aDeleted!");

                            saveAsynchronously(getInvManager());
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

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        if (index > trigger.getItems().length || index < 1) {
                            sender.sendMessage("&c" + "" + index + " is out of bounds. (Size: " + (trigger.getItems().length) + ")");
                            return true;
                        }

                        trigger.getItems()[index - 1] = IS;
                        saveAsynchronously(getInvManager());

                        sender.sendMessage("Successfully set item " + index);

                    } else if (args.length > 2 && args[2].equalsIgnoreCase("open")) {
                        String name = args[1];
                        IPlayer forWhom = null;
                        if (args.length == 3) {
                            forWhom = (IPlayer) sender;
                        } else {
                            IPlayer p = getPlayer(args[3]);
                            if (p != null)
                                forWhom = p;
                        }

                        if (forWhom == null) {
                            sender.sendMessage("&7Can't find that player.");
                            return true;
                        }

                        IInventory opened = getInvManager().openGUI(forWhom, name);
                        if (opened == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("edit")) {
                        String name = args[1];

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                try {
                                    trigger.setScript(script);
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }

                                sender.sendMessage("&aScript is updated!");

                                saveAsynchronously(getInvManager());
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

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
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

                        saveAsynchronously(getInvManager());
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

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
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

                        saveAsynchronously(getInvManager());
                        sender.sendMessage("Successfully filled column " + index);

                    } else if (args.length == 3 && args[2].equalsIgnoreCase("edititems")) {
                        String name = args[1];

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No such Inventory Trigger named " + name);
                            return true;
                        }

                        getInvEditManager().startEdit((IPlayer) sender, trigger);
                        return true;
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> create <size> [...]", "create a new inventory. <size> must be multiple of 9."
                                + " The <size> cannot be larger than 54");
                        sendDetails(sender, "/trg i MyInventory create 54");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> delete", "delete this inventory");
                        sendDetails(sender, "/trg i MyInventory delete");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> item <index>", "sets item of inventory to the held item. "
                                + "Clears the slot if you are holding nothing.");
                        sendDetails(sender, "/trg i MyInventory item 0");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> column <index>", "same as the item subcommand, but applied to an entire column."
                                + "Clears the slot if you are holding nothing.");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> row <index>", "same as the item subcommand, but applied to an entire row.");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> open", "Preview the inventory");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> open <player name>", "Send <player name> a preview of the inventory");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> edit", "Edit the inventory trigger.");
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
                        setItemTitle(IS, title);

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length > 3 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("add")) {
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if (IS == null) {
                            sender.sendMessage("&c" + "You are holding nothing.");
                            return true;
                        }

                        String lore = mergeArguments(args, 3, args.length - 1);
                        addItemLore(IS, lore);

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

                        if (!setLore(IS, index - 1, lore)) {
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

                        if (!removeLore(IS, index - 1)) {
                            sender.sendMessage("&7No lore at index " + index);
                            return true;
                        }

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] item title <item title>", "Change the title of holding item");
                        sendCommandDesc(sender, "/triggerreactor[trg] item lore add <line>", "Append lore to the holding item");
                        sendCommandDesc(sender, "/triggerreactor[trg] item lore set <index> <line>", "Replace lore at the specified index."
                                + "(Index start from 0)");
                        sendCommandDesc(sender, "/triggerreactor[trg] item lore remove <index>", "Delete lore at the specified index.");
                    }

                    return true;
                } else if (args.length > 0 && (args[0].equalsIgnoreCase("area") || args[0].equalsIgnoreCase("a"))) {
                    if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
                        boolean result = getSelectionManager().toggleSelection(((IPlayer) sender).getUniqueId());

                        sender.sendMessage("&7Area selection mode enabled: &6" + result);
                    } else if (args.length == 3 && args[2].equals("create")) {
                        String name = args[1];

                        // validate the name
                        if (!NAME_PATTERN.matcher(name).matches()) {
                            sender.sendMessage("&cThe name " + name + " has not allowed character!");
                            sender.sendMessage("&7Use only character, number, and underscore(_).");
                            return true;
                        }

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if (trigger != null) {
                            sender.sendMessage("&c" + "Area Trigger " + name + " is already exists!");
                            return true;
                        }

                        Area selected = getSelectionManager().getSelection(((IPlayer) sender).getUniqueId());
                        if (selected == null) {
                            sender.sendMessage("&7Invalid or incomplete area selection.");
                            return true;
                        }

                        Set<Area> conflicts = getAreaManager().getConflictingAreas(selected, selected::equals);
                        if (!conflicts.isEmpty()) {
                            sender.sendMessage("&7Found [" + conflicts.size() + "] conflicting areas:");
                            for (Area conflict : conflicts) {
                                sender.sendMessage("&d  " + conflict);
                            }
                            return true;
                        }

                        if (getAreaManager().createArea(name, selected.getSmallest(), selected.getLargest())) {
                            sender.sendMessage("&aCreated area trigger: " + name);

                            saveAsynchronously(getAreaManager());

                            getSelectionManager().resetSelections(((IPlayer) sender).getUniqueId());
                        } else {
                            sender.sendMessage("&7Area Trigger " + name + " already exists.");
                        }
                    } else if (args.length == 3 && args[2].equals("delete")) {
                        String name = args[1];

                        if (getAreaManager().deleteArea(name)) {
                            sender.sendMessage("&aArea Trigger deleted");

                            saveAsynchronously(getAreaManager());

                            getSelectionManager().resetSelections(((IPlayer) sender).getUniqueId());
                        } else {
                            sender.sendMessage("&7Area Trigger " + name + " does not exist.");
                        }
                    } else if (args.length > 2 && args[2].equals("enter")) {
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if (trigger.getEnterTrigger() != null) {
                            getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getEnterTrigger().getScript(), new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setEnterTrigger(script);

                                        saveAsynchronously(getAreaManager());

                                        sender.sendMessage("&aScript is updated!");
                                    } catch (Exception e) {
                                        handleException(sender, e);
                                    }
                                }
                            });
                        } else {
                            if (args.length == 3) {
                                getScriptEditManager().startEdit(sender, "Area Trigger [Enter]", "", new SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setEnterTrigger(script);

                                            saveAsynchronously(getAreaManager());
                                        } catch (Exception e) {
                                            handleException(sender, e);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    trigger.setEnterTrigger(mergeArguments(args, 3, args.length - 1));

                                    saveAsynchronously(getAreaManager());
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length > 2 && args[2].equals("exit")) {
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if (trigger.getExitTrigger() != null) {
                            getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getExitTrigger().getScript(), new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setExitTrigger(script);

                                        saveAsynchronously(getAreaManager());

                                        sender.sendMessage("&aScript is updated!");
                                    } catch (Exception e) {
                                        handleException(sender, e);
                                    }
                                }
                            });
                        } else {
                            if (args.length == 3) {
                                getScriptEditManager().startEdit(sender, "Area Trigger [Exit]", "", new SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            trigger.setExitTrigger(script);

                                            saveAsynchronously(getAreaManager());
                                        } catch (Exception e) {
                                            handleException(sender, e);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    trigger.setExitTrigger(mergeArguments(args, 3, args.length - 1));

                                    saveAsynchronously(getAreaManager());
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length == 3 && args[2].equals("sync")) {
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if (trigger == null) {
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        trigger.setSync(!trigger.isSync());

                        saveAsynchronously(getAreaManager());

                        sender.sendMessage("&7Sync mode: " + (trigger.isSync() ? "&a" : "&c") + trigger.isSync());
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] area[a] toggle", "Enable/Disable area selection mode.");
                        sendCommandDesc(sender, "/triggerreactor[trg] area[a] <name> create", "Create area trigger out of selected region.");
                        sendCommandDesc(sender, "/triggerreactor[trg] area[a] <name> delete", "Delete area trigger. BE CAREFUL!");
                        sendCommandDesc(sender, "/triggerreactor[trg] area[a] <name> enter [...]", "Enable/Disable area selection mode.");
                        sendDetails(sender, "/trg a TestingArea enter #MESSAGE \"Welcome\"");
                        sendCommandDesc(sender, "/triggerreactor[trg] area[a] <name> exit [...]", "Enable/Disable area selection mode.");
                        sendDetails(sender, "/trg a TestingArea exit #MESSAGE \"Bye\"");
                        sendCommandDesc(sender, "/triggerreactor[trg] area[a] <name> sync", "Enable/Disable sync mode.");
                        sendDetails(sender, "Setting it to true when you want to cancel event (with #CANCELEVENT)."
                                + " However, setting sync mode will make the trigger run on server thread; keep in mind that"
                                + " it can lag the server if you have too much things going on within the code."
                                + " Set it to false always if you are not sure.");
                    }
                    return true;
                } else if (args.length > 2 && args[0].equalsIgnoreCase("custom")) {
                    String eventName = args[1];
                    String name = args[2];

                    CustomTrigger trigger = getCustomManager().getTriggerForName(name);
                    if (trigger != null) {
                        getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler() {
                            @Override
                            public void onSave(String script) {
                                try {
                                    trigger.setScript(script);
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }

                                sender.sendMessage("&aScript is updated!");

                                saveAsynchronously(getCustomManager());
                            }
                        });
                    } else {
                        if (args.length == 3) {
                            getScriptEditManager().startEdit(sender,
                                    "Custom Trigger[" + eventName.substring(Math.max(0, eventName.length() - 10)) + "]", "",
                                    new SaveHandler() {
                                        @Override
                                        public void onSave(String script) {
                                            try {
                                                getCustomManager().createCustomTrigger(eventName, name, script);

                                                saveAsynchronously(getCustomManager());

                                                sender.sendMessage("&aCustom Trigger created!");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                sender.sendMessage("&c" + "Could not save! " + e.getMessage());
                                                sender.sendMessage("&c" + "See console for detailed messages.");
                                            }
                                        }
                                    });
                        } else {
                            String script = mergeArguments(args, 3, args.length - 1);

                            try {
                                getCustomManager().createCustomTrigger(eventName, name, script);

                                saveAsynchronously(getCustomManager());

                                sender.sendMessage("&aCustom Trigger created!");
                            } catch (ClassNotFoundException e2) {
                                sender.sendMessage("&c" + "Could not save! " + e2.getMessage());
                                sender.sendMessage("&c" + "Provided event name is not valid.");
                            } catch (Exception e) {
                                handleException(sender, e);
                            }
                        }
                    }
                    return true;
                } else if (args.length > 0 && (args[0].equalsIgnoreCase("repeat") || args[0].equalsIgnoreCase("r"))) {
                    if (args.length == 2) {
                        String name = args[1];

                        Trigger trigger = getRepeatManager().getTrigger(name);
                        if (trigger != null) {
                            getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setScript(script);
                                    } catch (Exception e) {
                                        handleException(sender, e);
                                    }

                                    sender.sendMessage("&aScript is updated!");

                                    saveAsynchronously(getRepeatManager());
                                }
                            });
                        } else {
                            this.getScriptEditManager().startEdit(sender, "Repeating Trigger", "", new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        getRepeatManager().createTrigger(name, script);
                                    } catch (Exception e) {
                                        handleException(sender, e);
                                    }

                                    saveAsynchronously(getRepeatManager());
                                }
                            });
                        }
                    } else if (args.length == 4 && args[2].equalsIgnoreCase("interval")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        String intervalValue = args[3];
                        long interval = TimeUtil.parseTime(intervalValue);

                        trigger.setInterval(interval);

                        saveAsynchronously(getRepeatManager());

                        sender.sendMessage("&aNow " +
                                "&6[" + name + "]" +
                                "&a will run every " +
                                "&6[" + TimeUtil.milliSecondsToString(interval) + "]");
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("autostart")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        trigger.setAutoStart(!trigger.isAutoStart());

                        saveAsynchronously(getRepeatManager());

                        sender.sendMessage("Auto start: " + (trigger.isAutoStart() ? "&a" : "&c") + trigger.isAutoStart());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("toggle")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        if (getRepeatManager().isRunning(name)) {
                            getRepeatManager().stopTrigger(name);
                            sender.sendMessage("&aScheduled stop. It may take some time depends on CPU usage.");
                        } else {
                            getRepeatManager().startTrigger(name);
                            sender.sendMessage("&aScheduled start up. It may take some time depends on CPU usage.");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("pause")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        trigger.setPaused(!trigger.isPaused());

                        sender.sendMessage("Paused: " + (trigger.isPaused() ? "&a" : "&c") + trigger.isPaused());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("status")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        getRepeatManager().showTriggerInfo(sender, trigger);
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if (trigger == null) {
                            sender.sendMessage("&7No Repeating Trigger with name " + name);
                            return true;
                        }

                        getRepeatManager().deleteTrigger(name);
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name>", "Create Repeating Trigger.");
                        sendDetails(sender, "&4Quick create is not supported.");
                        sendDetails(sender, "This creates a Repeating Trigger with default settings. You probably will want to change default values"
                                + " using other commands below. Also, creating Repeating Trigger doesn't start it automatically.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> interval <time format>", "Change the interval of this trigger.");
                        sendDetails(sender, "Notice the <time format> is not just a number but has specific format for it. For example, you first"
                                + " type what number you want to set and also define the unit of it. If you want it to repeat it every 1 hour, 20 minutes,"
                                + " 50seconds, and 10ticks, then it will be &6" + "/trg r BlahBlah interval 1h20m50s10t." + "&7 Currently only h, m,"
                                + " s, and t are supported for this format. Also notice that if you have two numbers with same format, they will add up as well. For example,"
                                + "&6 /trg r BlahBlah interval 30s40s" + "&7 will be added up to 70seconds total. All units other than"
                                + " h, m, s, or t will be ignored.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> autostart", "Enable/Disable automatic start for this trigger.");
                        sendDetails(sender, "By setting this to " + "&atrue" + "&7, this trigger will start on plugin enables itself. "
                                + "Otherwise, you have to start it yourself every time.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> toggle", "Start or stop the Repeating Trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> pause", "Pause or unpause the Repeating Trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> status", "See brief information about this trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> delete", "Delete repeating trigger.");
                    }

                    return true;
                } else if (args.length == 2 && (args[0].equalsIgnoreCase("synccustom") || args[0].equalsIgnoreCase("sync"))) {
                    String name = args[1];

                    CustomTrigger trigger = getCustomManager().getTriggerForName(name);
                    if (trigger == null) {
                        sender.sendMessage("&7No Custom Trigger found with that name.");
                        return true;
                    }

                    trigger.setSync(!trigger.isSync());

                    saveAsynchronously(getCustomManager());

                    sender.sendMessage("&7Sync mode: " + (trigger.isSync() ? "&a" : "&c") + trigger.isSync());
                    return true;
                } else if (args.length == 3 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del"))) {
                    String key = args[2];
                    switch (args[1]) {
                        case "vars":
                        case "variables":
                            getVariableManager().remove(key);
                            sender.sendMessage("&aRemoved the variable &6" + key);
                            break;
                        case "cmd":
                        case "command":
                            if (getCmdManager().removeCommandTrigger(key)) {
                                sender.sendMessage("&aRemoved the command trigger &6" + key);

                                saveAsynchronously(getCmdManager());
                            } else {
                                sender.sendMessage("&7Command trigger &6" + key + "&7 does not exist");
                            }
                            break;
                        case "custom":
                            if (getCustomManager().removeTriggerForName(key)) {
                                sender.sendMessage("&aRemoved the custom trigger &6" + key);

                                saveAsynchronously(getCustomManager());
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
                    showGlowStones(sender, getClickManager().getTriggersInChunk(scloc));
                    showGlowStones(sender, getWalkManager().getTriggersInChunk(scloc));
                    sender.sendMessage("&7Now trigger blocks will be shown as &6" + "glowstone");
                    return true;
                } else if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("- - - - - Result - - - - ");
                    for (Manager manager : Manager.getManagers()) {
                        if (!(manager instanceof AbstractTriggerManager<?>))
                            continue;

                        for (String val : ((AbstractTriggerManager<? extends Trigger>) manager).getTriggerList((name) -> {
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
                            File folder = new File(getDataFolder(), "timings");
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
                        sendCommandDesc(sender, "/triggerreactor[trg] timings toggle", "turn on/off timings analysis. Also analysis will be reset.");
                        sendCommandDesc(sender, "/triggerreactor[trg] timings reset", "turn on/off timings analysis. Also analysis will be reset.");
                        sendCommandDesc(sender, "/triggerreactor[trg] timings print", "Show analysis result.");
                        sendCommandDesc(sender, "/triggerreactor[trg] timings print xx", "Save analysis to file named xx.timings");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("saveall")) {
                    for (Manager manager : Manager.getManagers())
                        manager.saveAll();
                    sender.sendMessage("Save complete!");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    for (Manager manager : Manager.getManagers())
                        manager.reload();

                    getExecutorManager().reload();
                    getPlaceholderManager().reload();

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
                    AbstractInventoryEditManager manager = getInvEditManager();
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
		    names.add(trigger.getTriggerName());
		}
    	return names;
    }

    private static List<String> EMPTY = new ArrayList<String>();

    //only for /trg command
    public static List<String> onTabComplete(String[] args) {
        switch (args.length) {
            case 1:
                return filter(Arrays.asList("area", "click", "cmd", "command", "custom", "del", "delete", "help", "inventory", "item", "list",
                        "reload", "repeat", "run", "saveall", "search", "sudo", "synccustom", "timings", "variables", "version", "walk"), args[0]);
            case 2:
                switch (args[0].toLowerCase()) {
                    case "area":
                    case "a":
                        List<String> names = triggerNames(getInstance().getAreaManager());
                        // /trg area toggle
                        names.add("toggle");
                        return filter(names, args[1]);
                    case "cmd":
                    case "command":
                        return filter(triggerNames(getInstance().getCmdManager()), args[1]);
                    case "custom":
                        //event list
                        return filter(new ArrayList<String>(getInstance().getCustomManager().getAbbreviations()), args[1]);
                    case "delete":
                    case "del":
                        return filter(Arrays.asList("cmd", "command", "custom", "vars", "variables"), args[1]);
                    case "inventory":
                    case "i":
                        return filter(triggerNames(getInstance().getInvManager()), args[1]);
                    case "item":
                        return filter(Arrays.asList("lore", "title"), args[1]);
                    case "repeat":
                    case "r":
                        return filter(triggerNames(getInstance().getRepeatManager()), args[1]);
                    case "sudo":
                        return null; //player selection
                    case "synccustom":
                        return filter(triggerNames(getInstance().getCustomManager()), args[1]);
                    case "timings":
                        return filter(Arrays.asList("print", "toggle", "reset"), args[1]);
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
                        return filter(Arrays.asList("aliases", "permission", "sync"), args[2]);
                    case "custom":
                        return filter(triggerNames(getInstance().getCustomManager()), args[2]);
                    case "delete":
                    case "del":
                        AbstractTriggerManager manager;
                        switch (args[1]) {
                            case "cmd":
                            case "command":
                                manager = getInstance().getCmdManager();
                                break;
                            case "custom":
                                manager = getInstance().getCustomManager();
                                break;
                            //"vars" and "variables" also possible, but I won't be offering completions for these
                            default:
                                return EMPTY;
                        }
                        return filter(triggerNames(manager), args[2]);
                    case "inventory":
                    case "i":
                        return filter(Arrays.asList("column", "create", "delete", "edit", "edititems", "item", "open", "row"), args[2]);
                    case "item":
                        if (args[1].equals("lore")) {
                            return filter(Arrays.asList("add", "set", "remove"), args[2]);
                        }
                    case "repeat":
                    case "r":
                        return filter(Arrays.asList("autostart", "delete", "interval", "pause", "status", "toggle"), args[2]);
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

    protected abstract boolean removeLore(IItemStack iS, int index);

    protected abstract boolean setLore(IItemStack iS, int index, String lore);

    protected abstract void addItemLore(IItemStack iS, String lore);

    protected abstract void setItemTitle(IItemStack iS, String title);

    protected abstract IPlayer getPlayer(String string);

    protected abstract Object createEmptyPlayerEvent(ICommandSender sender);

    private void showHelp(ICommandSender sender) {
        showHelp(sender, 1);
    }

    private void showHelp(ICommandSender sender, int page) {
        page = Math.max(1, Math.min(helpPages.size(), page));

        sender.sendMessage("&7-----     &6" + getPluginDescription() + "&7    ----");
        helpPages.get(page - 1).sendParagraph(sender);
        sender.sendMessage("");
        sender.sendMessage("&d" + page + "&8/&4" + (helpPages.size()) + " &8- &6/trg help <page> &7to see other pages.");
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
    protected abstract void sendCommandDesc(ICommandSender sender, String command, String desc);

    /**
     * Send detail under the command. It is usually called after {@link #sendCommandDesc(ICommandSender, String, String)}
     * to add more information or example about the command.
     *
     * @param sender sender to show description
     * @param detail detail to show
     * @deprecated no longer used
     */
    @Deprecated
    protected abstract void sendDetails(ICommandSender sender, String detail);

    /**
     * get Plugin's description.
     *
     * @return returns the full name of the plugin and its version.
     */
    public abstract String getPluginDescription();

    /**
     * get Plugin's version as String
     *
     * @return version of the plugin as String.
     */
    public abstract String getVersion();

    /**
     * get Author of plugin
     *
     * @return author name of the plugin as String.
     */
    public abstract String getAuthor();

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

    public boolean isDebugging() {
        return debugging;
    } 
    
    /**
     * Show glowstones to indicate the walk/click triggers in the chunk. This should send block change packet
     * instead of changing the real block.
     *
     * @param sender sender to show the glow stones
     * @param set    the set contains location of block and its associated trigger.
     */
    protected abstract void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set);

    /**
     * Register events for Managers. If it was Bukkit API, we can assume that the 'manager' will implement Listener
     * interface, yet we need to verify it with instanceof to avoid any problems.
     *
     * @param manager the object instance of Manager
     */
    public abstract void registerEvents(Manager manager);

    /**
     * Get folder where the plugin files will be saved.
     *
     * @return folder to save plugin files.
     */
    public abstract File getDataFolder();

    /**
     * get Logger.
     *
     * @return Logger.
     */
    public abstract Logger getLogger();

    /**
     * Check if this plugin is enabled.
     *
     * @return true if enabled; false if disabled.
     */
    public abstract boolean isEnabled();

    /**
     * Disable this plugin.
     */
    public abstract void disablePlugin();

    /**
     * Get the main class instance. JavaPlugin for Bukkit API for example.
     *
     * @return
     */
    public abstract <T> T getMain();

    /**
     * Check if the 'key' is set in the config.yml. This might be only case for Bukkit API
     *
     * @param key the key
     * @return true if set; false if not set
     */
    public abstract boolean isConfigSet(String key);

    /**
     * Save the 'value' to the associated 'key' in config.yml. This might be only case for Bukkit API.
     * The new value should override the value if already exists.
     * This does not actually save values into config.yml unless you invoke {@link #saveConfig()}
     *
     * @param key   the key
     * @param value the value to set.
     */
    public abstract void setConfig(String key, Object value);

    /**
     * Get the saved value associated with 'key' in config.yml. This might be only case for Bukkit API.
     *
     * @param key the key
     * @return the value; null if not set.
     */
    public abstract Object getConfig(String key);

    /**
     * Get the saved value associated with 'key' in config.yml. This might be only case for Bukkit API.
     *
     * @param key the key
     * @param def the default value to return if the 'key' is not set
     * @return the value; null if not set.
     */
    public abstract <T> T getConfig(String key, T def);

    /**
     * Save all configs to config.yml.
     */
    public abstract void saveConfig();

    /**
     * Save all configs from config.yml.
     */
    public abstract void reloadConfig();

    /**
     * Run task on the server thread. Usually it happens via scheduler.
     *
     * @param runnable the Runnable to run
     */
    public abstract void runTask(Runnable runnable);

    /**
     * Call saveAll() on separated thread. It should also check if a saving task is already
     * happening with the 'manager.' (As it will cause concurrency issue without the proper check up)
     *
     * @param manager
     */
    public abstract void saveAsynchronously(Manager manager);

    /**
     * Handle the exception caused by Executors or Triggers. The 'e' is the context when the 'event' was
     * happened. For Bukkit API, it is child classes of Event. You may extract the player instance who is
     * related to this Exception and show useful information to the game.
     *
     * @param e         the context
     * @param throwable the exception that was thrown
     */
    final public void handleException(Object e, Throwable throwable) {
        if (isDebugging()) {
            throwable.printStackTrace();
        }

        ICommandSender sender = extractPlayerFromContext(e);
        if (sender == null)
            sender = getConsoleSender();

        sendExceptionMessage(sender, throwable);
    }

    /**
     * Handle the exception caused by Executors or Triggers.
     *
     * @param sender    the sender who will receive the message
     * @param throwable the exception that was thrown
     */
    final public void handleException(ICommandSender sender, Throwable throwable) {
        if (isDebugging()) {
            throwable.printStackTrace();
        }

        if (sender == null)
            sender = getConsoleSender();

        sendExceptionMessage(sender, throwable);
    }

    private void sendExceptionMessage(ICommandSender sender, Throwable e) {
        runTask(new Runnable() {
            @Override
            public void run() {
                Throwable ex = e;
                sender.sendMessage("&cCould not execute this trigger.");
                while (ex != null) {
                    sender.sendMessage("&c >> Caused by:");
                    sender.sendMessage("&c" + ex.getMessage());
                    ex = ex.getCause();
                }
                sender.sendMessage("&cIf you are administrator, see console for details.");
            }
        });
    }

    /**
     * get sender instance of the console
     *
     * @return
     */
    public abstract ICommandSender getConsoleSender();

    /**
     * Create ProcessInterrupter that will be used for the most of the Triggers. It is responsible for this
     * interrupter to handle
     * cooldowns, CALL executor, etc, that has to be processed during the iterpretation.
     *
     * @param e           the context
     * @param interpreter the interpreter
     * @param cooldowns   list of current cooldowns.
     * @return the interrupter created.
     */
    public abstract ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns);

    /**
     * Create ProcessInterrupter that will be used for the most of the Triggers. It is responsible for this
     * interrupter to handle
     * cooldowns, CALL executor, etc, that has to be processed during the interpretation.
     * This method exists specifically for Inventory Trigger. As Inventory Trigger should stop at some point when
     * the Inventory was closed, it is the iterrupter's responsibility to do that.
     *
     * @param e            the context
     * @param interpreter  the interpreter
     * @param cooldowns    list of current cooldowns.
     * @param inventoryMap the inventory map that contains all the information about open inventories. As child class that implements
     *                     IIventory should override hashCode() and equals() methods, you can assume that each IInventory instance represents one trigger
     *                     that is running with the InventoryTrigger mapped. So it is ideal to get inventory object from the 'e' context and see if the Inventory
     *                     object exists in the 'inventoryMap.' For the properly working InventoryTriggerManager, closing the inventory should delete the IInventory
     *                     from the 'inventoryMap,' so you can safely assume that closed inventory will not exists in the 'inventoryMap.'
     * @return
     */
    public abstract ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
                                                               Map<IInventory, InventoryTrigger> inventoryMap);

    /**
     * try to extract player from context 'e'.
     *
     * @param e Event for Bukkit API
     * @return
     */
    public abstract IPlayer extractPlayerFromContext(Object e);

    /**
     * Run Callable on the server thread.
     *
     * @param call the callable
     * @return the future object.
     */
    public abstract <T> Future<T> callSyncMethod(Callable<T> call);

    @Override
    public <T> Future<T> submitSync(Callable<T> call) {
        if (this.isServerThread()) {
            return new Future<T>() {
                private boolean done = false;

                @Override
                public boolean cancel(boolean arg0) {
                    return false;
                }

                @Override
                public T get() throws ExecutionException {
                    T out = null;
                    try {
                        out = call.call();
                        done = true;
                    } catch (Exception e) {
                        throw new ExecutionException(e);
                    }
                    return out;
                }

                @Override
                public T get(long arg0, TimeUnit arg1)
                        throws ExecutionException {
                    T out = null;
                    try {
                        out = call.call();
                        done = true;
                    } catch (Exception e) {
                        throw new ExecutionException(e);
                    }
                    return out;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return done;
                }

            };
        } else {
            return callSyncMethod(call);
        }
    }

    @Override
    public void submitAsync(Runnable run) {
        new Thread(run).start();
    }

    /**
     * Call event so that it can be heard by listeners
     *
     * @param event
     */
    public abstract void callEvent(IEvent event);

    /**
     * Check if the current Thread is the Server
     *
     * @return
     */
    public abstract boolean isServerThread();

    /**
     * extract useful custom variables manually from 'context'
     *
     * @param context
     * @return
     */
    public abstract Map<String, Object> getCustomVarsForTrigger(Object context);

    @SuppressWarnings("serial")
    private final List<Paragraph> helpPages = new ArrayList<Paragraph>() {{
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] walk[w] [...] &8- &7create a walk trigger.");
            sender.sendMessage("  &7/trg w #MESSAGE \"HEY YOU WALKED!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg w &7without extra parameters.");

            sender.sendMessage("&b/triggerreactor[trg] click[c] [...] &8- &7create a click trigger.");
            sender.sendMessage("  &7/trg c #MESSAGE \"HEY YOU CLICKED!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg c &7without extra parameters.");

            sender.sendMessage("&b/triggerreactor[trg] command[cmd] <command name> [...] &8- &7create a command trigger.");
            sender.sendMessage("  &7/trg cmd test #MESSAGE \"I'M test COMMAND!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg cmd <command name> &7without extra parameters.");
            sender.sendMessage("  &7To change sync/async mode, type &b/trg cmd <command name> sync&7.");
            sender.sendMessage("  &7- To set permissions for this command, type &b/trg cmd <command name> permission[p] x.y x.z y.y ...&7.");
            sender.sendMessage("  &7- To set aliases for this command, type &b/trg cmd <command name> aliases[a] some thing ...&7.");
            sender.sendMessage("    &6*&7Not providing any permission or aliases will remove them instead.");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] inventory[i] <inventory name> &8- &7Create an inventory trigger named <inventory name>");
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
            sender.sendMessage("&b/triggerreactor[trg] synccustom[sync] <name> &8- &7Toggle Sync/Async mode of custom trigger <name>");
            sender.sendMessage("  &7/trg synccustom Greet");

            sender.sendMessage("&b/triggerreactor[trg] variables[vars] [...] &8- &7set global variables.");
            sender.sendMessage("  &7&cWarning - This command will delete the previous data associated with the key if exists.");
            sender.sendMessage("  &7/trg vars Location test &8- &7save current location into global variable 'test'");
            sender.sendMessage("  &7/trg vars Item gifts.item1 &8- &7save hand held item into global variable 'test'");
            sender.sendMessage("  &7/trg vars test 13.5 &8- &7save 13.5 into global variable 'test'");

            sender.sendMessage("&b/triggerreactor[trg] variables[vars] <variable name> &8- &7get the value saved in <variable name>. null if nothing.");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] run [...] &8- &7Run simple script now without making a trigger.");
            sender.sendMessage("  &7/trg run #TP {\"MahPlace\"}");

            sender.sendMessage("&b/triggerreactor[trg] sudo <player> [...] &8- &7Run simple script now without making a trigger.");
            sender.sendMessage("  &7/trg sudo wysohn #TP {\"MahPlace\"}");

            sender.sendMessage("&b/triggerreactor[trg] delete[del] <type> <name> &8- &7Delete specific trigger/variable/etc.");
            sender.sendMessage("  &7/trg del vars test &8- &7delete the variable saved in 'test'");
            sender.sendMessage("  &7/trg del cmd test &8- &7delete the command trigger 'test'");
            sender.sendMessage("  &7/trg del custom Greet &8- &7delete the custom trigger 'Greet'");

            sender.sendMessage("&b/triggerreactor[trg] search &8- &7Show all trigger blocks in this chunk as glowing stones.");

            sender.sendMessage("&b/triggerreactor[trg] list [filter...] &8- &7List all triggers.");
            sender.sendMessage("  &7/trg list CommandTrigger some &8- &7Show results that contains 'CommandTrigger' and 'some'.");

            sender.sendMessage("&b/triggerreactor[trg] saveall &8- &7Save all scripts, variables, and settings.");

            sender.sendMessage("&b/triggerreactor[trg] reload &8- &7Reload all scripts, variables, and settings.");
        });
        add((sender -> {
            sender.sendMessage("&b/triggerreactor[trg] timings toggle &8- &7turn on/off timings analysis. Also analysis will be reset.");
            sender.sendMessage("&b/triggerreactor[trg] timings reset &8- &7turn on/off timings analysis. Also analysis will be reset.");
            sender.sendMessage("&b/triggerreactor[trg] timings print &8- &7Show analysis result.");
            sender.sendMessage("  &b/triggerreactor[trg] timings print xx &8- &7Save analysis to file named xx.timings");
        }));
    }};

//    private final List<Paragraph> deprecationPages = new ArrayList<Paragraph>(){{
//        add((sender -> {
//            sender.sendMessage("&d===============================================================");
//            sender.sendMessage("&6NOTICE: &cSyntax Change Planned!");
//            sender.sendMessage("");
//            sender.sendMessage("For version 3.0.0 and above, the Placholder now can be placed inside the 'string.'" +
//                    " For example, &6\"My name is $playername\" &fis equivalent to &6\"My name is \"+$playername&f." +
//                    " Therefore, you are hereby warned that the &6dollar sign($) &f used in the string will cause" +
//                    " the problem in future version. &cPlease fix it accordingly &fto avoid this problem." +
//                    " If you must use dollar sign, use escape sequence to do so;" +
//                    " for example, you can do so by &6\"The cost was 5\\$\"");
//            sender.sendMessage("&d===============================================================");
//        }));
//    }};

    private interface Paragraph {
        void sendParagraph(ICommandSender sender);
    }
}
