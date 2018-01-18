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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class CommandTriggerManager extends AbstractCommandTriggerManager {
    File folder;
    public CommandTriggerManager(TriggerReactor plugin) {
        super(plugin);

        File dataFolder = plugin.getDataFolder();
        if(!dataFolder.exists())
            dataFolder.mkdirs();

        folder = new File(dataFolder, "CommandTrigger");
        if(!folder.exists())
            folder.mkdirs();

        reload();
    }

    @Override
    public void reload() {
        commandTriggerMap.clear();

        for(File file : folder.listFiles()){
            String fileName = file.getName();

            String script = null;
            try{
                script = FileUtil.readFromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            CommandTrigger trigger = null;
            try {
                trigger = new CommandTrigger(fileName, script);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }

            commandTriggerMap.put(fileName, trigger);
        }
    }

    @Override
    public void saveAll(){
        Set<String> failed = new HashSet<>();
        for(Entry<String, CommandTrigger> entry : commandTriggerMap.entrySet()){
            String fileName = entry.getKey();
            CommandTrigger trigger = entry.getValue();

            String script = trigger.getScript();

            File file = new File(folder, fileName);
            try{
                FileUtil.writeToFile(file, script);
            }catch(Exception e){
                e.printStackTrace();
                plugin.getLogger().severe("Could not save command trigger for "+fileName);
                failed.add(fileName);
            }
        }

        for(String key : failed){
            commandTriggerMap.remove(key);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e){
        Player player = e.getPlayer();
        String[] split = e.getMessage().split(" ");

        String cmd = split[0];
        cmd = cmd.replaceAll("/", "");
        String[] args = new String[split.length - 1];
        for(int i = 0; i < args.length; i++)
            args[i] = split[i + 1];

        CommandTrigger trigger = commandTriggerMap.get(cmd);
        if(trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(e, varMap);
        e.setCancelled(true);
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(folder, trigger.getTriggerName()));
    }

}
