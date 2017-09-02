/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.bukkit.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class TriggerReactor extends JavaPlugin {
    private final JavaPluginBridge javaPluginBridge;

    public TriggerReactor(){
        javaPluginBridge = new JavaPluginBridge();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        checkConfigurationSerialization();

        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists()){
            try{
                String configStr = FileUtil.readFromStream(getResource("config.yml"));
                FileUtil.writeToFile(file, configStr);
            }catch(IOException e){
                e.printStackTrace();
                this.setEnabled(false);
            }
        }

        javaPluginBridge.onEnable(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        javaPluginBridge.onDisable(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            return this.javaPluginBridge.onCommand(
                    new BukkitPlayer((Player) sender),
                    command.getName(),
                    args);
        }else{
            return this.javaPluginBridge.onCommand(
                    new BukkitCommandSender(sender),
                    command.getName(),
                    args);
        }
    }

    public File getJarFile(){
        return super.getFile();
    }

    private final Set<Class<? extends Manager>> savings = new HashSet<>();

    public boolean saveAsynchronously(final Manager manager){
        if(savings.contains(manager))
            return false;

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
        return true;
    }

    public boolean isDebugging() {
        return this.javaPluginBridge.isDebugging();
    }

    private void checkConfigurationSerialization() {
        if(!ConfigurationSerializable.class.isAssignableFrom(Location.class)){
            ConfigurationSerialization.registerClass(LocationSerializer.class, Location.class.getName());
        }
    }

    private static class LocationSerializer extends Location implements ConfigurationSerializable{

        public LocationSerializer(World world, double x, double y, double z) {
            super(world, x, y, z);
        }

        public LocationSerializer(World world, double x, double y, double z, float yaw, float pitch) {
            super(world, x, y, z, yaw, pitch);
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("world", this.getWorld().getName());

            data.put("x", this.getX());
            data.put("y", this.getY());
            data.put("z", this.getZ());

            data.put("yaw", this.getYaw());
            data.put("pitch", this.getPitch());

            return data;
        }

        public static Location deserialize(Map<String, Object> args) {
            World world = Bukkit.getWorld((String) args.get("world"));
            if (world == null) {
                throw new IllegalArgumentException("unknown world");
            }

            return new Location(world, NumberConversions.toDouble(args.get("x")),
                    NumberConversions.toDouble(args.get("y")), NumberConversions.toDouble(args.get("z")),
                    NumberConversions.toFloat(args.get("yaw")), NumberConversions.toFloat(args.get("pitch")));
        }
    }
}
