/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

@Singleton
public class BukkitPluginManagement implements IPluginManagement {
    @Inject
    private Logger logger;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private Server server;

    @Inject
    private TaskSupervisor taskSupervisor;
    @Inject
    private NamedTriggerManager namedTriggerManager;

    private final Set<Class<? extends Manager>> savings = new HashSet<>();

    private boolean debugging = false;

    @Override
    public boolean isDebugging() {
        return debugging;
    }

    @Override
    public void setDebugging(boolean bool) {
        debugging = bool;
    }

    @Override
    public void runCommandAsConsole(String command) {
        server.dispatchCommand(server.getConsoleSender(), command);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public ICommandSender getConsoleSender() {
        return new BukkitCommandSender(Bukkit.getConsoleSender());
    }

    @Override
    public String getPluginDescription() {
        return plugin.getDescription().getFullName();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    public Map<String, Object> getCustomVarsForTrigger(Object e) {
        Map<String, Object> variables = new HashMap<String, Object>();
        //this should be fine as script loosely check the variable type
        if (e instanceof AbstractJavaPlugin.CommandSenderEvent) {
            variables.put("player", ((AbstractJavaPlugin.CommandSenderEvent) e).sender);
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
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                     | InvocationTargetException e1) {
                return variables;
            }
        }

        return variables;
    }

    @Override
    public IPlayer extractPlayerFromContext(Object e) {
        if (e instanceof PlayerEvent) {
            Player player = ((PlayerEvent) e).getPlayer();
            return new BukkitPlayer(player);
        } else if (e instanceof InventoryInteractEvent) {
            HumanEntity he = ((InventoryInteractEvent) e).getWhoClicked();
            if (he instanceof Player)
                return new BukkitPlayer((Player) he);
        }

        return null;
    }

    public void disablePlugin() {
        server.getPluginManager().disablePlugin(plugin);
    }

    private ProcessInterrupter.Builder newInterrupterBuilder() {
        return ProcessInterrupter.Builder.begin()
                .perExecutor((context, command, args) -> {
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

                            if (sync) {
                                trigger.activate(context.getTriggerCause(), context.getVars(), true);
                            } else {//use snapshot to avoid concurrent modification
                                trigger.activate(context.getTriggerCause(), new HashMap<>(context.getVars()), false);
                            }

                            return true;
                        } else {
                            throw new RuntimeException("Parameter type not match; it should be a String."
                                                               + " Make sure to put double quotes, if you provided "
                                                               + "String literal.");
                        }
                    }

                    return false;
                })
                .perExecutor((context, command, args) -> {
                    if ("CANCELEVENT".equalsIgnoreCase(command)) {
                        if (!taskSupervisor.isServerThread())
                            throw new RuntimeException("Trying to cancel event in async trigger.");

                        if (context.getTriggerCause() instanceof Cancellable) {
                            ((Cancellable) context.getTriggerCause()).setCancelled(true);
                            return true;
                        } else {
                            throw new RuntimeException(context.getTriggerCause() + " is not a Cancellable event!");
                        }
                    }

                    return false;
                });
    }

    private ProcessInterrupter.Builder appendCooldownInterrupter(ProcessInterrupter.Builder builder,
                                                                 Map<UUID, Long> cooldowns) {
        return builder.perExecutor(((context, command, args) -> {
            if ("COOLDOWN".equalsIgnoreCase(command)) {
                if (!(args[0] instanceof Number))
                    throw new RuntimeException(args[0] + " is not a number!");

                if (context.getTriggerCause() instanceof PlayerEvent) {
                    long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                    Player player = ((PlayerEvent) context.getTriggerCause()).getPlayer();
                    UUID uuid = player.getUniqueId();
                    cooldowns.put(uuid, System.currentTimeMillis() + mills);
                }
                return true;
            }

            return false;
        })).perPlaceholder((context, placeholder, args) -> {
            return null;
        });
    }

    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns)
                .build();
    }

    public ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns)
                .perNode((context, node) -> {
                    //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                    //  is still running after the inventory is closed.
                    if (context.getTriggerCause() instanceof InventoryOpenEvent
                            || context.getTriggerCause() instanceof InventoryClickEvent) {
                        Inventory inv = ((InventoryEvent) context.getTriggerCause()).getInventory();

                        //it's not GUI so stop execution
                        return !inventoryMap.containsKey(new BukkitInventory(inv));
                    }

                    return false;
                })
                .build();
    }

    @Override
    public void saveAsynchronously(final Manager manager) {
        if (savings.contains(manager.getClass()))
            return;

        new Thread(() -> {
            try {
                synchronized (savings) {
                    savings.add(manager.getClass());
                }

                logger.info("Saving " + manager.getClass().getSimpleName());
//                manager.saveAll();
                logger.info("Saving Done!");
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Failed to save " + manager.getClass().getSimpleName());
            } finally {
                synchronized (savings) {
                    savings.remove(manager.getClass());
                }
            }
        }) {{
            this.setPriority(MIN_PRIORITY);
        }}.start();
    }

    @Override
    public void saveAsynchronously(Class<? extends Manager> managerClass) {

    }

    @Override
    public <T> T getMain() {
        return (T) plugin;
    }
}
