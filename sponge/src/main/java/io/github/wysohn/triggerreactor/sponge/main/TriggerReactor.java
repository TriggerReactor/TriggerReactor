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

import com.google.inject.Inject;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWrapper;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeCommandSender;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeWrapper;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.manager.*;
import io.github.wysohn.triggerreactor.sponge.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.sponge.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.*;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.sponge.tools.DelegatedPlayer;
import io.github.wysohn.triggerreactor.tools.Lag;
import org.bstats.sponge.MetricsLite2;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Plugin(id = TriggerReactor.ID)
public class TriggerReactor extends io.github.wysohn.triggerreactor.core.main.TriggerReactorCore {
    protected static final String ID = "triggerreactor";

    private static SpongeExecutorService syncExecutor = null;

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    static {
        System.setProperty("bstats.relocatecheck", "false");
    }

    @Inject
    private MetricsLite2 metrics;

    private Lag tpsHelper;

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

    private AbstractNamedTriggerManager namedTriggerManager;

    private SelfReference selfReference = new CommonFunctions(this);
    private IWrapper wrapper = new SpongeWrapper();
    
    @Listener
    public void onConstruct(GameInitializationEvent event) {
        syncExecutor = Sponge.getScheduler().createSyncExecutor(this);

        try {
            executorManager = new ExecutorManager(this);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            placeholderManager = new PlaceholderManager(this);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            variableManager = new VariableManager(this);
        } catch (IOException e) {
            initFailed(e);
            return;
        }

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

        this.namedTriggerManager = new NamedTriggerManager(this);

        tpsHelper = new Lag();
        Sponge.getScheduler().createTaskBuilder().execute(tpsHelper).delayTicks(100L).intervalTicks(1L).submit(this);

    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        disablePlugin();
    }

    public Lag getTpsHelper() {
        return tpsHelper;
    }

    @Listener
    public void onLoadComplete(GameAboutToStartServerEvent e) {
        for (Entry<String, Class<? extends AbstractAPISupport>> entry : APISupport.getSharedVars().entrySet()) {
            AbstractAPISupport.addSharedVar(sharedVars, entry.getKey(), entry.getValue());
        }
    }

    @Listener
    public void onInitialize(GameAboutToStartServerEvent e) {
        Sponge.getCommandManager().register(this, new CommandCallable() {

            @Override
            public CommandResult process(CommandSource src, String args) throws CommandException {
                if (src instanceof Player) {
                    onCommand(new SpongePlayer((Player) src), "triggerreactor",
                            args.split(" "));
                } else {
                    onCommand(new SpongeCommandSender(src), "triggerreactor",
                            args.split(" "));
                }

                return CommandResult.success();
            }

            @Override
            public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition)
                    throws CommandException {
                return new ArrayList<>();
            }

            @Override
            public boolean testPermission(CommandSource source) {
                return source.hasPermission("triggerreactor.admin");
            }

            @Override
            public Optional<Text> getShortDescription(CommandSource source) {
                return Optional.of(Text.of("TriggerReactor"));
            }

            @Override
            public Optional<Text> getHelp(CommandSource source) {
                return Optional.of(Text.of("/trg for details"));
            }

            @Override
            public Text getUsage(CommandSource source) {
                return Text.of("/trg for details");
            }

        }, "trg", "trigger");
    }

    @Listener
    public void onEnable(GameStartedServerEvent e) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        File file = new File(getDataFolder(), "config.yml");
        getConfigManager().setMigrationHelper(new IMigrationHelper() {
            @Override
            public void migrate(IConfigSource current) {
                // seems not necessary since we haven't been using config.yml in sponge
            }
        });

        for (Manager manager : Manager.getManagers()) {
            try {
                manager.reload();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

        Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {

            @Override
            public void run() {
                Sponge.getEventManager().post(new TriggerReactorStartEvent(TriggerReactor.this));
            }

        }).submit(this);
    }

    @Listener
    public void onDisable(GameStoppingServerEvent e) {
        try {
            Sponge.getEventManager().post(new TriggerReactorStopEvent(TriggerReactor.this));
        } finally {
            getLogger().info("Shutting down the managers...");
            onDisable();
            getLogger().info("OK");

            getLogger().info("Finalizing the scheduled script executions...");
            cachedThreadPool.shutdown();
            getLogger().info("Shut down complete!");
        }
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        for (Manager manager : Manager.getManagers())
            manager.reload();

        getExecutorManager().reload();
        getPlaceholderManager().reload();
    }

    @Override
	public SelfReference getSelfReference() {
		return selfReference;
	}

	@Override
	public IWrapper getWrapper() {
		return wrapper;
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
    public AbstractNamedTriggerManager getNamedTriggerManager() {
        return namedTriggerManager;
    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        ItemStack IS = iS.get();
        List<Text> lores = IS.get(Keys.ITEM_LORE).orElse(null);
        if (lores != null) {
            lores.remove(index);
            IS.offer(Keys.ITEM_LORE, lores);
            return true;
        }

        return false;
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        ItemStack IS = iS.get();
        List<Text> lores = IS.get(Keys.ITEM_LORE).orElse(null);
        if (lores != null) {
            if (lores.size() > 0 && index >= 0 && index < lores.size()) {
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
        List<Text> lores = IS.get(Keys.ITEM_LORE).orElse(null);
        if (lores == null)
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
        Player player = Sponge.getServer().getPlayer(string).orElse(null);
        if (player == null)
            return null;

        return new SpongePlayer(player);
    }

    @Override
    protected Object createEmptyPlayerEvent(ICommandSender sender) {
        Object unwrapped = sender.get();

        if (unwrapped instanceof Player) {
            return new Event() {

                @Override
                public Cause getCause() {
                    Player src = (Player) unwrapped;
                    EventContext context = EventContext.builder().add(EventContextKeys.PLAYER, src).build();
                    return Cause.builder().append(unwrapped).build(context);
                }
            };
        } else if (unwrapped instanceof ConsoleSource) {
            return new Event() {
                Cause cause = null;

                {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(new DelegatedPlayer((CommandSource) unwrapped));
                        cause = frame.getCurrentCause();
                    }
                }

                @Override
                public Cause getCause() {
                    return cause;
                }
            };
        } else {
            throw new RuntimeException("Cannot create empty PlayerEvent for " + sender);
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
    public String getPluginDescription() {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin(ID).orElse(null);
        if (plugin != null)
            return plugin.getDescription().orElse(ID + " v[?]");
        else
            return ID + " v[?]";
    }

    @Override
    public String getVersion() {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin(ID).orElse(null);
        if (plugin != null)
            return plugin.getVersion().orElse("?");
        else
            return "?";
    }

    @Override
    public String getAuthor() {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin(ID).orElse(null);
        if (plugin != null)
            return plugin.getAuthors().toString();
        else
            return "?";
    }

    @Override
    protected void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        CommandSource source = sender.get();
        if (source instanceof Player) {
            Player player = (Player) source;

            for (Entry<SimpleLocation, Trigger> entry : set) {
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
        return logger;
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
        Sponge.getScheduler().createTaskBuilder().execute(runnable).submit(this);
    }

    private final Set<Class<? extends Manager>> savings = new HashSet<>();

    @Override
    public void saveAsynchronously(Manager manager) {
        if (savings.contains(manager.getClass()))
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (savings) {
                        savings.add(manager.getClass());
                    }

                    getLogger().info("Saving " + manager.getClass().getSimpleName());
                    manager.saveAll();
                    getLogger().info("Saving Done!");
                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("Failed to save " + manager.getClass().getSimpleName());
                } finally {
                    synchronized (savings) {
                        savings.remove(manager.getClass());
                    }
                }
            }
        }) {{
            this.setPriority(MIN_PRIORITY);
        }}.start();
    }

    @Override
    public ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns) {
        return new ProcessInterrupter() {
            @Override
            public boolean onNodeProcess(Node node) {
                return false;
            }

            @Override
            public boolean onCommand(Object context, String command, Object[] args) {
                if ("CALL".equalsIgnoreCase(command)) {
                    if (args.length < 1)
                        throw new RuntimeException("Need parameter [String] or [String, boolean]");

                    if (args[0] instanceof String) {
                        Trigger trigger = getNamedTriggerManager().getTriggerForName((String) args[0]);
                        if (trigger == null)
                            throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                        if (args.length > 1 && args[1] instanceof Boolean) {
                            trigger.setSync((boolean) args[1]);
                        } else {
                            trigger.setSync(true);
                        }

                        if (trigger.isSync()) {
                            trigger.activate(e, interpreter.getVars());
                        } else {//use snapshot to avoid concurrent modification
                            trigger.activate(e, new HashMap<>(interpreter.getVars()));
                        }

                        return true;
                    } else {
                        throw new RuntimeException("Parameter type not match; it should be a String."
                                + " Make sure to put double quotes, if you provided String literal.");
                    }
                } else if ("CANCELEVENT".equalsIgnoreCase(command)) {
                    if (!interpreter.isSync())
                        throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                    if (context instanceof Cancellable) {
                        ((Cancellable) context).setCancelled(true);
                        return true;
                    } else {
                        throw new RuntimeException(context + " is not a Cancellable event!");
                    }
                } else if ("COOLDOWN".equalsIgnoreCase(command)) {
                    if (!(args[0] instanceof Number))
                        throw new RuntimeException(args[0] + " is not a number!");

                    long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                    if (e instanceof Event) {
                        ((Event) e).getCause().first(Player.class).ifPresent((player) -> {
                            UUID uuid = player.getUniqueId();
                            cooldowns.put(uuid, System.currentTimeMillis() + mills);
                        });
                    }
                    return true;
                }

                return false;
            }

            @Override
            public Object onPlaceholder(Object context, String placeholder, Object[] args) {
//                if("cooldown".equals(placeholder) && e instanceof Event){
//                    Optional<Player> optPlayer = ((Event) e).getCause().first(Player.class);
//                    if(optPlayer.isPresent()){
//                        Player player = optPlayer.get();
//                        long secondsLeft = Math.max(0L,
//                                cooldowns.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis());
//                        return secondsLeft / 1000;
//                    }else{
//                        return 0L;
//                    }
//                }else{
//                    return null;
//                }
                return null;
            }
        };
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return new ProcessInterrupter() {
            @Override
            public boolean onNodeProcess(Node node) {
                //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                //  is still running after the inventory is closed.
                if (e instanceof InteractInventoryEvent.Open
                        || e instanceof InteractInventoryEvent.Close) {
                    Inventory inv = ((InteractInventoryEvent) e).getTargetInventory();
                    if (!(inv instanceof CarriedInventory))
                        return false;

                    CarriedInventory inventory = (CarriedInventory) inv;
                    Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

                    if (carrier == null)
                        return false;

                    //it's not GUI so stop execution
                    return !inventoryMap.containsKey(new SpongeInventory(inv, carrier));
                }

                return false;
            }

            @Override
            public boolean onCommand(Object context, String command, Object[] args) {
                if ("CALL".equalsIgnoreCase(command)) {
                    if (args.length < 1)
                        throw new RuntimeException("Need parameter [String] or [String, boolean]");

                    if (args[0] instanceof String) {
                        Trigger trigger = getNamedTriggerManager().getTriggerForName((String) args[0]);
                        if (trigger == null)
                            throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                        if (args.length > 1 && args[1] instanceof Boolean) {
                            trigger.setSync((boolean) args[1]);
                        } else {
                            trigger.setSync(true);
                        }

                        if (trigger.isSync()) {
                            trigger.activate(e, interpreter.getVars());
                        } else {//use snapshot to avoid concurrent modification
                            trigger.activate(e, new HashMap<>(interpreter.getVars()));
                        }

                        return true;
                    } else {
                        throw new RuntimeException("Parameter type not match; it should be a String."
                                + " Make sure to put double quotes, if you provided String literal.");
                    }
                } else if ("CANCELEVENT".equalsIgnoreCase(command)) {
                    if (!interpreter.isSync())
                        throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                    if (context instanceof Cancellable) {
                        ((Cancellable) context).setCancelled(true);
                        return true;
                    } else {
                        throw new RuntimeException(context + " is not a Cancellable event!");
                    }
                } else if ("COOLDOWN".equalsIgnoreCase(command)) {
                    if (!(args[0] instanceof Number))
                        throw new RuntimeException(args[0] + " is not a number!");

                    long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                    if (e instanceof Event) {
                        ((Event) e).getCause().first(Player.class).ifPresent((player) -> {
                            UUID uuid = player.getUniqueId();
                            cooldowns.put(uuid, System.currentTimeMillis() + mills);
                        });
                    }
                    return true;
                }

                return false;
            }

            @Override
            public Object onPlaceholder(Object context, String placeholder, Object[] args) {
//                if("cooldown".equals(placeholder) && e instanceof Event){
//                    Optional<Player> optPlayer = ((Event) e).getCause().first(Player.class);
//                    if(optPlayer.isPresent()){
//                        Player player = optPlayer.get();
//                        long secondsLeft = Math.max(0L,
//                                cooldowns.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis());
//                        return secondsLeft / 1000;
//                    }else{
//                        return 0L;
//                    }
//                }else{
//                    return null;
//                }
                return null;
            }
        };
    }

    @Override
    public IPlayer extractPlayerFromContext(Object e) {
        if (e instanceof Event) {
            Player player = ((Event) e).getCause().first(Player.class).orElse(null);
            if (player != null)
                return new SpongePlayer(player);
        }

        return null;
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        return syncExecutor.submit(call);
    }

    @Override
    public void callEvent(IEvent event) {
        Event e = event.get();

        Sponge.getEventManager().post(e);
    }

    @Override
    public boolean isServerThread() {
        boolean result = false;

        synchronized (this) {
            result = Sponge.getServer().isMainThread();
        }

        return result;
    }

    @Override
    public Map<String, Object> getCustomVarsForTrigger(Object e) {
        Map<String, Object> variables = new HashMap<>();
        // Thanks for the amazing API!
        if (e instanceof Event) {
            ((Event) e).getCause().first(Player.class).ifPresent((player) -> {
                variables.put("player", player);
            });

            ((Event) e).getCause().first(Entity.class).ifPresent((entity) -> {
                variables.put("entity", entity);
            });
        }
        return variables;
    }

    @Override
    public ICommandSender getConsoleSender() {
        return new SpongeCommandSender(Sponge.getServer().getConsole());
    }
}
