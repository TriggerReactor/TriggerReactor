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
package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtils;

public class CommandTriggerManager extends TriggerManager {
    private final Map<String, CommandTrigger> commandTriggerMap = new HashMap<>();

    private File folder;
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
            try(FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8")){
                StringBuilder builder = new StringBuilder();
                int read = -1;
                while((read = isr.read()) != -1)
                    builder.append((char) read);
                script = builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CommandTrigger trigger = null;
            try {
                trigger = new CommandTrigger(script);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
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
                FileUtils.writeToFile(file, script);
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
        varMap.put("player", player);
        varMap.put("command", cmd);
        if(args.length > 0){
            for(int i = 0; i < args.length; i++){
                varMap.put("args"+i, args[i]);
            }
        }

        trigger.activate(e, varMap);
        e.setCancelled(true);
    }

    public boolean hasCommandTrigger(String cmd){
        return commandTriggerMap.containsKey(cmd);
    }

    /**
     *
     * @param adding CommandSender to send error message on script error
     * @param cmd command to intercept
     * @param script script to be executed
     * @return true on success; false if cmd already binded.
     */
    public boolean addCommandTrigger(CommandSender adding, String cmd, String script){
        if(commandTriggerMap.containsKey(cmd))
            return false;

        CommandTrigger trigger = null;
        try {
            trigger = new CommandTrigger(script);
        } catch (IOException | LexerException | ParserException e1) {
            adding.sendMessage(ChatColor.RED + "Encounterd an error!");
            adding.sendMessage(ChatColor.RED + e1.getMessage());
            adding.sendMessage(ChatColor.RED + "If you are an administrator, check console to see details.");
            e1.printStackTrace();
            return false;
        }

        commandTriggerMap.put(cmd, trigger);
        return true;
    }

    /**
     *
     * @param cmd command to stop intercept
     * @return true on success; false if cmd does not exist.
     */
    public boolean removeCommandTrigger(String cmd){
        if(!commandTriggerMap.containsKey(cmd))
            return false;

        commandTriggerMap.remove(cmd);

        File file = new File(folder, cmd);
        file.delete();

        return true;
    }

    private class CommandTrigger extends TriggerManager.Trigger {

        public CommandTrigger(String script) throws IOException, LexerException, ParserException {
            super(script);
        }
    }
}
