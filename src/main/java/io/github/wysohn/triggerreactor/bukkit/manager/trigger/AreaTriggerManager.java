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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.location.Area;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class AreaTriggerManager extends AbstractAreaTriggerManager {
    File folder;
    public AreaTriggerManager(TriggerReactor plugin) {
        super(plugin);

        folder = new File(plugin.getDataFolder(), "AreaTrigger");
        if(!folder.exists()){
            folder.mkdirs();
        }

        reload();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        areaTriggers.clear();

        for(File file : folder.listFiles(filter)){
            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            try {
                yamlFile.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load Area Trigger "+file);
                continue;
            }

            String name = file.getName().substring(0, file.getName().indexOf('.'));

            SimpleLocation smallest = getSmallestFromSection(yamlFile);
            SimpleLocation largest = getLargestFromSection(yamlFile);

            if(smallest == null || largest == null){
                plugin.getLogger().warning("Could not load Area Trigger"+file);
                plugin.getLogger().warning("Could not find Smallest: or Largest:");
                continue;
            }

            File scriptFolder = new File(folder, name);
            if(!scriptFolder.exists()){
                scriptFolder.mkdirs();
            }

            String enterScript = null;
            try {
                enterScript = FileUtil.readFromFile(new File(scriptFolder, "Enter"));
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            String exitScript = null;
            try {
                exitScript = FileUtil.readFromFile(new File(scriptFolder, "Exit"));
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            Area area = new Area(smallest, largest);
            AreaTrigger trigger = new AreaTrigger(area, name);
            nameMapper.put(name, trigger);

            boolean isSync = yamlFile.getBoolean("Sync", false);
            trigger.setSync(isSync);

            this.setupArea(trigger);

            try {
                if(enterScript != null){
                    trigger.setEnterTrigger(enterScript);
                }
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }

            try {
                if(exitScript != null){
                    trigger.setExitTrigger(exitScript);
                }
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        Set<AreaTrigger> saveReady = new HashSet<>();

        for(Entry<SimpleChunkLocation, Map<Area, AreaTrigger>> oentry : areaTriggers.entrySet()){
            SimpleChunkLocation scloc = oentry.getKey();

            for(Entry<Area, AreaTrigger> entry : oentry.getValue().entrySet()){
                Area area = entry.getKey();
                AreaTrigger trigger = entry.getValue();

                saveReady.add(trigger);
            }
        }

        for(AreaTrigger trigger : saveReady){
            Area area = trigger.getArea();

            File file = new File(folder, trigger.getTriggerName()+".yml");
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not create "+file);
                }
            }

            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            setSmallestForSection(yamlFile, area.getSmallest());
            setLargestForSection(yamlFile, area.getLargest());

            yamlFile.set("Sync", trigger.isSync());

            try {
                yamlFile.save(file);
            } catch (IOException e1) {
                e1.printStackTrace();
                plugin.getLogger().warning("Could not save "+file);
            }

            File triggerFolder = new File(folder, trigger.getTriggerName());
            if(!triggerFolder.exists()){
                triggerFolder.mkdirs();
            }

            if(trigger.getEnterTrigger() != null){
                try {
                    FileUtil.writeToFile(new File(triggerFolder, "Enter"), trigger.getEnterTrigger().getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Enter] "+trigger.getTriggerName());
                }
            }

            if(trigger.getExitTrigger() != null){
                try {
                    FileUtil.writeToFile(new File(triggerFolder, "Exit"), trigger.getExitTrigger().getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Exit] "+trigger.getTriggerName());
                }
            }
        }
    }

    private static final String SMALLEST = "Smallest";
    private static final String LARGEST = "Largest";
    private SimpleLocation getSmallestFromSection(ConfigurationSection section) {
        if(!section.contains(SMALLEST))
            return null;
        return SimpleLocation.valueOf(section.getString(SMALLEST, null));
    }

    private SimpleLocation getLargestFromSection(ConfigurationSection section) {
        if(!section.contains(LARGEST))
            return null;
        return SimpleLocation.valueOf(section.getString(LARGEST, null));
    }

    private void setSmallestForSection(ConfigurationSection section, SimpleLocation sloc) {
        section.set(SMALLEST, sloc.toString());
    }

    private void setLargestForSection(ConfigurationSection section, SimpleLocation sloc) {
        section.set(LARGEST, sloc.toString());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e){
        Entry<Area, AreaTrigger> from = getAreaForLocation(e.getFrom());
        Entry<Area, AreaTrigger> to = getAreaForLocation(e.getTo());

        if(from == null && to == null)
            return;

        if(from != null && to != null && from.getKey().equals(to.getKey()))
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        if(from != null){
            from.getValue().activate(e, varMap, EventType.EXIT);
        }

        if(to != null){
            to.getValue().activate(e, varMap, EventType.ENTER);
        }
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        File areafile = new File(folder, trigger.getTriggerName()+".yml");
        FileUtil.delete(areafile);
        File areafolder = new File(folder, trigger.getTriggerName());
        FileUtil.delete(areafolder);
    }
}
