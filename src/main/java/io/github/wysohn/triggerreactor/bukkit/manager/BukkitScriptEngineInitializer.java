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
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);
        IScriptEngineInitializer.registerClass(sem, Executor.class);
        IScriptEngineInitializer.registerClass(sem, Bukkit.class);
        IScriptEngineInitializer.registerClass(sem, Location.class);
        IScriptEngineInitializer.registerClass(sem, ChatColor.class);
        IScriptEngineInitializer.registerClass(sem, BukkitUtil.class);
    }
}
