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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class NamedTriggerManager extends TriggerManager {
    private final Map<String, Trigger> triggers = new HashMap<>();
    private final File folder;

    public NamedTriggerManager(TriggerReactor plugin) {
        super(plugin);

        folder = new File(plugin.getDataFolder(), "NamedTriggers");
        if(!folder.exists())
            folder.mkdirs();

        reload();
    }

    @Override
    public void reload() {
        triggers.clear();

        for (File file : folder.listFiles()) {
            try {
                load(new Stack<String>(), file);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void load(Stack<String> stack, File file)
            throws UnsupportedEncodingException, IOException, LexerException, ParserException {
        if (file.isDirectory()) {
            stack.push(file.getName());
            for (File f : file.listFiles()) {
                load(stack, f);
            }
            stack.pop();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = stack.size() - 1; i >= 0; i--) {
                builder.append(stack.get(i) + ":");
            }
            String fileName = file.getName();
            builder.append(fileName);

            if (triggers.containsKey(builder.toString())) {
                plugin.getLogger().warning(builder.toString() + " already registered! Duplicating Named Trigger?");
            } else {
                Trigger trigger = new NamedTrigger(builder.toString(), FileUtil.readFromFile(file));
                triggers.put(builder.toString(), trigger);
            }
        }
    }

    @Override
    public void saveAll() {
        Set<Entry<String, Trigger>> failed = new HashSet<>();

        for(Entry<String, Trigger> entry : triggers.entrySet()){
            String key = entry.getKey().replaceAll(":", File.separator);
            Trigger trigger = entry.getValue();

            File file = new File(folder, key);
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            try{
                FileUtil.writeToFile(file, trigger.getScript());
            }catch(IOException e){
                e.printStackTrace();
                plugin.getLogger().warning("Failed to save file "+key);
                failed.add(entry);
            }
        }
    }

    /**
     * The naming follows this rule: triggers saved in the NamedTriggers folder will have name exactly same as the file name.
     * However, if you have another folder under NamedTriggers, and the trigger is saved under that folder,
     * you need to specify the folder in front of the file name.
     * For example, if you have your trigger path NamedTriggers/SomeFolder/AnotherFolder/yourtrigger, then the name will be like
     * this: <b>NamedTriggers:SomeFolder:AnotherFolder:yourtrigger</b>.
     * @param name the trigger name including path if any exists
     * @return the Trigger; null if no such trigger
     */
    public Trigger getTriggerForName(String name){
        return triggers.get(name);
    }

    private class NamedTrigger extends Trigger{

        public NamedTrigger(String name, String script) throws IOException, LexerException, ParserException {
            super(name, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new NamedTrigger(triggerName, getScript());
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
