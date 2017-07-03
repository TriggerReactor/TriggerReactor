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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class RepeatingTriggerManager extends AbstractRepeatingTriggerManager {
    private final File folder;
    public RepeatingTriggerManager(TriggerReactor plugin) {
        super(plugin);

        this.folder = new File(plugin.getDataFolder(), "RepeatTrigger");
        if(!this.folder.exists()){
            this.folder.mkdirs();
        }

        reload();
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        repeatTriggers.clear();
        for(Entry<String, Thread> entry : runningThreads.entrySet()){
            entry.getValue().interrupt();
        }
        runningThreads.clear();

        for(File file : folder.listFiles(filter)){
            String fileName = file.getName();
            String triggerName = fileName.substring(0, fileName.indexOf('.'));

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            boolean autoStart = yaml.getBoolean("AutoStart", false);
            long interval = yaml.getLong("Interval", 1000L);

            String script = null;
            try {
                script = FileUtil.readFromFile(new File(folder, triggerName));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            RepeatingTrigger trigger = null;
            try {
                trigger = new RepeatingTrigger(triggerName, script, interval);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            trigger.setAutoStart(autoStart);
            trigger.setInterval(interval);

            repeatTriggers.put(triggerName, trigger);

            final RepeatingTrigger triggerCopy = trigger;
            //start 1 tick later so other managers can be initialized.
            plugin.runTask(new Runnable(){
                @Override
                public void run() {
                    if(triggerCopy.isAutoStart()){
                        startTrigger(triggerName);
                    }
                }
            });
        }
    }

    @Override
    public void saveAll() {
        for(Entry<String, RepeatingTrigger> entry : repeatTriggers.entrySet()){
            String triggerName = entry.getKey();
            RepeatingTrigger trigger = entry.getValue();

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            yaml.set("AutoStart", trigger.isAutoStart());
            yaml.set("Interval", trigger.getInterval());
            try {
                yaml.save(new File(folder, triggerName+".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileUtil.writeToFile(new File(folder, triggerName), trigger.getScript());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void saveInfo(RepeatingTrigger trigger) throws IOException {
        Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
        yaml.set("AutoStart", false);
        yaml.set("Interval", trigger.getInterval());
        yaml.save(new File(folder, trigger.getTriggerName() + ".yml"));

        FileUtil.writeToFile(new File(folder, trigger.getTriggerName()), trigger.getScript());
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(folder, trigger.getTriggerName() + ".yml"));
        FileUtil.delete(new File(folder, trigger.getTriggerName()));
    }
}
