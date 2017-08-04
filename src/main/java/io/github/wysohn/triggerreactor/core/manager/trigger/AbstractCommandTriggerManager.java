package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.util.HashMap;
import java.util.Map;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class AbstractCommandTriggerManager extends TriggerManager {

    protected final Map<String, CommandTrigger> commandTriggerMap = new HashMap<>();

    public static class CommandTrigger extends TriggerManager.Trigger {

        public CommandTrigger(String name, String script) throws TriggerInitFailedException {
            super(name, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new CommandTrigger(triggerName, getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean hasCommandTrigger(String cmd) {
        return commandTriggerMap.containsKey(cmd);
    }

    public CommandTrigger getCommandTrigger(String cmd){
        return commandTriggerMap.get(cmd);
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
        } catch (TriggerInitFailedException e1) {
            plugin.handleException(adding, e1);
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

    public CommandTrigger createTempCommandTrigger(String script) throws TriggerInitFailedException {
        return new CommandTrigger("temp", script);
    }

    public AbstractCommandTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}