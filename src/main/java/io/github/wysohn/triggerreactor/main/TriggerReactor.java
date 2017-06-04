/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.manager.Manager;
import io.github.wysohn.triggerreactor.manager.PlayerLocationManager;
import io.github.wysohn.triggerreactor.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.manager.TriggerConditionManager;
import io.github.wysohn.triggerreactor.manager.TriggerManager.Trigger;
import io.github.wysohn.triggerreactor.manager.VariableManager;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.AreaTriggerManager.AreaTrigger;
import io.github.wysohn.triggerreactor.manager.trigger.ClickTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.CommandTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.InventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.manager.trigger.NamedTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.WalkTriggerManager;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public class TriggerReactor extends JavaPlugin {
    private static TriggerReactor instance;
    public static TriggerReactor getInstance() {
        return instance;
    }

    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool(new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(){{this.setPriority(MIN_PRIORITY);}};
        }
    });

    private BungeeCordHelper bungeeHelper;

    private ExecutorManager executorManager;
    private VariableManager variableManager;
    private ScriptEditManager scriptEditManager;
    private TriggerConditionManager conditionManager;
    private PlayerLocationManager locationManager;

    private ClickTriggerManager clickManager;
    private WalkTriggerManager walkManager;
    private CommandTriggerManager cmdManager;
    private InventoryTriggerManager invManager;
    private AreaTriggerManager areaManager;

    private NamedTriggerManager namedTriggerManager;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        bungeeHelper = new BungeeCordHelper();

        try {
            executorManager = new ExecutorManager(this);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            variableManager = new VariableManager(this);
        } catch (IOException | InvalidConfigurationException e) {
            initFailed(e);
            return;
        }

        scriptEditManager = new ScriptEditManager(this);
        conditionManager = new TriggerConditionManager(this);
        locationManager = new PlayerLocationManager(this);

        clickManager = new ClickTriggerManager(this);
        walkManager = new WalkTriggerManager(this);
        cmdManager = new CommandTriggerManager(this);
        invManager = new InventoryTriggerManager(this);
        areaManager = new AreaTriggerManager(this);

        namedTriggerManager = new NamedTriggerManager(this);
    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        this.setEnabled(false);
    }

    public BungeeCordHelper getBungeeHelper() {
        return bungeeHelper;
    }

    public ExecutorManager getExecutorManager() {
        return executorManager;
    }

    public VariableManager getVariableManager() {
        return variableManager;
    }

    public ScriptEditManager getScriptEditManager() {
        return scriptEditManager;
    }

    public TriggerConditionManager getConditionManager() {
        return conditionManager;
    }

    public ClickTriggerManager getClickManager() {
        return clickManager;
    }

    public WalkTriggerManager getWalkManager() {
        return walkManager;
    }

    public InventoryTriggerManager getInvManager() {
        return invManager;
    }

    public AreaTriggerManager getAreaManager() {
        return areaManager;
    }

    public NamedTriggerManager getNamedTriggerManager() {
        return namedTriggerManager;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("Finalizing the scheduled script executions...");
        cachedThreadPool.shutdown();
        getLogger().info("Shut down complete!");
    }

    private static final String INTEGER_REGEX = "^[0-9]+$";
    private static final String DOUBLE_REGEX = "^[0-9]+.[0-9]{0,}$";

    private boolean debugging = false;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("triggerreactor")){
            if(!sender.hasPermission("triggerreactor.admin"))
                return true;

            if(args.length > 0){
                if(args[0].equalsIgnoreCase("debug")){
                    debugging = !debugging;

                    getLogger().info("Debugging is set to "+debugging);
                    return true;
                }else if(args[0].equalsIgnoreCase("click") || args[0].equalsIgnoreCase("c")){
                    if(args.length == 1){
                        scriptEditManager.startEdit((Conversable) sender, "Click Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                if(clickManager.startLocationSet((Player) sender, script)){
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
                        if(clickManager.startLocationSet((Player) sender, builder.toString())){
                            sender.sendMessage(ChatColor.GRAY+"Now click the block to set click trigger.");
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Already on progress.");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("w")) {
                    if(args.length == 1){
                        scriptEditManager.startEdit((Conversable) sender, "Walk Trigger", "", new SaveHandler(){
                            @Override
                            public void onSave(String script) {
                                if (walkManager.startLocationSet((Player) sender, script)) {
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
                        if (walkManager.startLocationSet((Player) sender, builder.toString())) {
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
                            scriptEditManager.startEdit((Conversable) sender, "Command Trigger", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    cmdManager.addCommandTrigger(sender, args[1], script);

                                    sender.sendMessage(ChatColor.GREEN+"Command trigger is binded!");
                                }
                            });
                        }else{
                            StringBuilder builder = new StringBuilder();
                            for (int i = 2; i < args.length; i++)
                                builder.append(args[i] + " ");

                            cmdManager.addCommandTrigger(sender, args[1], builder.toString());

                            sender.sendMessage(ChatColor.GREEN+"Command trigger is binded!");
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

                            ItemStack IS = ((Player) sender).getInventory().getItemInMainHand();
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

                            Location loc = ((Player) sender).getLocation();
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
                    }else{

                    }
                } else if(args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("i")){
                    if(args.length == 4 && args[2].equalsIgnoreCase("create")){
                        String name = args[1];
                        int size = -1;
                        try{
                            size = Integer.parseInt(args[3]);
                        }catch(NumberFormatException e){
                            sender.sendMessage(ChatColor.RED+""+size+" is not a valid number");
                            return true;
                        }

                        if(invManager.createTrigger(size, name)){
                            sender.sendMessage(ChatColor.GREEN+"Inventory Trigger created!");

                            invManager.saveAll();
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Another Inventory Trigger with that name already exists");
                        }
                    } else if(args.length == 3 && args[2].equalsIgnoreCase("delete")){
                        String name = args[1];

                        if(invManager.deleteTrigger(name)){
                            sender.sendMessage(ChatColor.GREEN+"Deleted!");

                            invManager.saveAll();
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"No such inventory trigger found.");
                        }
                    } else if(args.length == 4 && args[2].equals("item")){
                        ItemStack IS = ((Player) sender).getInventory().getItemInMainHand();
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

                        trigger.getItems()[index] = IS;

                        invManager.saveAll();
                    } else if(args.length > 3 && args[2].equalsIgnoreCase("slot")){
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

                        if(index > trigger.getSlots().length - 1){
                            sender.sendMessage(ChatColor.RED+""+index+" is out of bound. (Size: "+trigger.getSlots().length+")");
                            return true;
                        }

                        if(args.length == 4){
                            final int copyIndex = index;
                            scriptEditManager.startEdit((Conversable) sender, "Inventory Trigger Slot", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.getSlots()[copyIndex] = trigger.new InventorySlot(script);
                                        invManager.saveAll();
                                    } catch (IOException | LexerException | ParserException e) {
                                        e.printStackTrace();
                                        sender.sendMessage(ChatColor.RED+e.getMessage());
                                    }
                                }
                            });
                        }else{
                            StringBuilder builder = new StringBuilder();
                            for (int i = 4; i < args.length; i++)
                                builder.append(args[i] + " ");

                            try {
                                trigger.getSlots()[index] = trigger.new InventorySlot(builder.toString());
                                invManager.saveAll();
                            } catch (IOException | LexerException | ParserException e) {
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED+e.getMessage());
                            }
                        }
                    } else if(args.length == 4 && args[2].equalsIgnoreCase("delslot")){
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

                        if(index > trigger.getSlots().length - 1){
                            sender.sendMessage(ChatColor.RED+""+index+" is out of bound. (Size: "+trigger.getSlots().length+")");
                            return true;
                        }

                        trigger.getSlots()[index] = null;
                        invManager.saveAll();
                    } else if(args.length > 2 && args[2].equalsIgnoreCase("open")){
                        String name = args[1];
                        Player forWhom;
                        if(args.length == 3){
                            forWhom = (Player) sender;
                        }else{
                            forWhom = Bukkit.getPlayer(args[3]);
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
                    } else {
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> create <size>", "create a new inventory. <size> must be multiple of 9.");
                        sendDetails(sender, "/trg i MyInventory create 180");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> delete", "delete this inventory");
                        sendDetails(sender, "/trg i MyInventory delete");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> item <index>", "set item of inventory to the holding item. "
                                + "Clears the slot if you are holding nothing.");
                        sendDetails(sender, "/trg i MyInventory item 0");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> slot <index> [...]", "Set trigger for the specified slot <index>");
                        sendDetails(sender, "/trg i MyInventory slot 0 #MESSAGE \"Clicked!\"");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> slot <index>", "Set multiple lined trigger for the specified slot <index>");
                        sendDetails(sender, "/trg i MyInventory slot 0");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> delslot <index>", "Delete trigger at specified slot <index>. "
                                + "However, this does not delete the icon.");
                        sendDetails(sender, "/trg i MyInventory delslot 0");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> open", "Simply open GUI");
                        sendCommandDesc(sender, "/triggerreactor[trg] inventory[i] <inventory name> open <player name>", "Simply open GUI for <player name>");
                    }
                    return true;
                } else if(args[0].equalsIgnoreCase("misc")){
                    if(args.length > 2 && args[1].equalsIgnoreCase("title")){
                        ItemStack IS = ((Player) sender).getInventory().getItemInMainHand();
                        if(IS == null || IS.getType() == Material.AIR){
                            sender.sendMessage(ChatColor.RED+"You are holding nothing.");
                            return true;
                        }

                        String title = mergeArguments(args, 2, args.length - 1);
                        ItemMeta IM = IS.getItemMeta();
                        IM.setDisplayName(title);
                        IS.setItemMeta(IM);

                        ((Player) sender).getInventory().setItemInMainHand(IS);
                        return true;
                    }else if(args.length > 3 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("add")){
                        ItemStack IS = ((Player) sender).getInventory().getItemInMainHand();
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

                        ((Player) sender).getInventory().setItemInMainHand(IS);
                        return true;
                    }else if(args.length > 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("set")){
                        ItemStack IS = ((Player) sender).getInventory().getItemInMainHand();
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

                        ((Player) sender).getInventory().setItemInMainHand(IS);
                        return true;
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("lore") && args[2].equalsIgnoreCase("remove")){
                        ItemStack IS = ((Player) sender).getInventory().getItemInMainHand();
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

                        ((Player) sender).getInventory().setItemInMainHand(IS);
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
                        boolean result = areaManager.SELECTION_HELPER.toggleSelection((Player) sender);

                        sender.sendMessage(ChatColor.GRAY+"Area selection mode enabled: "+ChatColor.GOLD+result);
                    } else if (args.length == 3 && args[2].equals("create")){
                        String name = args[1];

                        AreaTriggerManager.Area selected = areaManager.SELECTION_HELPER.getSelection((Player) sender);
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
                            areaManager.saveAll();

                            areaManager.SELECTION_HELPER.resetSelections((Player) sender);
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Area Trigger "+name+" already exists.");
                        }
                    } else if (args.length == 3 && args[2].equals("delete")){
                        String name = args[1];

                        if(areaManager.deleteArea(name)){
                            sender.sendMessage(ChatColor.GREEN+"Area Trigger deleted");
                            areaManager.saveAll();

                            areaManager.SELECTION_HELPER.resetSelections((Player) sender);
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
                            scriptEditManager.startEdit((Conversable) sender, "Area Trigger [Enter]", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setEnterTrigger(script);
                                        areaManager.saveAll();
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
                                areaManager.saveAll();
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
                            scriptEditManager.startEdit((Conversable) sender, "Area Trigger [Exit]", "", new SaveHandler(){
                                @Override
                                public void onSave(String script) {
                                    try {
                                        trigger.setExitTrigger(script);
                                        areaManager.saveAll();
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
                                areaManager.saveAll();
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
                        areaManager.saveAll();

                        sender.sendMessage(ChatColor.GRAY+"Sync mode: "+(trigger.isSync() ? ChatColor.GREEN : ChatColor.GREEN)+trigger.isSync());
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
                        }else{
                            sender.sendMessage(ChatColor.GRAY+"Command trigger "+ChatColor.GOLD+key+ChatColor.GRAY+" does not exist");
                        }
                        break;
                    default:
                        sender.sendMessage("Ex) /trg del vars player.count");
                        sender.sendMessage("List: variables[vars], command[cmd]");
                        break;
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("search")) {
                    Chunk chunk = ((Player) sender).getLocation().getChunk();
                    showGlowStones(sender, clickManager.getTriggersInChunk(chunk));
                    showGlowStones(sender, walkManager.getTriggersInChunk(chunk));
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

    /**
     *
     * @param args
     * @param indexFrom inclusive
     * @param indexTo inclusive
     * @return
     */
    private String mergeArguments(String[] args, int indexFrom, int indexTo) {
        StringBuilder builder = new StringBuilder();
        for(int i = indexFrom; i <= indexTo; i++){
            builder.append(args[i]+" ");
        }
        return builder.toString();
    }

    private final Set<Class<? extends Manager>> savings = new HashSet<>();
    public boolean saveAsynchronously(final Manager manager){
        if(savings.contains(manager))
            return false;

        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    synchronized(savings){
                        savings.add(manager.getClass());
                    }

                    getLogger().info("Saving "+manager.getClass().getSimpleName());
                    manager.saveAll();
                    getLogger().info("Saving Done!");
                }catch(Exception e){
                    e.printStackTrace();
                    getLogger().warning("Failed to save "+manager.getClass().getSimpleName());
                }finally{
                    synchronized(savings){
                        savings.remove(manager.getClass());
                    }
                }
            }
        }){{this.setPriority(MIN_PRIORITY);}}.start();
        return true;
    }

    public boolean isDebugging() {
        return debugging;
    }


    @SuppressWarnings("deprecation")
    private void showGlowStones(CommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        for (Entry<SimpleLocation, Trigger> entry : set) {
            SimpleLocation sloc = entry.getKey();
            ((Player) sender).sendBlockChange(
                    new Location(Bukkit.getWorld(sloc.getWorld()), sloc.getX(), sloc.getY(), sloc.getZ()),
                    Material.GLOWSTONE, (byte) 0);
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY+"-----     "+ChatColor.GOLD+this.getDescription().getFullName()+ChatColor.GRAY+"    ----");

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

        sendCommandDesc(sender, "/triggerreactor[trg] misc", "Miscellaneous. Type it to see the list.");

        sendCommandDesc(sender, "/triggerreactor[trg] variables[vars] [...]", "set global variables.");
        sendDetails(sender, "&cWarning - This command will delete the previous data associated with the key if exists.");
        sendDetails(sender, "/trg vars Location test &8- &7save current location into global variable 'test'");
        sendDetails(sender, "/trg vars Item gifts.item1 &8- &7save hand held item into global variable 'test'");
        sendDetails(sender, "/trg vars test 13.5 &8- &7save 13.5 into global variable 'test'");

        sendCommandDesc(sender, "/triggerreactor[trg] variables[vars] <variable name>", "get the value saved in <variable name>. null if nothing.");

        sendCommandDesc(sender, "/triggerreactor[trg] delete[del] <type> <name>", "Delete specific trigger/variable/etc.");
        sendDetails(sender, "/trg del vars test &8- &7delete the variable saved in 'test'");
        sendDetails(sender, "/trg del cmd test &8- &7delete the command trigger 'test'");

        sendCommandDesc(sender, "/triggerreactor[trg] search", "Show all trigger blocks in this chunk as glowing stone.");

        sendCommandDesc(sender, "/triggerreactor[trg] saveall", "Save all scripts, variables, and settings.");

        sendCommandDesc(sender, "/triggerreactor[trg] reload", "Reload all scripts, variables, and settings.");
    }

    private void sendCommandDesc(CommandSender sender, String command, String desc){
        sender.sendMessage(ChatColor.AQUA+command+" "+ChatColor.DARK_GRAY+"- "+ChatColor.GRAY+desc);
    }

    private void sendDetails(CommandSender sender, String detail){
        detail = ChatColor.translateAlternateColorCodes('&', detail);
        sender.sendMessage("  "+ChatColor.GRAY+detail);
    }

    public class BungeeCordHelper implements PluginMessageListener {
        private final String CHANNEL = "BungeeCord";

        /**
         * constructor should only be called from onEnable()
         */
        private BungeeCordHelper() {
            getServer().getMessenger().registerOutgoingPluginChannel(TriggerReactor.this, CHANNEL);
            getServer().getMessenger().registerIncomingPluginChannel(TriggerReactor.this, CHANNEL, this);
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            if (!channel.equals("BungeeCord")) {
                return;
            }
/*            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();
            if (subchannel.equals("SomeSubChannel")) {
                // Use the code sample in the 'Response' sections below to read
                // the data.
            }*/
        }

        public void sendToServer(Player player, String serverName){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connecct");
            out.writeUTF(serverName);

            player.sendPluginMessage(TriggerReactor.this, CHANNEL, out.toByteArray());
        }
    }
}
