package io.github.wysohn.triggerreactor.core.main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import io.github.wysohn.triggerreactor.bukkit.manager.location.Area;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPermissionManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlayerLocationManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractRepeatingTriggerManager.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import io.github.wysohn.triggerreactor.tools.TimeUtil;

/**
 * The main abstract class of TriggerReactor. Interacting with any platform should extends this class to
 * create important internal components.
 * @author wysohn
 *
 */
public abstract class TriggerReactor {
    /**
     * Cached Pool for thread execution. It is used by {@link io.github.wysohn.triggerreactor.core.script.interpreter.Executor#runSyncTask(Runnable)}
     */
    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool(new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(){{this.setPriority(MIN_PRIORITY);}};
        }
    });

    private static TriggerReactor instance;

    /**
     * get instance of this class.
     * @return
     */
    public static TriggerReactor getInstance() {
        return instance;
    }

    protected TriggerReactor(){
        instance = this;
    }

    public abstract AbstractExecutorManager getExecutorManager();

    public abstract AbstractVariableManager getVariableManager();

    public abstract AbstractScriptEditManager getScriptEditManager();

    public abstract AbstractPlayerLocationManager getLocationManager();

    public abstract AbstractPermissionManager getPermissionManager();

    public abstract AbstractAreaSelectionManager getSelectionManager();

    public abstract AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> getClickManager();

    public abstract AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> getWalkManager();

    public abstract AbstractCommandTriggerManager getCmdManager();

    public abstract AbstractInventoryTriggerManager getInvManager();

    public abstract AbstractAreaTriggerManager getAreaManager();

    public abstract AbstractCustomTriggerManager getCustomManager();

    public abstract AbstractRepeatingTriggerManager getRepeatManager();

    public abstract AbstractNamedTriggerManager getNamedTriggerManager();

    private static final String INTEGER_REGEX = "^[0-9]+$";
    private static final String DOUBLE_REGEX = "^[0-9]+.[0-9]{0,}$";

    private boolean debugging = false;
    public boolean onCommand(ICommandSender sender, String command, String[] args){
        if(command.equalsIgnoreCase("triggerreactor")){
            if(!sender.hasPermission("triggerreactor.admin"))
                return true;

            if(args.length > 0){
                if(args[0].equalsIgnoreCase("debug")){
                    debugging = !debugging;

                    getLogger().info("Debugging is set to "+debugging);
                    return true;
                }else if(args[0].equalsIgnoreCase("click") || args[0].equalsIgnoreCase("c")){
                    if(args.length == 1){
                        getScriptEditManager().startEdit(sender, "Click Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                if(getClickManager().startLocationSet((IPlayer) sender, script)){
                                    sender.sendMessage("&7Now click the block to set click trigger.");
                                }else{
                                    sender.sendMessage("&7Already on progress.");
                                }
                            }
                        });
                    }else{
                        StringBuilder builder = new StringBuilder();
                        for(int i = 1; i < args.length; i++)
                            builder.append(args[i] + " ");
                        if(getClickManager().startLocationSet((IPlayer) sender, builder.toString())){
                            sender.sendMessage("&7Now click the block to set click trigger.");
                        }else{
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("w")) {
                    if(args.length == 1){
                        getScriptEditManager().startEdit(sender, "Walk Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                if (getWalkManager().startLocationSet((IPlayer) sender, script)) {
                                    sender.sendMessage("&7Now click the block to set walk trigger.");
                                } else {
                                    sender.sendMessage("&7Already on progress.");
                                }
                            }
                        });
                    }else{
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++)
                            builder.append(args[i] + " ");
                        if (getWalkManager().startLocationSet((IPlayer) sender, builder.toString())) {
                            sender.sendMessage("&7Now click the block to set walk trigger.");
                        } else {
                            sender.sendMessage("&7Already on progress.");
                        }
                    }
                    return true;
                } else if(args.length > 1 && (args[0].equalsIgnoreCase("command") || args[0].equalsIgnoreCase("cmd"))){
                    if(getCmdManager().hasCommandTrigger(args[1])){
                        Trigger trigger = getCmdManager().getCommandTrigger(args[1]);

                        getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler(){
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
                    }else{
                        if(args.length == 2){
                            getScriptEditManager().startEdit(sender, "Command Trigger", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    getCmdManager().addCommandTrigger(sender, args[1], script);

                                    sender.sendMessage("&aCommand trigger is binded!");

                                    saveAsynchronously(getCmdManager());
                                }
                            });
                        }else{
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
                    if(args.length == 3){
                        if(args[1].equalsIgnoreCase("Item")){
                            String name = args[2];
                            if(!getVariableManager().isValidName(name)){
                                sender.sendMessage("&c"+name+" is not a valid key!");
                                return true;
                            }

                            IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                            if(IS == null){
                                sender.sendMessage("&c"+"You are holding nothing on your main hand!");
                                return true;
                            }

                            try {
                                getVariableManager().put(name, IS.get());
                            } catch (Exception e) {
                                this.handleException(sender, e);
                            }

                            sender.sendMessage("&aItem saved!");
                        }else if(args[1].equalsIgnoreCase("Location")){
                            String name = args[2];
                            if(!getVariableManager().isValidName(name)){
                                sender.sendMessage("&c"+name+" is not a valid key!");
                                return true;
                            }

                            ILocation loc = ((IPlayer) sender).getLocation();
                            try {
                                getVariableManager().put(name, loc.get());
                            } catch (Exception e) {
                                this.handleException(sender, e);
                            }

                            sender.sendMessage("&aLocation saved!");
                        }else{
                            String name = args[1];
                            String value = args[2];

                            if(!getVariableManager().isValidName(name)){
                                sender.sendMessage("&c"+name+" is not a valid key!");
                                return true;
                            }

                            if(value.matches(INTEGER_REGEX)){
                                try {
                                    getVariableManager().put(name, Integer.parseInt(value));
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            }else if(value.matches(DOUBLE_REGEX)){
                                try {
                                    getVariableManager().put(name, Double.parseDouble(value));
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            }else if(value.equals("true") || value.equals("false")){
                                try {
                                    getVariableManager().put(name, Boolean.parseBoolean(value));
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            }else{
                                try {
                                    getVariableManager().put(name, value);
                                } catch (Exception e) {
                                    this.handleException(sender, e);
                                }
                            }

                            sender.sendMessage("&aVariable saved!");
                        }
                        return true;
                    }else if(args.length == 2){
                        String name = args[1];
                        sender.sendMessage("&7Value of "+name+": "+getVariableManager().get(name));

                        return true;
                    }
                } else if(args.length > 1 && args[0].equalsIgnoreCase("run")){
                    String script = mergeArguments(args, 1, args.length - 1);

                    try {
                        Trigger trigger = getCmdManager().createTempCommandTrigger(script);

                        trigger.activate(createEmptyPlayerEvent((IPlayer) sender), new HashMap<>());

                    } catch (Exception e) {
                        handleException(sender, e);
                    }

                    return true;
                } else if(args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("i")){
                    if(args.length > 3 && args[2].equalsIgnoreCase("create")){
                        String name = args[1];
                        int size = -1;
                        try{
                            size = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage("&c"+""+size+" is not a valid number");
                            return true;
                        }

                        if(args.length == 4){
                            final int sizeCopy = size;
                            getScriptEditManager().startEdit(sender, "Inventory Trigger", "", new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        if(getInvManager().createTrigger(sizeCopy, name, script)){
                                            sender.sendMessage("&aInventory Trigger created!");

                                            saveAsynchronously(getInvManager());
                                        }else{
                                            sender.sendMessage("&7Another Inventory Trigger with that name already exists");
                                        }
                                    } catch (Exception e) {
                                        handleException(sender, e);
                                    }
                                }
                            });
                        }else{
                            String script = mergeArguments(args, 4, args.length - 1);

                            try {
                                if(getInvManager().createTrigger(size, name, script)){
                                    sender.sendMessage("&aInventory Trigger created!");

                                    saveAsynchronously(getInvManager());
                                }else{
                                    sender.sendMessage("&7Another Inventory Trigger with that name already exists");
                                }
                            } catch (Exception e) {
                                handleException(sender, e);
                            }
                        }
                    } else if(args.length == 3 && args[2].equalsIgnoreCase("delete")){
                        String name = args[1];

                        if(getInvManager().deleteTrigger(name)){
                            sender.sendMessage("&aDeleted!");

                            saveAsynchronously(getInvManager());
                        }else{
                            sender.sendMessage("&7No such inventory trigger found.");
                        }
                    } else if(args.length == 4 && args[2].equals("item")){
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        IS = IS == null ? null : IS.clone();

                        String name = args[1];

                        int index = -1;
                        try{
                            index = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage("&c"+""+index+" is not a valid number.");
                            return true;
                        }

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
                        if(trigger == null){
                            sender.sendMessage("&7No such Inventory Trigger named "+name);
                            return true;
                        }

                        if(index > trigger.getItems().length - 1){
                            sender.sendMessage("&c"+""+index+" is out of bound. (Size: "+trigger.getItems().length+")");
                            return true;
                        }

                        trigger.getItems()[index] = IS;

                        saveAsynchronously(getInvManager());
                    } else if(args.length > 2 && args[2].equalsIgnoreCase("open")){
                        String name = args[1];
                        IPlayer forWhom = null;
                        if(args.length == 3){
                            forWhom = (IPlayer) sender;
                        }else{
                            IPlayer p = getPlayer(args[3]);
                            if(p != null)
                                forWhom = p;
                        }

                        if(forWhom == null){
                            sender.sendMessage("&7Can't find that player.");
                            return true;
                        }

                        IInventory opened = getInvManager().openGUI(forWhom, name);
                        if(opened == null){
                            sender.sendMessage("&7No such Inventory Trigger named "+name);
                            return true;
                        }
                    } else if(args.length == 3 && args[2].equalsIgnoreCase("edit")){
                        String name = args[1];

                        InventoryTrigger trigger = getInvManager().getTriggerForName(name);
                        if(trigger == null){
                            sender.sendMessage("&7No such Inventory Trigger named "+name);
                            return true;
                        }

                        getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler(){
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
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> create <size> [...]", "create a new inventory. <size> must be multiple of 9."
                                + " The <size> cannot be larger than 54");
                        sendDetails(sender, "/trg i MyInventory create 54");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> delete", "delete this inventory");
                        sendDetails(sender, "/trg i MyInventory delete");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> item <index>", "set item of inventory to the holding item. "
                                + "Clears the slot if you are holding nothing.");
                        sendDetails(sender, "/trg i MyInventory item 0");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> open", "Simply open GUI");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> open <player name>", "Simply open GUI for <player name>");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> edit", "Edit the inventory trigger.");
                    }
                    return true;
                } else if(args[0].equalsIgnoreCase("misc")){
                    if(args.length > 2 && args[1].equalsIgnoreCase("title")){
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if(IS == null){
                            sender.sendMessage("&c"+"You are holding nothing.");
                            return true;
                        }

                        String title = mergeArguments(args, 2, args.length - 1);
                        setItemTitle(IS, title);

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    }else if(args.length > 3 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("add")){
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if(IS == null){
                            sender.sendMessage("&c"+"You are holding nothing.");
                            return true;
                        }

                        String lore = mergeArguments(args, 3, args.length - 1);
                        addItemLore(IS, lore);

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    }else if(args.length > 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("set")){
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if(IS == null){
                            sender.sendMessage("&c"+"You are holding nothing.");
                            return true;
                        }

                        int index = -1;
                        try{
                            index = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage("&c"+""+index+" is not a valid number");
                            return true;
                        }

                        String lore = mergeArguments(args, 4, args.length - 1);

                        if(!setLore(IS, index, lore)){
                            sender.sendMessage("&c"+""+index+" is out of bound.");
                            return true;
                        }

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("remove")){
                        IItemStack IS = ((IPlayer) sender).getItemInMainHand();
                        if(IS == null){
                            sender.sendMessage("&c"+"You are holding nothing.");
                            return true;
                        }

                        int index = -1;
                        try{
                            index = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage("&c"+""+index+" is not a valid number");
                            return true;
                        }

                        if(!removeLore(IS, index)){
                            sender.sendMessage("&7No lore at index "+index);
                            return true;
                        }

                        ((IPlayer) sender).setItemInMainHand(IS);
                        return true;
                    } else{
                        sendCommandDesc(sender, "/triggerreactor[trg] misc title <item title>", "Change the title of holding item");
                        sendCommandDesc(sender, "/triggerreactor[trg] misc lore add <string>", "Append lore to the holding item");
                        sendCommandDesc(sender, "/triggerreactor[trg] misc lore set <index> <string>", "Replace lore at the specified index."
                                + "(Index start from 0)");
                        sendCommandDesc(sender, "/triggerreactor[trg] misc lore remove <index>", "Append lore to the holding item");
                    }

                    return true;
                } else if(args.length > 0 && (args[0].equalsIgnoreCase("area") || args[0].equalsIgnoreCase("a"))){
                    if(args.length == 2 && args[1].equalsIgnoreCase("toggle")){
                        boolean result = getSelectionManager().toggleSelection(((IPlayer) sender).getUniqueId());

                        sender.sendMessage("&7Area selection mode enabled: &6"+result);
                    } else if (args.length == 3 && args[2].equals("create")){
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if(trigger != null){
                            sender.sendMessage("&c"+"Area Trigger "+name+" is already exists!");
                            return true;
                        }

                        Area selected = getSelectionManager().getSelection(((IPlayer) sender).getUniqueId());
                        if(selected == null){
                            sender.sendMessage("&7Invalid or incomplete area selection.");
                            return true;
                        }

                        Set<Area> conflicts = getAreaManager().getConflictingAreas(selected);
                        if(!conflicts.isEmpty()){
                            sender.sendMessage("&7Found ["+conflicts.size()+"] conflicting areas:");
                            for(Area conflict : conflicts){
                                sender.sendMessage("&d  "+conflict);
                            }
                            return true;
                        }

                        if(getAreaManager().createArea(name, selected.getSmallest(), selected.getLargest())){
                            sender.sendMessage("&aArea Trigger has created!");

                            saveAsynchronously(getAreaManager());

                            getSelectionManager().resetSelections(((IPlayer) sender).getUniqueId());
                        }else{
                            sender.sendMessage("&7Area Trigger "+name+" already exists.");
                        }
                    } else if (args.length == 3 && args[2].equals("delete")){
                        String name = args[1];

                        if(getAreaManager().deleteArea(name)){
                            sender.sendMessage("&aArea Trigger deleted");

                            saveAsynchronously(getAreaManager());

                            getSelectionManager().resetSelections(((IPlayer) sender).getUniqueId());
                        }else{
                            sender.sendMessage("&7Area Trigger "+name+" does not exists.");
                        }
                    }else if (args.length > 2 && args[2].equals("enter")){
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if(trigger == null){
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if(trigger.getEnterTrigger() != null){
                            getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getEnterTrigger().getScript(), new SaveHandler(){
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
                            if(args.length == 3){
                                getScriptEditManager().startEdit(sender, "Area Trigger [Enter]", "", new SaveHandler(){
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
                            }else{
                                try {
                                    trigger.setEnterTrigger(mergeArguments(args, 3, args.length - 1));

                                    saveAsynchronously(getAreaManager());
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length > 2 && args[2].equals("exit")){
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if(trigger == null){
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        if(trigger.getExitTrigger() != null){
                            getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getExitTrigger().getScript(), new SaveHandler(){
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
                        }else{
                            if(args.length == 3){
                                getScriptEditManager().startEdit(sender, "Area Trigger [Exit]", "", new SaveHandler(){
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
                            }else{
                                try {
                                    trigger.setExitTrigger(mergeArguments(args, 3, args.length - 1));

                                    saveAsynchronously(getAreaManager());
                                } catch (Exception e) {
                                    handleException(sender, e);
                                }
                            }
                        }
                    } else if (args.length == 3 && args[2].equals("sync")){
                        String name = args[1];

                        AreaTrigger trigger = getAreaManager().getArea(name);
                        if(trigger == null){
                            sender.sendMessage("&7No Area Trigger found with that name.");
                            return true;
                        }

                        trigger.setSync(!trigger.isSync());

                        saveAsynchronously(getAreaManager());

                        sender.sendMessage("&7Sync mode: "+(trigger.isSync() ? "&a" : "&c")+trigger.isSync());
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
                    if(trigger != null){
                        getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler(){
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
                        if(args.length == 3){
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
                                                sender.sendMessage("&c"+"Could not save! "+e.getMessage());
                                                sender.sendMessage("&c"+"See console for detailed messages.");
                                            }
                                        }
                                    });
                        }else{
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
                }  else if(args.length > 0 && (args[0].equalsIgnoreCase("repeat") || args[0].equalsIgnoreCase("r"))){
                    if(args.length == 2){
                        String name = args[1];

                        Trigger trigger = getRepeatManager().getTrigger(name);
                        if(trigger != null){
                            getScriptEditManager().startEdit(sender, trigger.getTriggerName(), trigger.getScript(), new SaveHandler(){
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
                            this.getScriptEditManager().startEdit(sender, "Repeating Trigger", "", new SaveHandler(){
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

                        if(trigger == null){
                            sender.sendMessage("&7No Repeating Trigger with name "+name);
                            return true;
                        }

                        String intervalValue = args[3];
                        long interval = TimeUtil.parseTime(intervalValue);

                        trigger.setInterval(interval);

                        saveAsynchronously(getRepeatManager());

                        sender.sendMessage("&aNow "+
                                "&6["+name+"]"+
                                "&a will run every "+
                                "&6["+TimeUtil.milliSecondsToString(interval)+"]");
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("autostart")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage("&7No Repeating Trigger with name "+name);
                            return true;
                        }

                        trigger.setAutoStart(!trigger.isAutoStart());

                        saveAsynchronously(getRepeatManager());

                        sender.sendMessage("Auto start: "+(trigger.isAutoStart() ? "&a" : "&c")+trigger.isAutoStart());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("toggle")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage("&7No Repeating Trigger with name "+name);
                            return true;
                        }

                        if(getRepeatManager().isRunning(name)){
                            getRepeatManager().stopTrigger(name);
                            sender.sendMessage("&aScheduled stop. It may take some time depends on CPU usage.");
                        } else {
                            getRepeatManager().startTrigger(name);
                            sender.sendMessage("&aScheduled start up. It may take some time depends on CPU usage.");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("pause")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage("&7No Repeating Trigger with name "+name);
                            return true;
                        }

                        trigger.setPaused(!trigger.isPaused());

                        sender.sendMessage("Paused: "+(trigger.isPaused() ? "&a" : "&c")+trigger.isPaused());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("status")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage("&7No Repeating Trigger with name "+name);
                            return true;
                        }

                        getRepeatManager().showTriggerInfo(sender, trigger);
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        RepeatingTrigger trigger = getRepeatManager().getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage("&7No Repeating Trigger with name "+name);
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
                                + " and 50seconds, then it will be &6"+"/trg r BlahBlah interval 1h20m50s."+"&7 Currently only h, m,"
                                + " and s are supported for this format. Also notice that if you have two numbers with same format, they will add up as well. For example,"
                                + "&6 /trg r BlahBlah interval 30s40s"+"&7 will be added up to 70seconds total. All units other than"
                                + " h, m, or s will be ignored.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> autostart", "Enable/Disable automatic start for this trigger.");
                        sendDetails(sender, "By setting this to "+"&atrue"+"&7, this trigger will start on plugin enables itself. "
                                + "Otherwise, you have to start it yourself every time.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> toggle", "Start or stop the Repeating Trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> pause", "Pause or unpause the Repeating Trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> status", "See brief information about this trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> delete", "Delete repeating trigger.");
                    }

                    return true;
                }  else if (args.length == 2 && (args[0].equalsIgnoreCase("synccustom") || args[0].equalsIgnoreCase("sync"))) {
                    String name = args[1];

                    CustomTrigger trigger = getCustomManager().getTriggerForName(name);
                    if(trigger == null){
                        sender.sendMessage("&7No Custom Trigger found with that name.");
                        return true;
                    }

                    trigger.setSync(!trigger.isSync());

                    saveAsynchronously(getCustomManager());

                    sender.sendMessage("&7Sync mode: "+(trigger.isSync() ? "&a" : "&c")+trigger.isSync());
                    return true;
                } else if (args.length == 3 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del"))) {
                    String key = args[2];
                    switch (args[1]) {
                    case "vars":
                    case "variables":
                        getVariableManager().remove(key);
                        sender.sendMessage("&aRemoved the variable &6"+key);
                        break;
                    case "cmd":
                    case "command":
                        if(getCmdManager().removeCommandTrigger(key)){
                            sender.sendMessage("&aRemoved the command trigger &6"+key);

                            saveAsynchronously(getCmdManager());
                        }else{
                            sender.sendMessage("&7Command trigger &6"+key+"&7 does not exist");
                        }
                        break;
                    case "custom":
                        if(getCustomManager().removeTriggerForName(key)){
                            sender.sendMessage("&aRemoved the custom trigger &6"+key);

                            saveAsynchronously(getCustomManager());
                        }else{
                            sender.sendMessage("&7Custom Trigger &6"+key+"&7 does not exist");
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
                    sender.sendMessage("&7Now trigger blocks will be shown as &6"+"glowstone");
                    return true;
                } else if(args[0].equalsIgnoreCase("saveall")){
                    for(Manager manager : Manager.getManagers())
                        manager.saveAll();
                    sender.sendMessage("Save complete!");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    for(Manager manager : Manager.getManagers())
                        manager.reload();

                    getExecutorManager().reload();

                    sender.sendMessage("Reload Complete!");
                    return true;
                }
            }

            showHelp(sender);
        }

        return true;
    }

    protected abstract boolean removeLore(IItemStack iS, int index);

    protected abstract boolean setLore(IItemStack iS, int index, String lore);

    protected abstract void addItemLore(IItemStack iS, String lore);

    protected abstract void setItemTitle(IItemStack iS, String title);

    protected abstract IPlayer getPlayer(String string);

    protected abstract Object createEmptyPlayerEvent(IPlayer sender);

    private void showHelp(ICommandSender sender) {
        sender.sendMessage("&7-----     &6"+getPluginDescription()+"&7    ----");

        sendCommandDesc(sender, "/triggerreactor[trg] walk[w] [...]", "create a walk trigger.");
        sendDetails(sender, "/trg w #MESSAGE \"HEY YOU WALKED!\"");
        sendDetails(sender, "To create lines of script, simply type &b/trg w &7without extra parameters.");

        sendCommandDesc(sender, "/triggerreactor[trg] click[c] [...]", "create a click trigger.");
        sendDetails(sender, "/trg c #MESSAGE \"HEY YOU CLICKED!\"");
        sendDetails(sender, "To create lines of script, simply type &b/trg c &7without extra parameters.");

        sendCommandDesc(sender, "/triggerreactor[trg] command[cmd] <command name> [...]", "create a command trigger.");
        sendDetails(sender, "/trg cmd test #MESSAGE \"I'M test COMMAND!\"");
        sendDetails(sender, "To create lines of script, simply type &b/trg cmd <command name> &7without extra parameters.");

        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name>", "Create an inventory trigger named <inventory name>");
        sendDetails(sender, "/trg i to see more commands...");

        sendCommandDesc(sender, "/triggerreactor[trg] area[a]", "Create an area trigger.");
        sendDetails(sender, "/trg a to see more commands...");

        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r]", "Create an repeating trigger.");
        sendDetails(sender, "/trg r to see more commands...");

        sendCommandDesc(sender, "/triggerreactor[trg] custom <event> <name> [...]", "Create an area trigger.");
        sendDetails(sender, "/trg custom onJoin Greet #BROADCAST \"&aPlease welcome &6\"+player.getName()+\"&a!\"");
        sendCommandDesc(sender, "/triggerreactor[trg] synccustom[sync] <name>", "Toggle Sync/Async mode of custom trigger <name>");
        sendDetails(sender, "/trg synccustom Greet");

        sendCommandDesc(sender, "/triggerreactor[trg] misc", "Miscellaneous. Type it to see the list.");

        sendCommandDesc(sender, "/triggerreactor[trg] variables[vars] [...]", "set global variables.");
        sendDetails(sender, "&cWarning - This command will delete the previous data associated with the key if exists.");
        sendDetails(sender, "/trg vars Location test &8- &7save current location into global variable 'test'");
        sendDetails(sender, "/trg vars Item gifts.item1 &8- &7save hand held item into global variable 'test'");
        sendDetails(sender, "/trg vars test 13.5 &8- &7save 13.5 into global variable 'test'");

        sendCommandDesc(sender, "/triggerreactor[trg] variables[vars] <variable name>", "get the value saved in <variable name>. null if nothing.");

        sendCommandDesc(sender, "/triggerreactor[trg] run [...]", "Run simple code treating it as Command Trigger.");
        sendDetails(sender, "/trg run #TP {\"MahPlace\"}");

        sendCommandDesc(sender, "/triggerreactor[trg] delete[del] <type> <name>", "Delete specific trigger/variable/etc.");
        sendDetails(sender, "/trg del vars test &8- &7delete the variable saved in 'test'");
        sendDetails(sender, "/trg del cmd test &8- &7delete the command trigger 'test'");
        sendDetails(sender, "/trg del custom Greet &8- &7delete the command trigger 'test'");

        sendCommandDesc(sender, "/triggerreactor[trg] search", "Show all trigger blocks in this chunk as glowing stones.");

        sendCommandDesc(sender, "/triggerreactor[trg] saveall", "Save all scripts, variables, and settings.");

        sendCommandDesc(sender, "/triggerreactor[trg] reload", "Reload all scripts, variables, and settings.");
    }

    /**
     * Send command description.
     * @param sender sender to show description
     * @param command the command to explain
     * @param desc description
     */
    protected abstract void sendCommandDesc(ICommandSender sender, String command, String desc);

    /**
     * Send detail under the command. It is usually called after {@link #sendCommandDesc(ICommandSender, String, String)}
     * to add more information or example about the command.
     * @param sender sender to show description
     * @param detail detail to show
     */
    protected abstract void sendDetails(ICommandSender sender, String detail);

    /**
     * get Plugin's description.
     * @return returns the full name of the plugin and its version.
     */
    protected abstract String getPluginDescription();

    /**
    *
    * @param args
    * @param indexFrom inclusive
    * @param indexTo inclusive
    * @return
    */
   private String mergeArguments(String[] args, int indexFrom, int indexTo) {
       StringBuilder builder = new StringBuilder(args[indexFrom]);
       for(int i = indexFrom + 1; i <= indexTo; i++){
           builder.append(" "+args[i]);
       }
       return builder.toString();
   }

    public boolean isDebugging() {
        return debugging;
    }

    /**
     * Show glowstones to indicate the walk/click triggers in the chunk. This should send block change packet
     * instead of changing the real block.
     * @param sender sender to show the glow stones
     * @param set the set contains location of block and its associated trigger.
     */
    protected abstract void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set);

    /**
     * Register events for Managers. If it was Bukkit API, we can assume that the 'manager' will implement Listener
     * interface, yet we need to verify it with instanceof to avoid any problems.
     * @param manager the object instance of Manager
     */
    public abstract void registerEvents(Manager manager);

    /**
     * Get folder where the plugin files will be saved.
     * @return folder to save plugin files.
     */
    public abstract File getDataFolder();

    /**
     * get Logger.
     * @return Logger.
     */
    public abstract Logger getLogger();

    /**
     * Check if this plugin is enabled.
     * @return true if enabled; false if disabled.
     */
    public abstract boolean isEnabled();

    /**
     * Disable this plugin.
     */
    public abstract void disablePlugin();

    /**
     * Get the main class instance. JavaPlugin for Bukkit API for example.
     * @return
     */
    public abstract <T> T getMain();

    /**
     * Check if the 'key' is set in the config.yml. This might be only case for Bukkit API
     * @param key the key
     * @return true if set; false if not set
     */
    public abstract boolean isConfigSet(String key);

    /**
     * Save the 'value' to the associated 'key' in config.yml. This might be only case for Bukkit API.
     * The new value should override the value if already exists.
     * This does not actually save values into config.yml unless you invoke {@link #saveConfig()}
     * @param key the key
     * @param value the value to set.
     */
    public abstract void setConfig(String key, Object value);

    /**
     * Get the saved value associated with 'key' in config.yml. This might be only case for Bukkit API.
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
     * @param runnable the Runnable to run
     */
    public abstract void runTask(Runnable runnable);

    /**
     * Call saveAll() on separated thread. It should also check if a saving task is already
     * happening with the 'manager.' (As it will cause concurrency issue without the proper check up)
     * @param manager
     */
    public abstract void saveAsynchronously(Manager manager);

    /**
     * Handle the exception caused by Executors or Triggers. The 'e' is the context when the 'event' was
     * happened. For Bukkit API, it is child classes of Event. You may extract the player instance who is
     * related to this Exception and show useful information to the game.
     * @param e the context
     * @param ex the exception that was thrown
     */
    public abstract void handleException(Object e, Throwable ex);

    /**
     * Handle the exception caused by Executors or Triggers.
     * @param e the context
     * @param ex the exception that was thrown
     */
    public abstract void handleException(ICommandSender sender, Throwable ex);

    /**
     * Create ProcessInterrupter that will be used for the most of the Triggers. It is responsible for this
     * interrupter to handle
     * cooldowns, CALL executor, etc, that has to be processed during the iterpretation.
     * @param e the context
     * @param interpreter the interpreter
     * @param cooldowns list of current cooldowns.
     * @return the interrupter created.
     */
    public abstract ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns);

    /**
     * Create ProcessInterrupter that will be used for the most of the Triggers. It is responsible for this
     * interrupter to handle
     * cooldowns, CALL executor, etc, that has to be processed during the interpretation.
     * This method exists specifically for Inventory Trigger. As Inventory Trigger should stop at some point when
     * the Inventory was closed, it is the iterrupter's responsibility to do that.
     * @param e the context
     * @param interpreter the interpreter
     * @param cooldowns list of current cooldowns.
     * @param inventoryMap the inventory map that contains all the information about open inventories. As child class that implements
     *   IIventory should override hashCode() and equals() methods, you can assume that each IInventory instance represents one trigger
     *   that is running with the InventoryTrigger mapped. So it is ideal to get inventory object from the 'e' context and see if the Inventory
     *   object exists in the 'inventoryMap.' For the properly working InventoryTriggerManager, closing the inventory should delete the IInventory
     *   from the 'inventoryMap,' so you can safely assume that closed inventory will not exists in the 'inventoryMap.'
     * @return
     */
    public abstract ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
            Map<IInventory, InventoryTrigger> inventoryMap);

    /**
     * Try to extract UUID from the context 'e'. This is the UUID that will be used to check the cooldown for Triggers.
     * @param e the context
     * @return the UUID extracted. Can be null if the context doesn't have any UUID.
     */
    public abstract UUID extractUUIDFromContext(Object e);

    /**
     * Run Callable on the server thread.
     * @param call the callable
     * @return the future object.
     */
    public abstract <T> Future<T> callSyncMethod(Callable<T> call);

    /**
     * Call event so that it can be heard by listeners
     * @param event
     */
    public abstract void callEvent(IEvent event);

    /**
     * Check if the current Thread is the Server
     * @return
     */
    public abstract boolean isServerThread();
}
