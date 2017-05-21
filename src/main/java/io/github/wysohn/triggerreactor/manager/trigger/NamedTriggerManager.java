package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

        load(new Stack<String>(), folder);
    }

    private void load(Stack<String> stack, File file) {
        if(file.isDirectory()){
            stack.push(file.getName());
            for(File f : file.listFiles()){
                load(stack, f);
            }
            stack.pop();
        }else{
            StringBuilder builder = new StringBuilder();
            for(int i = stack.size() - 1; i >= 0; i--){
                builder.append(stack.get(i)+":");
            }
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if(triggers.containsKey(builder.toString())){
                plugin.getLogger().warning(builder.toString()+" already registered! Duplicating executors?");
            }else{
                try(FileReader fr = new FileReader(file)){
                    StringBuilder scriptBuilder = new StringBuilder();
                    int read = -1;

                    while((read = fr.read()) != -1){
                        scriptBuilder.append((char) read);
                    }

                    Trigger trigger = new Trigger(scriptBuilder.toString());
                    triggers.put(builder.toString(), trigger);
                }catch(IOException | LexerException | ParserException e){
                    e.printStackTrace();
                    plugin.getLogger().warning("Failed to load trigger "+builder.toString()+"!");
                }
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

            try(FileWriter fw = new FileWriter(file)){
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
}
