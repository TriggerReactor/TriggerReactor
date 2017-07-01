package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public abstract Trigger getTriggerForName(String name);

    protected static class NamedTrigger extends Trigger{

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

    /**
     * Load script file or folder recursively. If given file is file, it will just load
     * the trigger, but if it is folder, it will recursively load the trigger
     * with their path named appended with ':' sign. For example, if Test is
     * under Hi folder, it will be named Hi:Test.
     * @param file
     *            the file/folder
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws LexerException
     * @throws ParserException
     */
    protected void load(File file)
            throws UnsupportedEncodingException, IOException, LexerException, ParserException {
                load(new Stack<>(), file);
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
                        trigger.setSync(true);
                        triggers.put(builder.toString(), trigger);
                    }
                }
            }

    public AbstractNamedTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}