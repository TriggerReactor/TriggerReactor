package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public abstract class AbstractNamedTriggerManager extends TriggerManager {

    protected final Map<String, Trigger> triggers = new HashMap<>();

    public AbstractNamedTriggerManager(TriggerReactor plugin) {
        super(plugin);
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

    /**
     * Load script file or folder recursively. If given file is file, it will
     * just load the trigger, but if it is folder, it will recursively load the
     * trigger with their path named appended with ':' sign. For example, if
     * Test is under Hi folder, it will be named Hi:Test.
     *
     * @param file
     *            the file/folder
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

    protected static class NamedTrigger extends Trigger{

        public NamedTrigger(String name, String script) throws TriggerInitFailedException {
            super(name, script);

            init();
        }

        @Override
        public boolean isSync() {
            // Let named trigger be always sync (as the caller will be sync/async anyway.)
            return true;
        }

        @Override
        public Trigger clone() {
            try {
                return new NamedTrigger(triggerName, getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}