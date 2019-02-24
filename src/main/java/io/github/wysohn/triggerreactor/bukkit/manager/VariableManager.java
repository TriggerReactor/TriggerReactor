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
package io.github.wysohn.triggerreactor.bukkit.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class VariableManager extends AbstractVariableManager{
    private File varFile;
    private FileConfiguration varFileConfig;

    private Boolean saving = false;
    
    public VariableManager(TriggerReactor plugin) throws IOException, InvalidConfigurationException {
        super(plugin);

        varFile = new File(plugin.getDataFolder(), "var.yml");
        if(!varFile.exists())
            varFile.createNewFile();

        checkConfigurationSerialization();

        reload();
    }

    @Override
    public void reload(){
    	plugin.getLogger().info("Waiting for previous saving tasks...");
       	synchronized(saving) {
           	plugin.getLogger().info("Done! now reloading global variables...");
            varFileConfig = new Utf8YamlConfiguration();
            try {
                varFileConfig.load(varFile);
             	plugin.getLogger().info("Global variables were loaded from "+varFile.getName());
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
       	}
    }

    @Override
    public void saveAll(){
        if(saving)
            return;

        saving = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
            	synchronized(saving) {
                    try {
                        FileUtil.writeToFile(varFile, varFileConfig.saveToString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe("Something went wrong while saving global variable!");
                    } finally {
                        saving = false;
                    }
            	}
            }
        }).start();
    }

    @Override
    public Object get(String key){
        return varFileConfig.get(key);
    }

    @Override
    public void put(String key, Object value) throws Exception{
        //hard code it for now
        if(value instanceof Location){
            varFileConfig.set(key, new SerializableLocation((Location) value));
        }else if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)
                && !(value instanceof ConfigurationSerializable)){
            throw new Exception("[" + value.getClass().getSimpleName() + "] is not a valid type to be saved.");
        }else{
            varFileConfig.set(key, value);
        }

        saveAll();
    }

    @Override
    public boolean has(String key){
        return varFileConfig.contains(key);
    }

    @Override
    public void remove(String key){
        varFileConfig.set(key, null);
    }

    private static void checkConfigurationSerialization() {
        if(!ConfigurationSerializable.class.isAssignableFrom(Location.class)){
            ConfigurationSerialization.registerClass(SerializableLocation.class, "org.bukkit.Location");
        }
    }

    @SerializableAs(value = "org.bukkit.Location")
    public static class SerializableLocation extends Location implements ConfigurationSerializable{

        public SerializableLocation(Location location) {
            super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(),
                    location.getPitch());
        }

        public SerializableLocation(World world, double x, double y, double z) {
            super(world, x, y, z);
        }

        public SerializableLocation(World world, double x, double y, double z, float yaw, float pitch) {
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

        public static SerializableLocation deserialize(Map<String, Object> args) {
            World world = Bukkit.getWorld((String) args.get("world"));
            if (world == null) {
                throw new IllegalArgumentException("unknown world");
            }

            return new SerializableLocation(new Location(world, NumberConversions.toDouble(args.get("x")),
                    NumberConversions.toDouble(args.get("y")), NumberConversions.toDouble(args.get("z")),
                    NumberConversions.toFloat(args.get("yaw")), NumberConversions.toFloat(args.get("pitch"))));
        }
    }
}