package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.event.CommandSenderEvent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class BukkitGameController implements IGameController {
    @Inject
    @Named("PluginInstance")
    Object plugin;
    @Inject
    Server server;
    @Inject
    IWrapper wrapper;

    @Inject
    NamedTriggerManager namedTriggerManager;

    @Inject
    public BukkitGameController() {
    }

    public void callEvent(IEvent event) {
        server.getPluginManager().callEvent(event.get());
    }

    public <T> Future<T> callSyncMethod(Callable<T> call) {
        try {
            return server.getScheduler().callSyncMethod((Plugin) plugin, call);
        } catch (Exception e) {
        }
        return null;
    }

    public Object createEmptyPlayerEvent(ICommandSender sender) {
        Object unwrapped = sender.get();

        if (unwrapped instanceof Player) {
            return new PlayerEvent((Player) unwrapped) {
                @Override
                public HandlerList getHandlers() {
                    return null;
                }
            };
        } else if (unwrapped instanceof CommandSender) {
            return new CommandSenderEvent((CommandSender) unwrapped);
        } else {
            throw new RuntimeException("Cannot create empty PlayerEvent for " + sender);
        }
    }

    private ProcessInterrupter.Builder appendCooldownInterrupter(ProcessInterrupter.Builder builder,
                                                                 Map<UUID, Long> cooldowns) {
        return builder.perExecutor(((context, command, args) -> {
            if ("COOLDOWN".equalsIgnoreCase(command)) {
                if (!(args[0] instanceof Number))
                    throw new RuntimeException(args[0] + " is not a number!");

                Player player = (Player) context.getVar(Trigger.VAR_NAME_PLAYER);
                if (player != null) {
                    long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                    UUID uuid = player.getUniqueId();
                    cooldowns.put(uuid, System.currentTimeMillis() + mills);
                }
                return true;
            }

            return false;
        })).perPlaceholder((context, placeholder, args) -> {
//            if ("cooldown".equals(placeholder)) {
//                if (context.getTriggerCause() instanceof PlayerEvent) {
//                    return cooldowns.getOrDefault(((PlayerEvent) context.getTriggerCause()).getPlayer().getUniqueId
//                    (), 0L);
//                } else {
//                    return 0;
//                }
//            }
            return null;
        });
    }

    private ProcessInterrupter.Builder newInterrupterBuilder() {
        return ProcessInterrupter.Builder.begin().perExecutor((context, command, args) -> {
            if ("CALL".equalsIgnoreCase(command)) {
                if (args.length < 1)
                    throw new RuntimeException("Need parameter [String] or [String, boolean]");

                if (args[0] instanceof String) {
                    Trigger trigger = namedTriggerManager.get((String) args[0]);
                    if (trigger == null)
                        throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                    boolean sync = true;
                    if (args.length > 1 && args[1] instanceof Boolean) {
                        sync = (boolean) args[1];
                    }

                    Map<String, Object> vars = context.getVarCopy();
                    if (sync) {
                        trigger.activate(vars, true);
                        context.clearVars();
                        context.putAllVars(vars);
                    } else {
                        trigger.activate(vars, false);
                        // we don't replace variables since the trigger will run asynchronously
                    }

                    return true;
                } else {
                    throw new RuntimeException("Parameter type not match; it should be a String. Make sure to put "
                            + "double quotes, if you provided String literal.");
                }
            }

            return false;
        }).perExecutor((context, command, args) -> {
            if ("CANCELEVENT".equalsIgnoreCase(command)) {
                if (!server.isPrimaryThread())
                    throw new RuntimeException("Trying to cancel event in async trigger.");

                Event event = (Event) context.getVar(Trigger.VAR_NAME_EVENT);
                if (event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                    return true;
                } else {
                    throw new RuntimeException(event + " is not a Cancellable event!");
                }
            }

            return false;
        });
    }

    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns).build();
    }

    public ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns).perNode((context, node) -> {
            //safety feature to stop all trigger immediately if executing on 'open' or 'click'
            //  is still running after the inventory is closed.
            Event event = (Event) context.getVar(Trigger.VAR_NAME_EVENT);
            if (event instanceof InventoryOpenEvent || event instanceof InventoryClickEvent) {
                Inventory inv = ((InventoryEvent) event).getInventory();

                //stop execution if it's not GUI
                IInventory inventory = wrapper.wrap(inv);
                return !inventoryMap.containsKey(inventory);
            }

            return false;
        }).build();
    }

    public Object createPlayerCommandEvent(ICommandSender sender, String label, String[] args) {
        Object unwrapped = sender.get();

        StringBuilder builder = new StringBuilder("/");
        builder.append(label);
        for (String arg : args) {
            builder.append(' ');
            builder.append(arg);
        }

        if (unwrapped instanceof Player) {
            return new PlayerCommandPreprocessEvent((Player) unwrapped, builder.toString());
        } else {
            throw new RuntimeException("Cannot create empty PlayerCommandPreprocessEvent for " + sender);
        }
    }

    public IPlayer extractPlayerFromContext(Object e) {
        if (e instanceof PlayerEvent) {
            Player player = ((PlayerEvent) e).getPlayer();
            return wrapper.wrap(player);
        } else if (e instanceof InventoryInteractEvent) {
            HumanEntity he = ((InventoryInteractEvent) e).getWhoClicked();
            if (he instanceof Player)
                return wrapper.wrap(he);
        }

        return null;
    }

    public ICommandSender getConsoleSender() {
        return wrapper.wrap(server.getConsoleSender());
    }

    public Map<String, Object> getCustomVarsForTrigger(Object e) {
        Map<String, Object> variables = new HashMap<String, Object>();
        //this should be fine as script loosely check the variable type
        if (e instanceof CommandSenderEvent) {
            variables.put("player", ((CommandSenderEvent) e).sender);
        } else if (e instanceof PlayerEvent) {
            variables.put("player", ((PlayerEvent) e).getPlayer());
        } else if (e instanceof InventoryInteractEvent) {
            if (((InventoryInteractEvent) e).getWhoClicked() instanceof Player)
                variables.put("player", ((InventoryInteractEvent) e).getWhoClicked());
        } else if (e instanceof InventoryCloseEvent) {
            if (((InventoryCloseEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryCloseEvent) e).getPlayer());
        } else if (e instanceof InventoryOpenEvent) {
            if (((InventoryOpenEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryOpenEvent) e).getPlayer());
        } else if (e instanceof PlayerDeathEvent) {
            variables.put("player", ((PlayerDeathEvent) e).getEntity());
        } else if (e instanceof EntityEvent) { // Some EntityEvent use entity field to store Player instance.
            Entity entity = ((EntityEvent) e).getEntity();
            variables.put("entity", entity);

            if (entity instanceof Player) {
                variables.put("player", entity);
            }
        } else if (e instanceof BlockEvent) {
            variables.put("block", ((BlockEvent) e).getBlock());

            try {
                Method m = e.getClass().getMethod("getPlayer");
                variables.put("player", m.invoke(e));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                return variables;
            }
        }

        return variables;
    }

    public IPlayer getPlayer(String string) {
        Player player = server.getPlayer(string);
        if (player != null)
            return wrapper.wrap(player);
        else
            return null;
    }

    public void showGlowStones(ICommandSender sender, Set<Map.Entry<SimpleLocation, Trigger>> set) {
        for (Map.Entry<SimpleLocation, Trigger> entry : set) {
            SimpleLocation sloc = entry.getKey();
            Player player = sender.get();
            player.sendBlockChange(
                    new Location(server.getWorld(sloc.getWorld()), sloc.getX(), sloc.getY(), sloc.getZ()),
                    Material.GLOWSTONE, (byte) 0);
        }
    }

    @Override
    public Iterable<? extends IPlayer> getOnlinePlayers() {
        return server.getOnlinePlayers()
                .stream()
                .map(wrapper::wrap)
                .map(IPlayer.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<? extends IWorld> getWorlds() {
        return server.getWorlds().stream()
                .map(wrapper::wrap)
                .map(IWorld.class::cast)
                .collect(Collectors.toList());
    }
}
