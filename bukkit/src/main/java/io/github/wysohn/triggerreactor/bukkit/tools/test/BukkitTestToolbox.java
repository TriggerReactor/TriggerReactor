package io.github.wysohn.triggerreactor.bukkit.tools.test;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;

public class BukkitTestToolbox {
    public static PluginCommand createCommand(Plugin plugin) throws Exception {
        Constructor<PluginCommand> con = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        con.setAccessible(true);
        return con.newInstance("", plugin);
    }
}
