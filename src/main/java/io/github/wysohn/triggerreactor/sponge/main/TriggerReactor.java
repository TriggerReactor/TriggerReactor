/*******************************************************************************
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
package io.github.wysohn.triggerreactor.sponge.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.script.ScriptException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPermissionManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlayerLocationManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeCommandSender;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.manager.AreaSelectionManager;
import io.github.wysohn.triggerreactor.sponge.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.sponge.manager.PlaceholderManager;
import io.github.wysohn.triggerreactor.sponge.manager.PlayerLocationManager;
import io.github.wysohn.triggerreactor.sponge.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.sponge.manager.VariableManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.ClickTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.CommandTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.CustomTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.WalkTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

@Plugin(id = TriggerReactor.ID, version = TriggerReactor.VERSION)
public class TriggerReactor extends io.github.wysohn.triggerreactor.core.main.TriggerReactor{
    protected static final String ID = "triggerreactor";
    protected static final String VERSION = "2.0.0";

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    private AbstractExecutorManager executorManager;
    private AbstractPlaceholderManager placeholderManager;
    private AbstractVariableManager variableManager;
    private AbstractScriptEditManager scriptEditManager;
    private AbstractPlayerLocationManager locationManager;
    private AbstractPermissionManager permissionManager;
    private AbstractAreaSelectionManager selectionManager;

    private AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> clickManager;
    private AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> walkManager;
    private AbstractCommandTriggerManager cmdManager;
    private AbstractInventoryTriggerManager invManager;
    private AbstractAreaTriggerManager areaManager;
    private AbstractCustomTriggerManager customManager;
    private AbstractRepeatingTriggerManager repeatManager;

    public TriggerReactor() {
        try {
            this.executorManager = new ExecutorManager(this);
            this.placeholderManager = new PlaceholderManager(this);
            this.variableManager = new VariableManager(this);
            this.scriptEditManager = new ScriptEditManager(this);
            this.locationManager = new PlayerLocationManager(this);
            //this.permissionManager = new PermissionManager(this);
            this.selectionManager = new AreaSelectionManager(this);

            this.clickManager = new ClickTriggerManager(this);
            this.walkManager = new WalkTriggerManager(this);
            this.cmdManager = new CommandTriggerManager(this);
            this.invManager = new InventoryTriggerManager(this);
            this.areaManager = new AreaTriggerManager(this);
            this.customManager = new CustomTriggerManager(this);
            this.repeatManager = new RepeatingTriggerManager(this);

        } catch (ScriptException | IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onInitialize(GameAboutToStartServerEvent e) {
        CommandSpec cs = CommandSpec.builder()
                .permission("triggerreactor.admin")
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("arguments")))
                .executor(new CommandExecutor() {

                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

                        return null;
                    }

                }).build();
        Sponge.getCommandManager().register(this, cs, "trg", "trigger");
    }

    @Listener
    public void onEnable(GameStartedServerEvent e) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists()){
            try{
                Asset asset = Sponge.getAssetManager().getAsset(this, "config.yml").get();
                String configStr = asset.readString();
                FileUtil.writeToFile(file, configStr);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

        for(Manager manager : Manager.getManagers()) {
            manager.reload();
        }

//        FileConfiguration config = plugin.getConfig();
//        if(config.getBoolean("Mysql.Enable", false)) {
//            try {
//                plugin.getLogger().info("Initializing Mysql support...");
//                mysqlHelper = new MysqlSupport(config.getString("Mysql.Address"),
//                        config.getString("Mysql.DbName"),
//                        "data",
//                        config.getString("Mysql.UserName"),
//                        config.getString("Mysql.Password"));
//                plugin.getLogger().info(mysqlHelper.toString());
//                plugin.getLogger().info("Done!");
//            } catch (SQLException e) {
//                e.printStackTrace();
//                plugin.getLogger().warning("Failed to initialize Mysql. Check for the error above.");
//            }
//        } else {
//            String path = "Mysql.Enable";
//            if(!config.isSet(path))
//                config.set(path, false);
//            path = "Mysql.Address";
//            if(!config.isSet(path))
//                config.set(path, "127.0.0.1:3306");
//            path = "Mysql.DbName";
//            if(!config.isSet(path))
//                config.set(path, "TriggerReactor");
//            path = "Mysql.UserName";
//            if(!config.isSet(path))
//                config.set(path, "root");
//            path = "Mysql.Password";
//            if(!config.isSet(path))
//                config.set(path, "1234");
//
//            plugin.saveConfig();
//        }
    }

    @Listener
    public void onDisable(GameStoppingServerEvent e) {
        getLogger().info("Finalizing the scheduled script executions...");
        cachedThreadPool.shutdown();
        getLogger().info("Shut down complete!");
    }

    public boolean onCommand(CommandSource sender, CommandContext command) {
        if(sender instanceof Player){
            return this.onCommand(
                    new SpongePlayer((Player) sender),
                    "triggerreactor",
                    command.<String>getOne("arguments").get().split(" "));
        }else{
            return this.onCommand(
                    new SpongeCommandSender(sender),
                    "triggerreactor",
                    command.<String>getOne("arguments").get().split(" "));
        }
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        for(Manager manager : Manager.getManagers())
            manager.reload();

        getExecutorManager().reload();
        getPlaceholderManager().reload();
    }

    @Override
    public AbstractExecutorManager getExecutorManager() {
        return executorManager;
    }

    @Override
    public AbstractPlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public AbstractVariableManager getVariableManager() {
        return variableManager;
    }

    @Override
    public AbstractScriptEditManager getScriptEditManager() {
        return scriptEditManager;
    }

    @Override
    public AbstractPlayerLocationManager getLocationManager() {
        return locationManager;
    }

    @Override
    public AbstractPermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public AbstractAreaSelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> getClickManager() {
        return clickManager;
    }

    @Override
    public AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> getWalkManager() {
        return walkManager;
    }

    @Override
    public AbstractCommandTriggerManager getCmdManager() {
        return cmdManager;
    }

    @Override
    public AbstractInventoryTriggerManager getInvManager() {
        return invManager;
    }

    @Override
    public AbstractAreaTriggerManager getAreaManager() {
        return areaManager;
    }

    @Override
    public AbstractCustomTriggerManager getCustomManager() {
        return customManager;
    }

    @Override
    public AbstractRepeatingTriggerManager getRepeatManager() {
        return repeatManager;
    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        ItemStack IS = iS.get();
        List<Text> lores = IS.get(Keys.ITEM_LORE).get();
        if(lores != null) {
            lores.remove(index);
            IS.offer(Keys.ITEM_LORE, lores);
            return true;
        }

        return false;
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        ItemStack IS = iS.get();
        List<Text> lores = IS.get(Keys.ITEM_LORE).get();
        if(lores != null) {
            if(lores.size() > 0 && index >= 0 && index < lores.size()) {
                lores.set(index, Text.of(lore));
                IS.offer(Keys.ITEM_LORE, lores);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void addItemLore(IItemStack iS, String lore) {
        ItemStack IS = iS.get();
        List<Text> lores = IS.get(Keys.ITEM_LORE).get();
        if(lores == null)
            lores = new ArrayList<Text>();
        lores.add(Text.of(lore));
        IS.offer(Keys.ITEM_LORE, lores);
    }

    @Override
    protected void setItemTitle(IItemStack iS, String title) {
        ItemStack IS = iS.get();
        IS.offer(Keys.DISPLAY_NAME, Text.of(title));
    }

    @Override
    protected IPlayer getPlayer(String string) {
        Player player = Sponge.getServer().getPlayer(string).get();
        if(player == null)
            return null;

        return new SpongePlayer(player);
    }

    @Override
    protected Object createEmptyPlayerEvent(ICommandSender sender) {
        Object unwrapped = sender.get();

        if(unwrapped instanceof Player) {
            return new Event(){

                @Override
                public Cause getCause() {
                    Player src = (Player) unwrapped;
                    EventContext context = EventContext.builder().add(EventContextKeys.PLAYER, src).build();
                    return Cause.builder().append(unwrapped).build(context);
                }
            };
        }else if(unwrapped instanceof ConsoleSource) {
            return new Event(){

                @Override
                public Cause getCause() {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(unwrapped);
                        return frame.getCurrentCause();
                    }
                }
            };
        }else{
            throw new RuntimeException("Cannot create empty PlayerEvent for "+sender);
        }
    }

    @Override
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        sender.sendMessage(String.format("&b%s &8- &7%s", command, desc));
    }

    @Override
    protected void sendDetails(ICommandSender sender, String detail) {
        sender.sendMessage(String.format("  &7%s", detail));
    }

    @Override
    protected String getPluginDescription() {
        return ID + " v"+VERSION;
    }

    @Override
    protected void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        CommandSource source = sender.get();
        if(source instanceof Player) {
            Player player = (Player) source;

            for(Entry<SimpleLocation, Trigger> entry : set) {
                SimpleLocation sloc = entry.getKey();
                player.sendBlockChange(sloc.getX(), sloc.getY(), sloc.getZ(), BlockTypes.GLOWSTONE.getDefaultState());
            }
        }
    }

    @Override
    public void registerEvents(Manager manager) {
        Sponge.getEventManager().registerListeners(this, manager);
    }

    @Override
    public File getDataFolder() {
        return this.privateConfigDir.toFile();
    }

    @Override
    public Logger getLogger() {
        return getLogger();
    }

    @Override
    public boolean isEnabled() {
        return true; //Sponge doesn't disable plugins at runtime
    }

    @Override
    public void disablePlugin() {
      //Sponge doesn't disable plugins at runtime
    }

    @Override
    public <T> T getMain() {
        return (T) this;
    }

    @Override
    public boolean isConfigSet(String key) {
        // TODO later
        return false;
    }

    @Override
    public void setConfig(String key, Object value) {
        // TODO later
    }

    @Override
    public Object getConfig(String key) {
        // TODO later
        return null;
    }

    @Override
    public <T> T getConfig(String key, T def) {
        // TODO later
        return null;
    }

    @Override
    public void saveConfig() {
        // TODO later
    }

    @Override
    public void reloadConfig() {
        // TODO later
    }

    @Override
    public void runTask(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable);
    }

    private final Set<Class<? extends Manager>> savings = new HashSet<>();

    @Override
    public void saveAsynchronously(Manager manager) {
        if(savings.contains(manager))
            return;

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
    }

    @Override
    public void handleException(Object e, Throwable ex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleException(ICommandSender sender, Throwable ex) {
        // TODO Auto-generated method stub

    }

    @Override
    public ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
            Map<IInventory, InventoryTrigger> inventoryMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID extractUUIDFromContext(Object e) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void callEvent(IEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isServerThread() {
        boolean result = false;

        synchronized(this){
            result = Sponge.getServer().isMainThread();
        }

        return result;
    }

    @Override
    public Map<String, AbstractAPISupport> getSharedVars() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractNamedTriggerManager getNamedTriggerManager() {
        // TODO Auto-generated method stub
        return null;
    }
}
