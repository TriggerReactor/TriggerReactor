package io.github.wysohn.triggerreactor.bukkit.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;

public interface BukkitScriptEngineInitializer extends IScriptEngineInitializer {
    @Override
    default void extractCustomVariables(Map<String, Object> variables, Object e) {
        if(e instanceof InventoryInteractEvent){
            if(((InventoryInteractEvent) e).getWhoClicked() instanceof Player)
                variables.put("player", ((InventoryInteractEvent) e).getWhoClicked());
        } else if(e instanceof InventoryCloseEvent){
            if(((InventoryCloseEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryCloseEvent) e).getPlayer());
        } else if(e instanceof InventoryOpenEvent){
            if(((InventoryOpenEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryOpenEvent) e).getPlayer());
        } else if(e instanceof PlayerDeathEvent) {
            variables.put("player", ((PlayerDeathEvent) e).getEntity());
        } else if(e instanceof EntityEvent) { //Some EntityEvent use entity field to store Player instance.
            Entity entity = ((EntityEvent) e).getEntity();
            if(entity instanceof Player) {
                variables.put("player", entity);
            }
        } else if(e instanceof BlockEvent) {
            try {
                Method m = e.getClass().getMethod("getPlayer");
                variables.put("player", m.invoke(e));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                return;
            }
        }
    }

    @Override
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);
        IScriptEngineInitializer.registerClass(sem, Executor.class);
        IScriptEngineInitializer.registerClass(sem, Bukkit.class);
        IScriptEngineInitializer.registerClass(sem, Location.class);
        IScriptEngineInitializer.registerClass(sem, ChatColor.class);
        IScriptEngineInitializer.registerClass(sem, BukkitUtil.class);
    }
}
