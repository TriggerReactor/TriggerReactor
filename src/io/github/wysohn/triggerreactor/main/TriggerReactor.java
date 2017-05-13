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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.wysohn.triggerreactor.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.manager.Manager;
import io.github.wysohn.triggerreactor.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.manager.TriggerConditionManager;
import io.github.wysohn.triggerreactor.manager.TriggerManager.Trigger;
import io.github.wysohn.triggerreactor.manager.VariableManager;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.manager.trigger.ClickTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.CommandTriggerManager;
import io.github.wysohn.triggerreactor.manager.trigger.WalkTriggerManager;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public class TriggerReactor extends JavaPlugin {
    private static TriggerReactor instance;
    public static TriggerReactor getInstance() {
        return instance;
    }

    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private ExecutorManager executorManager;
    private VariableManager variableManager;
    private ScriptEditManager scriptEditManager;
    private TriggerConditionManager conditionManager;

    private ClickTriggerManager clickManager;
    private WalkTriggerManager walkManager;
    private CommandTriggerManager cmdManager;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

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

        clickManager = new ClickTriggerManager(this);
        walkManager = new WalkTriggerManager(this);
        cmdManager = new CommandTriggerManager(this);

        new AutoSavingThread().start();
    }

    private class AutoSavingThread extends Thread{
        public AutoSavingThread(){
            this.setPriority(MIN_PRIORITY);
            this.setName("TriggerReactor Autosave Thread");
        }

        @Override
        public void run() {
            while(!Thread.interrupted() && instance.isEnabled()){
                for(Manager manager : Manager.getManagers())
                    manager.saveAll();

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initFailed(Exception e) {
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        this.setEnabled(false);
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

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("Finalizing the scheduled script executions...");
        cachedThreadPool.shutdown();
        getLogger().info("Shut down complete!");
    }

    private static final String INTEGER_REGEX = "^[0-9]+$";
    private static final String DOUBLE_REGEX = "^[0-9]+.[0-9]{0,}$";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("triggerreactor")){
            if(!sender.hasPermission("triggerreactor.admin"))
                return true;

            if(args.length > 0){
                if(args[0].equalsIgnoreCase("click") || args[0].equalsIgnoreCase("c")){
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
                }  else if (args.length == 3 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del"))) {
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
                } else if (args[0].equalsIgnoreCase("reload")) {
                    for(Manager manager : Manager.getManagers())
                        manager.reload();
                    sender.sendMessage("Reload Complete!");
                    return true;
                }
            }

            showHelp(sender);
        }

        return true;
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

        sendCommandDesc(sender, "/triggerreactor[trg] reload", "Reload all scripts, variables, and settings.");
    }

    private void sendCommandDesc(CommandSender sender, String command, String desc){
        sender.sendMessage(ChatColor.AQUA+command+" "+ChatColor.DARK_GRAY+"- "+ChatColor.GRAY+desc);
    }

    private void sendDetails(CommandSender sender, String detail){
        detail = ChatColor.translateAlternateColorCodes('&', detail);
        sender.sendMessage("  "+ChatColor.GRAY+detail);
    }

}
