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
package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class AbstractNamedTriggerManager extends AbstractTriggerManager {

    protected final Map<String, Trigger> triggers = new HashMap<>();

    public AbstractNamedTriggerManager(TriggerReactorCore plugin, File tirggerFolder) {
        super(plugin, tirggerFolder);
    }

    @Override
    public void reload() {
        triggers.clear();

        for (File file : folder.listFiles()) {
            try {
                load(file);
            } catch (TriggerInitFailedException | IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
/*        Set<Entry<String, Trigger>> failed = new HashSet<>();

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
        }*/
    }

    @Override
    public Collection<? extends Trigger> getAllTriggers() {
        return triggers.values();
    }

    /**
     * The naming follows this rule: triggers saved in the NamedTriggers folder will have name exactly same as the file name.
     * However, if you have another folder under NamedTriggers, and the trigger is saved under that folder,
     * you need to specify the folder in front of the file name.
     * For example, if you have your trigger path NamedTriggers/SomeFolder/AnotherFolder/yourtrigger, then the name will be like
     * this: <b>NamedTriggers:SomeFolder:AnotherFolder:yourtrigger</b>.
     *
     * @param name the trigger name including path if any exists
     * @return the Trigger; null if no such trigger
     */
    public Trigger getTriggerForName(String name) {
        return triggers.get(name);
    }

    /**
     * Load script file or folder recursively. If given file is file, it will
     * just load the trigger, but if it is folder, it will recursively load the
     * trigger with their path named appended with ':' sign. For example, if
     * Test is under Hi folder, it will be named Hi:Test.
     *
     * @param file the file/folder
     * @throws IOException
     * @throws LexerException
     * @throws ParserException
     */
    protected void load(File file) throws TriggerInitFailedException, IOException {
        load(new Stack<>(), file);
    }

    private void load(Stack<String> stack, File file)
            throws TriggerInitFailedException, IOException {
        if (file.isDirectory()) {
            stack.push(file.getName());
            for (File f : file.listFiles()) {
                load(stack, f);
            }
            stack.pop();
        } else {
            if (!isTriggerFile(file))
                return;

            String triggerName = extractName(file);

            StringBuilder builder = new StringBuilder();
            for (int i = stack.size() - 1; i >= 0; i--) {
                builder.append(stack.get(i) + ":");
            }
            builder.append(triggerName);

            if (triggers.containsKey(builder.toString())) {
                plugin.getLogger().warning(builder.toString() + " already registered! Duplicating Named Trigger?");
            } else {
                Trigger trigger = new NamedTrigger(builder.toString(), file, FileUtil.readFromFile(file));
                triggers.put(builder.toString(), trigger);
            }
        }
    }

    protected static class NamedTrigger extends Trigger {

        public NamedTrigger(String name, File file, String script) throws TriggerInitFailedException {
            super(name, file, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new NamedTrigger(triggerName, file, getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}