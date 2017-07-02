package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

public abstract class AbstractCommandTriggerManager extends TriggerManager {

    protected final Map<String, CommandTrigger> commandTriggerMap = new HashMap<>();

    public static class CommandTrigger extends TriggerManager.Trigger {

        public CommandTrigger(String name, String script) throws IOException, LexerException, ParserException {
            super(name, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new CommandTrigger(triggerName, getScript());
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean hasCommandTrigger(String cmd) {
        return commandTriggerMap.containsKey(cmd);
    }

    /**
     *
     * @param adding CommandSender to send error message on script error
     * @param cmd command to intercept
     * @param script script to be executed
     * @return true on success; false if cmd already binded.
     */
    public boolean addCommandTrigger(ICommandSender adding, String cmd, String script) {
        if(commandTriggerMap.containsKey(cmd))
            return false;

        CommandTrigger trigger = null;
        try {
            trigger = new CommandTrigger(cmd, script);
        } catch (IOException | LexerException | ParserException e1) {
            adding.sendMessage(ChatColor.RED + "Encounterd an error!");
            adding.sendMessage(ChatColor.RED + e1.getMessage());
            adding.sendMessage(ChatColor.RED + "If you are an administrator, check console to see details.");
            e1.printStackTrace();
            return false;
        }

        commandTriggerMap.put(cmd, trigger);

        plugin.saveAsynchronously(this);
        return true;
    }

    /**
     *
     * @param cmd command to stop intercept
     * @return true on success; false if cmd does not exist.
     */
    public boolean removeCommandTrigger(String cmd) {
        if(!commandTriggerMap.containsKey(cmd))
            return false;

        deleteInfo(commandTriggerMap.remove(cmd));

        return true;
    }

    public CommandTrigger createTempCommandTrigger(String script) throws IOException, LexerException, ParserException {
        return new CommandTrigger("temp", script);
    }

    public AbstractCommandTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}