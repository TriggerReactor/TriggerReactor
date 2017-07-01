package io.github.wysohn.triggerreactor.core.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.VariableManager;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.util.LocationUtil;
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
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import io.github.wysohn.triggerreactor.tools.TimeUtil;

public abstract class TriggerReactor {
    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool(new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(){{this.setPriority(MIN_PRIORITY);}};
        }
    });

    private static TriggerReactor instance;

    public static TriggerReactor getInstance() {
        return instance;
    }

    protected AbstractExecutorManager executorManager;
    protected AbstractVariableManager variableManager;
    protected AbstractScriptEditManager scriptEditManager;
    protected AbstractPlayerLocationManager locationManager;
    protected AbstractPermissionManager permissionManager;
    protected AbstractAreaSelectionManager selectionManager;

    protected AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> clickManager;
    protected AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> walkManager;
    protected AbstractCommandTriggerManager cmdManager;
    protected AbstractInventoryTriggerManager invManager;
    protected AbstractAreaTriggerManager areaManager;
    protected AbstractCustomTriggerManager customManager;
    protected AbstractRepeatingTriggerManager repeatManager;

    protected AbstractNamedTriggerManager namedTriggerManager;

    protected TriggerReactor(){
        instance = this;
    }

    public AbstractExecutorManager getExecutorManager() {
        return executorManager;
    }

    public AbstractVariableManager getVariableManager() {
        return variableManager;
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

    public AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> getClickManager() {
        return clickManager;
    }

    public AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> getWalkManager() {
        return walkManager;
    }

    public AbstractCommandTriggerManager getCmdManager() {
        return cmdManager;
    }

    public AbstractInventoryTriggerManager getInvManager() {
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

    private static final String INTEGER_REGEX = "^[0-9]+$";
    private static final String DOUBLE_REGEX = "^[0-9]+.[0-9]{0,}$";

    private boolean debugging = false;
    public boolean onCommand(IPlayer sender, String command, String[] args){
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
                        scriptEditManager.startEdit(sender, "Click Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                if(clickManager.startLocationSet(sender, script)){
                                    sender.sendMessage(ChatColor.GRAY+"Now click the block to set click trigger.");
                                }else{
                                    sender.sendMessage(ChatColor.GRAY+"Already on progress.");
                                }
                            }
                        });
                    }else{
                        StringBuilder builder = new StringBuilder();
                        for(int i = 1; i < args.length; i++)
                            builder.append(args[i] + " ");
                        if(clickManager.startLocationSet(sender, builder.toString())){
                            sender.sendMessage(ChatColor.GRAY+"Now click the block to set click trigger.");
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Already on progress.");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("w")) {
                    if(args.length == 1){
                        scriptEditManager.startEdit(sender, "Walk Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                if (walkManager.startLocationSet(sender, script)) {
                                    sender.sendMessage(ChatColor.GRAY + "Now click the block to set walk trigger.");
                                } else {
                                    sender.sendMessage(ChatColor.GRAY + "Already on progress.");
                                }
                            }
                        });
                    }else{
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++)
                            builder.append(args[i] + " ");
                        if (walkManager.startLocationSet(sender, builder.toString())) {
                            sender.sendMessage(ChatColor.GRAY + "Now click the block to set walk trigger.");
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Already on progress.");
                        }
                    }
                    return true;
                } else if(args.length > 1 && (args[0].equalsIgnoreCase("command") || args[0].equalsIgnoreCase("cmd"))){
                    if(cmdManager.hasCommandTrigger(args[1])){
                        sender.sendMessage(ChatColor.GRAY + "This command is already binded!");
                    }else{
                        if(args.length == 2){
                            scriptEditManager.startEdit(sender, "Command Trigger", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    cmdManager.addCommandTrigger(sender, args[1], script);

                                    sender.sendMessage(ChatColor.GREEN+"Command trigger is binded!");

                                    saveAsynchronously(cmdManager);
                                }
                            });
                        }else{
                            StringBuilder builder = new StringBuilder();
                            for (int i = 2; i < args.length; i++)
                                builder.append(args[i] + " ");

                            cmdManager.addCommandTrigger(sender, args[1], builder.toString());

                            sender.sendMessage(ChatColor.GREEN+"Command trigger is binded!");

                            saveAsynchronously(cmdManager);
                        }
                    }
                    return true;
                } else if ((args[0].equalsIgnoreCase("variables") || args[0].equalsIgnoreCase("vars"))) {
                    if(args.length == 3){
                        if(args[1].equalsIgnoreCase("Item")){
                            String name = args[2];
                            if(!VariableManager.isValidName(name)){
                                sender.sendMessage(ChatColor.RED+name+" is not a valid key!");
                                return true;
                            }

                            PlayerInventory inv = sender.getInventory().get();
                            ItemStack IS = inv.getItemInMainHand();
                            if(IS == null || IS.getType() == Material.AIR){
                                sender.sendMessage(ChatColor.RED+"You are holding nothing on your main hand!");
                                return true;
                            }

                            variableManager.put(name, IS);

                            sender.sendMessage(ChatColor.GREEN+"Item saved!");
                        }else if(args[1].equalsIgnoreCase("Location")){
                            String name = args[2];
                            if(!VariableManager.isValidName(name)){
                                sender.sendMessage(ChatColor.RED+name+" is not a valid key!");
                                return true;
                            }

                            Player player = sender.get();
                            Location loc = player.getLocation();
                            variableManager.put(name, loc);

                            sender.sendMessage(ChatColor.GREEN+"Location saved!");
                        }else{
                            String name = args[1];
                            String value = args[2];

                            if(!VariableManager.isValidName(name)){
                                sender.sendMessage(ChatColor.RED+name+" is not a valid key!");
                                return true;
                            }

                            if(value.matches(INTEGER_REGEX)){
                                variableManager.put(name, Integer.parseInt(value));
                            }else if(value.matches(DOUBLE_REGEX)){
                                variableManager.put(name, Double.parseDouble(value));
                            }else if(value.equals("true") || value.equals("false")){
                                variableManager.put(name, Boolean.parseBoolean(value));
                            }else{
                                variableManager.put(name, value);
                            }

                            sender.sendMessage(ChatColor.GREEN+"Variable saved!");
                        }
                        return true;
                    }else if(args.length == 2){
                        String name = args[1];
                        sender.sendMessage(ChatColor.GRAY+"Value of "+name+": "+variableManager.get(name));

                        return true;
                    }
                } else if(args.length > 1 && args[0].equalsIgnoreCase("run")){
                    String script = mergeArguments(args, 1, args.length - 1);

                    try {
                        Trigger trigger = cmdManager.createTempCommandTrigger(script);

                        trigger.activate(new PlayerEvent(sender.get()){
                            @Override
                            public HandlerList getHandlers() {
                                return null;
                            }}, new HashMap<>());

                    } catch (IOException | LexerException | ParserException e) {
                        e.printStackTrace();
                    }

                    return true;
                } else if(args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("i")){
                    if(args.length > 3 && args[2].equalsIgnoreCase("create")){
                        String name = args[1];
                        int size = -1;
                        try{
                            size = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage(ChatColor.RED+""+size+" is not a valid number");
                            return true;
                        }

                        if(args.length == 4){
                            final int sizeCopy = size;
                            scriptEditManager.startEdit(sender, "Inventory Trigger", "", new SaveHandler() {
                                @Override
                                public void onSave(String script) {
                                    try {
                                        if(invManager.createTrigger(sizeCopy, name, script)){
                                            sender.sendMessage(ChatColor.GREEN+"Inventory Trigger created!");

                                            saveAsynchronously(invManager);
                                        }else{
                                            sender.sendMessage(ChatColor.GRAY+"Another Inventory Trigger with that name already exists");
                                        }
                                    } catch (IOException | LexerException | ParserException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }else{
                            String script = mergeArguments(args, 4, args.length - 1);

                            try {
                                if(invManager.createTrigger(size, name, script)){
                                    sender.sendMessage(ChatColor.GREEN+"Inventory Trigger created!");

                                    saveAsynchronously(invManager);
                                }else{
                                    sender.sendMessage(ChatColor.GRAY+"Another Inventory Trigger with that name already exists");
                                }
                            } catch (IOException | LexerException | ParserException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if(args.length == 3 && args[2].equalsIgnoreCase("delete")){
                        String name = args[1];

                        if(invManager.deleteTrigger(name)){
                            sender.sendMessage(ChatColor.GREEN+"Deleted!");

                            saveAsynchronously(invManager);
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"No such inventory trigger found.");
                        }
                    } else if(args.length == 4 && args[2].equals("item")){
                        PlayerInventory inv = sender.getInventory().get();
                        ItemStack IS = inv.getItemInMainHand();
                        IS = IS == null ? null : IS.clone();

                        String name = args[1];

                        int index = -1;
                        try{
                            index = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage(ChatColor.RED+""+index+" is not a valid number.");
                            return true;
                        }

                        InventoryTrigger trigger = invManager.getTriggerForName(name);
                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No such Inventory Trigger named "+name);
                            return true;
                        }

                        if(index > trigger.getItems().length - 1){
                            sender.sendMessage(ChatColor.RED+""+index+" is out of bound. (Size: "+trigger.getItems().length+")");
                            return true;
                        }

                        trigger.getItems()[index] = new BukkitItemStack(IS);

                        saveAsynchronously(invManager);
                    } else if(args.length > 2 && args[2].equalsIgnoreCase("open")){
                        String name = args[1];
                        IPlayer forWhom = null;
                        if(args.length == 3){
                            forWhom = sender;
                        }else{
                            Player p = Bukkit.getPlayer(args[3]);
                            if(p != null)
                                forWhom = new BukkitPlayer(p);
                        }

                        if(forWhom == null){
                            sender.sendMessage(ChatColor.GRAY+"Can't find that player.");
                            return true;
                        }

                        Inventory opened = invManager.openGUI(forWhom, name);
                        if(opened == null){
                            sender.sendMessage(ChatColor.GRAY+"No such Inventory Trigger named "+name);
                            return true;
                        }
                    } /*else if(args.length == 3 && args[2].equalsIgnoreCase("sync")){
                        String name = args[1];

                        InventoryTrigger trigger = invManager.getTriggerForName(name);
                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No such Inventory Trigger named "+name);
                            return true;
                        }

                        trigger.setSync(!trigger.isSync());

                        invManager.saveAll();

                        sender.sendMessage(ChatColor.GRAY+"Sync mode: "+(trigger.isSync() ? ChatColor.GREEN : ChatColor.RED)+trigger.isSync());
                    } */else {
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
                        //sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> sync", "Toggle sync/async mode.");
                    }
                    return true;
                } else if(args[0].equalsIgnoreCase("misc")){
                    if(args.length > 2 && args[1].equalsIgnoreCase("title")){
                        PlayerInventory inv = sender.getInventory().get();
                        ItemStack IS = inv.getItemInMainHand();
                        if(IS == null || IS.getType() == Material.AIR){
                            sender.sendMessage(ChatColor.RED+"You are holding nothing.");
                            return true;
                        }

                        String title = mergeArguments(args, 2, args.length - 1);
                        ItemMeta IM = IS.getItemMeta();
                        IM.setDisplayName(title);
                        IS.setItemMeta(IM);

                        inv.setItemInMainHand(IS);
                        return true;
                    }else if(args.length > 3 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("add")){
                        PlayerInventory inv = sender.getInventory().get();
                        ItemStack IS = inv.getItemInMainHand();
                        if(IS == null || IS.getType() == Material.AIR){
                            sender.sendMessage(ChatColor.RED+"You are holding nothing.");
                            return true;
                        }

                        String lore = mergeArguments(args, 3, args.length - 1);
                        ItemMeta IM = IS.getItemMeta();
                        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
                        lores.add(lore);
                        IM.setLore(lores);
                        IS.setItemMeta(IM);

                        inv.setItemInMainHand(IS);
                        return true;
                    }else if(args.length > 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("set")){
                        PlayerInventory inv = sender.getInventory().get();
                        ItemStack IS = inv.getItemInMainHand();
                        if(IS == null || IS.getType() == Material.AIR){
                            sender.sendMessage(ChatColor.RED+"You are holding nothing.");
                            return true;
                        }

                        int index = -1;
                        try{
                            index = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage(ChatColor.RED+""+index+" is not a valid number");
                            return true;
                        }

                        String lore = mergeArguments(args, 4, args.length - 1);
                        ItemMeta IM = IS.getItemMeta();
                        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
                        if(index > lores.size() - 1){
                            sender.sendMessage(ChatColor.RED+""+index+" is out of bound. (Lore size: "+lores.size()+")");
                            return true;
                        }

                        lores.set(index, lore);
                        IM.setLore(lores);
                        IS.setItemMeta(IM);

                       inv.setItemInMainHand(IS);
                        return true;
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("remove")){
                        PlayerInventory inv = sender.getInventory().get();
                        ItemStack IS = inv.getItemInMainHand();
                        if(IS == null || IS.getType() == Material.AIR){
                            sender.sendMessage(ChatColor.RED+"You are holding nothing.");
                            return true;
                        }

                        int index = -1;
                        try{
                            index = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage(ChatColor.RED+""+index+" is not a valid number");
                            return true;
                        }

                        ItemMeta IM = IS.getItemMeta();
                        List<String> lores = IM.getLore();
                        if(lores == null || index > lores.size() - 1 || index < 0){
                            sender.sendMessage(ChatColor.GRAY+"No lore at index "+index);
                            return true;
                        }

                        lores.remove(index);
                        IM.setLore(lores);
                        IS.setItemMeta(IM);

                        inv.setItemInMainHand(IS);
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
                        boolean result = selectionManager.toggleSelection(sender.getUniqueId());

                        sender.sendMessage(ChatColor.GRAY+"Area selection mode enabled: "+ChatColor.GOLD+result);
                    } else if (args.length == 3 && args[2].equals("create")){
                        String name = args[1];

                        AreaTrigger trigger = areaManager.getArea(name);
                        if(trigger != null){
                            sender.sendMessage(ChatColor.RED+"Area Trigger "+name+" is already exists!");
                            return true;
                        }

                        AreaTriggerManager.Area selected = selectionManager.getSelection(sender.getUniqueId());
                        if(selected == null){
                            sender.sendMessage(ChatColor.GRAY+"Invalid or incomplete area selection.");
                            return true;
                        }

                        Set<AreaTriggerManager.Area> conflicts = areaManager.getConflictingAreas(selected);
                        if(!conflicts.isEmpty()){
                            sender.sendMessage(ChatColor.GRAY+"Found ["+conflicts.size()+"] conflicting areas:");
                            for(AreaTriggerManager.Area conflict : conflicts){
                                sender.sendMessage(ChatColor.LIGHT_PURPLE+"  "+conflict);
                            }
                            return true;
                        }

                        if(areaManager.createArea(name, selected.getSmallest(), selected.getLargest())){
                            sender.sendMessage(ChatColor.GREEN+"Area Trigger has created!");

                            saveAsynchronously(areaManager);

                            selectionManager.resetSelections(sender.getUniqueId());
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Area Trigger "+name+" already exists.");
                        }
                    } else if (args.length == 3 && args[2].equals("delete")){
                        String name = args[1];

                        if(areaManager.deleteArea(name)){
                            sender.sendMessage(ChatColor.GREEN+"Area Trigger deleted");

                            saveAsynchronously(areaManager);

                            selectionManager.resetSelections(sender.getUniqueId());
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Area Trigger "+name+" does not exists.");
                        }
                    }else if (args.length > 2 && args[2].equals("enter")){
                        String name = args[1];

                        AreaTrigger trigger = areaManager.getArea(name);
                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Area Trigger found with that name.");
                            return true;
                        }

                        if(args.length == 3){
                            scriptEditManager.startEdit(sender, "Area Trigger [Enter]", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setEnterTrigger(script);

                                        saveAsynchronously(areaManager);
                                    } catch (IOException | LexerException | ParserException e) {
                                        e.printStackTrace();
                                        sender.sendMessage(ChatColor.RED+"Could not save!");
                                        sender.sendMessage(e.getMessage());
                                        sender.sendMessage(ChatColor.RED+"See console for more information.");
                                    }
                                }
                            });
                        }else{
                            try {
                                trigger.setEnterTrigger(mergeArguments(args, 3, args.length - 1));

                                saveAsynchronously(areaManager);
                            } catch (IOException | LexerException | ParserException e) {
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"Could not save!");
                                sender.sendMessage(e.getMessage());
                                sender.sendMessage(ChatColor.RED+"See console for more information.");
                            }
                        }
                    } else if (args.length > 2 && args[2].equals("exit")){
                        String name = args[1];

                        AreaTrigger trigger = areaManager.getArea(name);
                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Area Trigger found with that name.");
                            return true;
                        }

                        if(args.length == 3){
                            scriptEditManager.startEdit(sender, "Area Trigger [Exit]", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setExitTrigger(script);

                                        saveAsynchronously(areaManager);
                                    } catch (IOException | LexerException | ParserException e) {
                                        e.printStackTrace();
                                        sender.sendMessage(ChatColor.RED+"Could not save!");
                                        sender.sendMessage(e.getMessage());
                                        sender.sendMessage(ChatColor.RED+"See console for more information.");
                                    }
                                }
                            });
                        }else{
                            try {
                                trigger.setExitTrigger(mergeArguments(args, 3, args.length - 1));

                                saveAsynchronously(areaManager);
                            } catch (IOException | LexerException | ParserException e) {
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+"Could not save!");
                                sender.sendMessage(e.getMessage());
                                sender.sendMessage(ChatColor.RED+"See console for more information.");
                            }
                        }
                    } else if (args.length == 3 && args[2].equals("sync")){
                        String name = args[1];

                        AreaTrigger trigger = areaManager.getArea(name);
                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Area Trigger found with that name.");
                            return true;
                        }

                        trigger.setSync(!trigger.isSync());

                        saveAsynchronously(areaManager);

                        sender.sendMessage(ChatColor.GRAY+"Sync mode: "+(trigger.isSync() ? ChatColor.GREEN : ChatColor.RED)+trigger.isSync());
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

                    if(customManager.getTriggerForName(name) != null){
                        sender.sendMessage(ChatColor.GRAY+"No Area Trigger found with that name.");
                        return true;
                    }

                    if(args.length == 3){
                        scriptEditManager.startEdit(sender,
                                "Custom Trigger[" + eventName.substring(Math.max(0, eventName.length() - 10)) + "]", "",
                                new SaveHandler() {
                                    @Override
                                    public void onSave(String script) {
                                        try {
                                            customManager.createCustomTrigger(eventName, name, script);

                                            saveAsynchronously(customManager);

                                            sender.sendMessage(ChatColor.GREEN+"Custom Trigger created!");
                                        } catch (ClassNotFoundException | IOException | LexerException
                                                | ParserException e) {
                                            e.printStackTrace();
                                            sender.sendMessage(ChatColor.RED+"Could not save! "+e.getMessage());
                                            sender.sendMessage(ChatColor.RED+"See console for detailed messages.");
                                        }
                                    }
                                });
                    }else{
                        String script = mergeArguments(args, 3, args.length - 1);

                        try {
                            customManager.createCustomTrigger(eventName, name, script);

                            saveAsynchronously(customManager);

                            sender.sendMessage(ChatColor.GREEN+"Custom Trigger created!");
                        } catch (IOException | LexerException | ParserException e) {
                            e.printStackTrace();
                            sender.sendMessage(ChatColor.RED+"Could not save! "+e.getMessage());
                            sender.sendMessage(ChatColor.RED+"See console for detailed messages.");
                        } catch(ClassNotFoundException e2){
                            sender.sendMessage(ChatColor.RED+"Could not save! "+e2.getMessage());
                            sender.sendMessage(ChatColor.RED+"Provided event name is not valid.");
                        }
                    }
                    return true;
                }  else if(args.length > 0 && (args[0].equalsIgnoreCase("repeat") || args[0].equalsIgnoreCase("r"))){
                    if(args.length == 2){
                        String name = args[1];

                        if(repeatManager.getTrigger(name) != null){
                            sender.sendMessage(ChatColor.GRAY+"This named is already in use.");
                            return true;
                        }

                        this.scriptEditManager.startEdit(sender, "Repeating Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                try {
                                    repeatManager.createTrigger(name, script);
                                } catch (IOException | LexerException | ParserException e) {
                                    e.printStackTrace();
                                    sender.sendMessage(ChatColor.RED+"Could not save!");
                                    sender.sendMessage(e.getMessage());
                                    sender.sendMessage(ChatColor.RED+"See console for more information.");
                                }

                                saveAsynchronously(repeatManager);
                            }
                        });
                    } else if (args.length == 4 && args[2].equalsIgnoreCase("interval")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Repeating Trigger with name "+name);
                            return true;
                        }

                        String intervalValue = args[3];
                        long interval = TimeUtil.parseTime(intervalValue);

                        trigger.setInterval(interval);

                        saveAsynchronously(repeatManager);

                        sender.sendMessage(ChatColor.GREEN+"Now "+
                                ChatColor.GOLD+"["+name+"]"+
                                ChatColor.GREEN+" will run every "+
                                ChatColor.GOLD+"["+TimeUtil.milliSecondsToString(interval)+"]");
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("autostart")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Repeating Trigger with name "+name);
                            return true;
                        }

                        trigger.setAutoStart(!trigger.isAutoStart());

                        saveAsynchronously(repeatManager);

                        sender.sendMessage("Auto start: "+(trigger.isAutoStart() ? ChatColor.GREEN : ChatColor.RED)+trigger.isAutoStart());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("toggle")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Repeating Trigger with name "+name);
                            return true;
                        }

                        if(repeatManager.isRunning(name)){
                            repeatManager.stopTrigger(name);
                            sender.sendMessage(ChatColor.GREEN+"Scheduled stop. It may take some time depends on CPU usage.");
                        } else {
                            repeatManager.startTrigger(name);
                            sender.sendMessage(ChatColor.GREEN+"Scheduled start up. It may take some time depends on CPU usage.");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("pause")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Repeating Trigger with name "+name);
                            return true;
                        }

                        trigger.setPaused(!trigger.isPaused());

                        sender.sendMessage("Paused: "+(trigger.isPaused() ? ChatColor.GREEN : ChatColor.RED)+trigger.isPaused());
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("status")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Repeating Trigger with name "+name);
                            return true;
                        }

                        repeatManager.showTriggerInfo(sender, trigger);
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("delete")) {
                        String name = args[1];

                        RepeatingTrigger trigger = repeatManager.getTrigger(name);

                        if(trigger == null){
                            sender.sendMessage(ChatColor.GRAY+"No Repeating Trigger with name "+name);
                            return true;
                        }

                        repeatManager.deleteTrigger(name);
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name>", "Create Repeating Trigger.");
                        sendDetails(sender, ChatColor.DARK_RED+"Quick create is not supported.");
                        sendDetails(sender, "This creates a Repeating Trigger with default settings. You probably will want to change default values"
                                + " using other commands below. Also, creating Repeating Trigger doesn't start it automatically.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> interval <time format>", "Change the interval of this trigger.");
                        sendDetails(sender, "Notice the <time format> is not just a number but has specific format for it. For example, you first"
                                + " type what number you want to set and also define the unit of it. If you want it to repeat it every 1 hour, 20 minutes,"
                                + " and 50seconds, then it will be "+ChatColor.GOLD+"/trg r BlahBlah interval 1h20m50s."+ChatColor.GRAY+" Currently only h, m,"
                                + " and s are supported for this format. Also notice that if you have two numbers with same format, they will add up as well. For example,"
                                + ChatColor.GOLD+" /trg r BlahBlah interval 30s40s"+ChatColor.GRAY+" will be added up to 70seconds total. All units other than"
                                + " h, m, or s will be ignored.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> autostart", "Enable/Disable automatic start for this trigger.");
                        sendDetails(sender, "By setting this to "+ChatColor.GREEN+"true"+ChatColor.GRAY+", this trigger will start on plugin enables itself. "
                                + "Otherwise, you have to start it yourself every time.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> toggle", "Start or stop the Repeating Trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> pause", "Pause or unpause the Repeating Trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> status", "See brief information about this trigger.");
                        sendCommandDesc(sender, "/triggerreactor[trg] repeat[r] <name> delete", "Delete repeating trigger.");
                    }

                    return true;
                }  else if (args.length == 2 && (args[0].equalsIgnoreCase("synccustom") || args[0].equalsIgnoreCase("sync"))) {
                    String name = args[1];

                    CustomTrigger trigger = customManager.getTriggerForName(name);
                    if(trigger == null){
                        sender.sendMessage(ChatColor.GRAY+"No Custom Trigger found with that name.");
                        return true;
                    }

                    trigger.setSync(!trigger.isSync());

                    saveAsynchronously(customManager);

                    sender.sendMessage(ChatColor.GRAY+"Sync mode: "+(trigger.isSync() ? ChatColor.GREEN : ChatColor.RED)+trigger.isSync());
                    return true;
                } else if (args.length == 3 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del"))) {
                    String key = args[2];
                    switch (args[1]) {
                    case "vars":
                    case "variables":
                        variableManager.remove(key);
                        sender.sendMessage(ChatColor.GREEN+"Removed the variable "+ChatColor.GOLD+key);
                        break;
                    case "cmd":
                    case "command":
                        if(cmdManager.removeCommandTrigger(key)){
                            sender.sendMessage(ChatColor.GREEN+"Removed the command trigger "+ChatColor.GOLD+key);

                            saveAsynchronously(cmdManager);
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Command trigger "+ChatColor.GOLD+key+ChatColor.GRAY+" does not exist");
                        }
                        break;
                    case "custom":
                        if(customManager.removeTriggerForName(key)){
                            sender.sendMessage(ChatColor.GREEN+"Removed the custom trigger "+ChatColor.GOLD+key);

                            saveAsynchronously(customManager);
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Custom Trigger "+ChatColor.GOLD+key+ChatColor.GRAY+" does not exist");
                        }
                        break;
                    default:
                        sender.sendMessage("Ex) /trg del vars player.count");
                        sender.sendMessage("List: variables[vars], command[cmd], custom");
                        break;
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("search")) {
                    Player p = sender.get();
                    Chunk chunk = p.getLocation().getChunk();
                    showGlowStones(sender, clickManager.getTriggersInChunk(LocationUtil.convertToSimpleChunkLocation(chunk)));
                    showGlowStones(sender, walkManager.getTriggersInChunk(LocationUtil.convertToSimpleChunkLocation(chunk)));
                    sender.sendMessage(ChatColor.GRAY+"Now trigger blocks will be shown as "+ChatColor.GOLD+"glowstone");
                    return true;
                } else if(args[0].equalsIgnoreCase("saveall")){
                    for(Manager manager : Manager.getManagers())
                        manager.saveAll();
                    sender.sendMessage("Save complete!");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    for(Manager manager : Manager.getManagers())
                        manager.reload();

                    executorManager.reload();

                    sender.sendMessage("Reload Complete!");
                    return true;
                }
            }

            showHelp(sender);
        }

        return true;
    }

    private void showHelp(ICommandSender sender) {
        sender.sendMessage(ChatColor.GRAY+"-----     "+ChatColor.GOLD+getPluginDescription()+ChatColor.GRAY+"    ----");

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

    protected abstract void sendCommandDesc(ICommandSender sender, String command, String desc);

    protected abstract void sendDetails(ICommandSender sender, String detail);

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

    protected abstract void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set);

    public abstract void registerEvents(Manager manager, TriggerReactor plugin);

    public abstract File getDataFolder();

    public abstract Logger getLogger();

    public abstract boolean isEnabled();

    public abstract void disablePlugin();

    /**
     * Get the main class instance. JavaPlugin for Bukkit API for example.
     * @return
     */
    public abstract <T> T getMain();

    public abstract boolean isConfigSet(String key);

    public abstract void setConfig(String key, Object value);

    public abstract Object getConfig(String key);

    public abstract <T> T getConfig(String key, T def);

    public abstract void saveConfig();

    public abstract void reloadConfig();

    public abstract void runTask(Runnable runnable);

    public abstract void saveAsynchronously(Manager manager);

    public abstract void handleException(Object e, Throwable ex);

    public abstract ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns);

    public abstract ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
            Map<IInventory, InventoryTrigger> inventoryMap);

    public abstract UUID extractUUIDFromContext(Object e);

    public abstract <T> Future<T> callSyncMethod(TriggerReactor instance2, Callable<T> call);
}
