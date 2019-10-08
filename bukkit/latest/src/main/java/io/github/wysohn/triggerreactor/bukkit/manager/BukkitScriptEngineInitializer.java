package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
