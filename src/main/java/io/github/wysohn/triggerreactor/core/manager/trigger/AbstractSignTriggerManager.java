package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public abstract class AbstractSignTriggerManager extends AbstractTriggerManager {
    protected final String LINES = "lines";

    private final Map<String, SignTrigger> signTriggerMap = new HashMap<>();
    private final Map<String, SignTrigger> linesToTriggerMap = new HashMap<>();

    public AbstractSignTriggerManager(TriggerReactor plugin, SelfReference ref, File tirggerFolder) {
        super(plugin, ref, tirggerFolder);
    }

    @Override
    public void reload() {
        signTriggerMap.clear();

        for(File file : folder.listFiles()){
            if(!isTriggerFile(file))
                continue;

            String triggerName = extractName(file);

            File ymlFile = new File(folder, triggerName+".yml");
            if(!ymlFile.exists()) {
                plugin.getLogger().warning("Skipped Sign Trigger "+triggerName+" because couldn't find yml file.");
                continue;
            }

            String script = null;
            try{
                script = FileUtil.readFromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            List<String> lines = null;
            try {
                lines = this.getData(ymlFile, LINES, null);
            } catch (IOException e1) {
                e1.printStackTrace();
                plugin.getLogger().warning("Skipped Sign Trigger "+triggerName+" because couldn't read '"+LINES+"'");
            }

            if(lines == null) {
                plugin.getLogger().warning("Skipped Sign Trigger "+triggerName+" because couldn't find '"+LINES+"'");
                continue;
            }

            SignTrigger trigger = null;
            try {
                trigger = new SignTrigger(triggerName, lines.toArray(new String[4]), file, script);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }

            signTriggerMap.put(triggerName, trigger);
            String merged = trigger.getLinesMerged();
            if(linesToTriggerMap.containsKey(merged)) {
                SignTrigger prev = linesToTriggerMap.get(merged);
                plugin.getLogger().warning("SignTrigger "+trigger.triggerName+" has lines duplicating with another SignTrigger "+prev.triggerName+".");
                plugin.getLogger().warning("No sign interaction event will be passed to "+trigger.triggerName+".");
            }else {
                linesToTriggerMap.put(trigger.getLinesMerged(), trigger);
            }
        }
    }

    @Override
    public void saveAll() {
        Set<String> failed = new HashSet<>();
        for(Entry<String, SignTrigger> entry : signTriggerMap.entrySet()) {
            String fileName = entry.getKey();
            SignTrigger trigger = entry.getValue();

            String script = trigger.getScript();

            File file = new File(folder, fileName+".trg");
            try {
                FileUtil.writeToFile(file, script);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Could not save sign trigger for "+fileName);
                failed.add(fileName);
            }
        }

        for(String key : failed){
            signTriggerMap.remove(key);
        }
    }

    public SignTrigger getSignTriggerByName(String triggerName) {
        return signTriggerMap.get(triggerName);
    }

    public SignTrigger getSignTriggerByLines(String[] lines) {
        return this.linesToTriggerMap.get(getLinesMerged(lines));
    }

    public boolean addSignTrigger(String name, String[] lines, String script) throws TriggerInitFailedException {
        File file = new File(folder, name+".trg");
        if(file.exists())
            return false;

        SignTrigger trigger = new SignTrigger(name, lines, file, script);

        this.signTriggerMap.put(name, trigger);
        this.linesToTriggerMap.put(getLinesMerged(lines), trigger);

        return true;
    }

    public boolean deleteSignTrigger(String name) {
        if(!this.signTriggerMap.containsKey(name))
            return false;

        SignTrigger trigger = this.signTriggerMap.remove(name);
        if(trigger != null) {
            this.linesToTriggerMap.remove(trigger.getLinesMerged());
        }

        return true;
    }

    public static class SignTrigger extends Trigger {
        private final String[] lines;
        public SignTrigger(String triggerName, String[] lines, File file, String script) throws TriggerInitFailedException {
            super(triggerName, file, script);

            if(lines.length < 1)
                throw new TriggerInitFailedException("Sign trigger should have at least one line.", null);

            if(lines.length > 4)
                throw new TriggerInitFailedException("Sign trigger cannot have lines more than four.", null);

            this.lines = lines;

            init();
        }

        public String[] getLines() {
            return lines;
        }

        public String getLinesMerged() {
            return AbstractSignTriggerManager.getLinesMerged(lines);
        }

        @Override
        public Trigger clone() {
            try {
                return new SignTrigger(triggerName, lines, file, getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static String getLinesMerged(String[] lines) {
        if(lines.length < 1)
            return null;

        StringBuilder builder = new StringBuilder(lines[0]);
        for(int i = 1; i < lines.length; i++) {
            builder.append(":"+lines[i]);
        }
        return builder.toString();
    }
}
