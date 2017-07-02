package io.github.wysohn.triggerreactor.bukkit.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.IItemStack;
import io.github.wysohn.triggerreactor.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.manager.AreaSelectionManager;
import io.github.wysohn.triggerreactor.bukkit.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.bukkit.manager.PermissionManager;
import io.github.wysohn.triggerreactor.bukkit.manager.PlayerLocationManager;
import io.github.wysohn.triggerreactor.bukkit.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.bukkit.manager.VariableManager;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ClickTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.CommandTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.CustomTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.NamedTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class JavaPluginBridge extends TriggerReactor{
    private io.github.wysohn.triggerreactor.bukkit.main.TriggerReactor bukkitPlugin;

    private BungeeCordHelper bungeeHelper;

    private Thread bungeeConnectionThread;

    public void onEnable(io.github.wysohn.triggerreactor.bukkit.main.TriggerReactor plugin){
        Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());

        this.bukkitPlugin = plugin;

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
        locationManager = new PlayerLocationManager(this);
        permissionManager = new PermissionManager(this);
        selectionManager = new AreaSelectionManager(this);

        clickManager = new ClickTriggerManager(this);
        walkManager = new WalkTriggerManager(this);
        cmdManager = new CommandTriggerManager(this);
        invManager = new InventoryTriggerManager(this);
        areaManager = new AreaTriggerManager(this);
        customManager = new CustomTriggerManager(this);
        repeatManager = new RepeatingTriggerManager(this);

        namedTriggerManager = new NamedTriggerManager(this);

        bungeeHelper = new BungeeCordHelper();
        bungeeConnectionThread = new Thread(bungeeHelper);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();
    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        disablePlugin();
    }

    public void onDisable(JavaPlugin plugin){
        getLogger().info("Finalizing the scheduled script executions...");
        cachedThreadPool.shutdown();
        bungeeConnectionThread.interrupt();
        getLogger().info("Shut down complete!");
    }

    @Override
    protected void sendCommandDesc(ICommandSender sender, String command, String desc){
        sender.sendMessage(ChatColor.AQUA+command+" "+ChatColor.DARK_GRAY+"- "+ChatColor.GRAY+desc);
    }

    @Override
    protected void sendDetails(ICommandSender sender, String detail){
        detail = ChatColor.translateAlternateColorCodes('&', detail);
        sender.sendMessage("  "+ChatColor.GRAY+detail);
    }

    @Override
    protected String getPluginDescription() {
        return bukkitPlugin.getDescription().getFullName();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        for (Entry<SimpleLocation, Trigger> entry : set) {
            SimpleLocation sloc = entry.getKey();
            Player player = sender.get();
            player.sendBlockChange(
                    new Location(Bukkit.getWorld(sloc.getWorld()), sloc.getX(), sloc.getY(), sloc.getZ()),
                    Material.GLOWSTONE, (byte) 0);
        }
    }

    @Override
    public void registerEvents(Manager manager) {
        Bukkit.getPluginManager().registerEvents(manager, this.bukkitPlugin);
    }

    @Override
    public File getDataFolder() {
        return bukkitPlugin.getDataFolder();
    }

    @Override
    public Logger getLogger() {
        return bukkitPlugin.getLogger();
    }

    @Override
    public boolean isEnabled() {
        return bukkitPlugin.isEnabled();
    }

    @Override
    public <T> T getMain() {
        return (T) bukkitPlugin;
    }

    @Override
    public boolean isConfigSet(String key) {
        return bukkitPlugin.getConfig().isSet(key);
    }

    @Override
    public void setConfig(String key, Object value) {
        bukkitPlugin.getConfig().set(key, value);
    }

    @Override
    public Object getConfig(String key) {
        return bukkitPlugin.getConfig().get(key);
    }

    @Override
    public <T> T getConfig(String key, T def) {
        return (T) bukkitPlugin.getConfig().get(key, def);
    }

    @Override
    public void saveConfig() {
        bukkitPlugin.saveConfig();
    }

    @Override
    public void reloadConfig() {
        bukkitPlugin.reloadConfig();
    }

    @Override
    public void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(bukkitPlugin, runnable);
    }

    @Override
    public void saveAsynchronously(Manager manager) {
        bukkitPlugin.saveAsynchronously(manager);
    }

    @Override
    public void handleException(Object e, Throwable ex) {
        if(e instanceof PlayerEvent){
            Player player = ((PlayerEvent) e).getPlayer();
            player.sendMessage(ChatColor.RED+"Could not execute this trigger.");
            while(ex != null){
                player.sendMessage(ChatColor.RED+" >> Caused by:");
                player.sendMessage(ChatColor.RED+ex.getMessage());
                ex = ex.getCause();
            }
            player.sendMessage(ChatColor.RED+"If you are administrator, see console for details.");
        }
    }

    @Override
    public ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns) {
        return new ProcessInterrupter(){
            @Override
            public boolean onNodeProcess(Node node) {
                if(interpreter.isCooldown() && e instanceof PlayerEvent){
                    Player player = ((PlayerEvent) e).getPlayer();
                    UUID uuid = player.getUniqueId();
                    cooldowns.put(uuid, interpreter.getCooldownEnd());
                }
                return false;
            }

            @Override
            public boolean onCommand(Object context, String command, Object[] args) {
                if("CALL".equals(command)){
                    if(args.length < 1)
                        throw new RuntimeException("Need parameter [String]");

                    if(args[0] instanceof String){
                        Trigger trigger = getNamedTriggerManager().getTriggerForName((String) args[0]);
                        if(trigger == null)
                            throw new RuntimeException("No trigger found for Named Trigger "+args[0]);

                        trigger.activate(context, interpreter.getVars());
                        return true;
                    } else {
                        throw new RuntimeException("Parameter type not match; it should be a String."
                                + " Make sure to put double quotes, if you provided String literal.");
                    }
                } else if("CANCELEVENT".equals(command)) {
                    if(!interpreter.isSync())
                        throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                    if(context instanceof Cancellable){
                        ((Cancellable) context).setCancelled(true);
                        return true;
                    } else {
                        throw new RuntimeException(context+" is not a Cancellable event!");
                    }
                }

                return false;
            }

        };
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
            Map<IInventory, InventoryTrigger> inventoryMap) {
        return new ProcessInterrupter() {
            @Override
            public boolean onNodeProcess(Node node) {
                if (interpreter.isCooldown()) {
                    if(e instanceof InventoryInteractEvent){
                        HumanEntity he = ((InventoryInteractEvent) e).getWhoClicked();
                        if(he instanceof Player){
                            Player player = (Player) he;
                            UUID uuid = player.getUniqueId();
                            cooldowns.put(uuid, interpreter.getCooldownEnd());
                        }
                    }
                    return false;
                }

                //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                //  is still running after the inventory is closed.
                if(e instanceof InventoryOpenEvent
                        || e instanceof InventoryClickEvent){
                    Inventory inv = ((InventoryEvent) e).getInventory();

                    //it's not GUI so stop execution
                    if(!inventoryMap.containsKey(new BukkitInventory(inv)))
                        return true;
                }

                return false;
            }

            @Override
            public boolean onCommand(Object context, String command, Object[] args) {
                if("CALL".equals(command)){
                    if(args.length < 1)
                        throw new RuntimeException("Need parameter [String]");

                    if(args[0] instanceof String){
                        Trigger trigger = getNamedTriggerManager().getTriggerForName((String) args[0]);
                        if(trigger == null)
                            throw new RuntimeException("No trigger found for Named Trigger "+args[0]);

                        trigger.activate(e, interpreter.getVars());
                        return true;
                    } else {
                        throw new RuntimeException("Parameter type not match; it should be a String."
                                + " Make sure to put double quotes, if you provided String literal.");
                    }
                } else if("CANCELEVENT".equals(command)) {
                    if(!interpreter.isSync())
                        throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                    if(context instanceof Cancellable){
                        ((Cancellable) context).setCancelled(true);
                        return true;
                    } else {
                        throw new RuntimeException(context+" is not a Cancellable event!");
                    }
                }

                return false;
            }

        };
    }

    @Override
    public UUID extractUUIDFromContext(Object e) {
        if(e instanceof PlayerEvent){
            Player player = ((PlayerEvent) e).getPlayer();
            return player.getUniqueId();
        }else if(e instanceof InventoryInteractEvent){
            return ((InventoryInteractEvent) e).getWhoClicked().getUniqueId();
        }

        return null;
    }

    public class BungeeCordHelper implements PluginMessageListener, Runnable{
        private final String CHANNEL = "BungeeCord";

        private final String SUB_SERVERLIST = "ServerList";
        private final String SUB_USERCOUNT = "UserCount";

        private final Map<String, Integer> playerCounts = new ConcurrentHashMap<>();

        /**
         * constructor should only be called from onEnable()
         */
        private BungeeCordHelper() {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(bukkitPlugin, CHANNEL);
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(bukkitPlugin, CHANNEL, this);
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            if (!channel.equals(CHANNEL)) {
                return;
            }

            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();
            if (subchannel.equals(SUB_SERVERLIST)) {
                String[] serverList = in.readUTF().split(", ");
                Set<String> serverListSet = Sets.newHashSet(serverList);

                for(String server : serverListSet){
                    if(!playerCounts.containsKey(server))
                        playerCounts.put(server, -1);
                }

                Set<String> deleteServer = new HashSet<>();
                for(Entry<String, Integer> entry : playerCounts.entrySet()){
                    if(!serverListSet.contains(entry.getKey()))
                        deleteServer.add(entry.getKey());
                }

                for(String delete : deleteServer){
                    playerCounts.remove(delete);
                }
            } else if(subchannel.equals(SUB_USERCOUNT)){
                String server = in.readUTF(); // Name of server, as given in the arguments
                int playercount = in.readInt();

                playerCounts.put(server, playercount);
            }
        }

        public void sendToServer(Player player, String serverName){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(bukkitPlugin, CHANNEL, out.toByteArray());
        }

        public String[] getServerNames(){
            String[] servers = playerCounts.keySet().toArray(new String[playerCounts.size()]);
            return servers;
        }

        public int getPlayerCount(String serverName){
            return playerCounts.getOrDefault(serverName, -1);
        }

        @Override
        public void run(){
            while(!Thread.interrupted()){
                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                if(player == null)
                    return;

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(SUB_SERVERLIST);
                out.writeUTF("GetServers");
                player.sendPluginMessage(bukkitPlugin, SUB_SERVERLIST, out.toByteArray());

                if(!playerCounts.isEmpty()){
                    for(Entry<String, Integer> entry : playerCounts.entrySet()){
                        ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
                        out2.writeUTF(SUB_USERCOUNT);
                        out2.writeUTF("PlayerCount");
                        out2.writeUTF(entry.getKey());
                        player.sendPluginMessage(bukkitPlugin, SUB_USERCOUNT, out2.toByteArray());
                    }
                }

                try {
                    Thread.sleep(5 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        return Bukkit.getScheduler().callSyncMethod(bukkitPlugin, call);
    }

    @Override
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(bukkitPlugin);
    }

    @Override
    public void callEvent(IEvent event) {
        Bukkit.getPluginManager().callEvent(event.get());
    }

    @Override
    protected IPlayer getPlayer(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object createEmptyPlayerEvent(IPlayer sender) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void setItemTitle(IItemStack iS, String title) {
        ItemStack IS = iS.get();
        ItemMeta IM = IS.getItemMeta();
        IM.setDisplayName(title);
        IS.setItemMeta(IM);
    }

    @Override
    protected void addItemLore(IItemStack iS, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        lores.add(lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        if(lore == null || index < 0 || index > lores.size() - 1)
            return false;

        lores.set(index, lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);

        return true;
    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.getLore();
        if(lores == null || index < 0 || index > lores.size() - 1)
            return false;

        lores.remove(index);
        IM.setLore(lores);
        IS.setItemMeta(IM);

        return true;
    }
}
