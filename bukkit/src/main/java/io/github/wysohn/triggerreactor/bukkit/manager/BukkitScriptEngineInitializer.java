package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public interface BukkitScriptEngineInitializer extends IScriptEngineInitializer {
    @Override
    default void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);
//        IScriptEngineInitializer.registerClass(sem, Executor.class);
//        IScriptEngineInitializer.registerClass(sem, Bukkit.class);
//        IScriptEngineInitializer.registerClass(sem, Location.class);
//        IScriptEngineInitializer.registerClass(sem, ChatColor.class);
//        IScriptEngineInitializer.registerClass(sem, BukkitUtil.class);
    }
}
