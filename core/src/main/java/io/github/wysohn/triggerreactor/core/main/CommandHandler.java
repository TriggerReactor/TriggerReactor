package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class CommandHandler {
    @Inject
    @Named("CommandName")
    String commandName;
    @Inject
    @Named("Permission")
    String permission;
    @Inject
    ITriggerCommand triggerCommand;
    @Inject
    IPluginLifecycleController pluginLifecycleController;

    @Inject
    CommandHandler() {

    }

    public boolean onCommand(ICommandSender sender, String command, String[] args) {
        if (command.equalsIgnoreCase(commandName)) {
            if (!sender.hasPermission(permission))
                return true;

            if (!pluginLifecycleController.isEnabled()) {
                sender.sendMessage("&cTriggerReactor is disabled. Check your latest.log to see why the plugin is not "
                        + "loaded properly. If there was an error while loading, please report it through github "
                        + "issue or our discord channel.");
                return true;
            }

            Queue<String> commandQueue = ITriggerCommand.toQueue(args);
            triggerCommand.onCommand(sender, commandQueue);
        }

        return true;
    }

    /**
     * @param args
     * @param indexFrom inclusive
     * @param indexTo   inclusive
     * @return
     */
    private String mergeArguments(String[] args, int indexFrom, int indexTo) {
        StringBuilder builder = new StringBuilder(args[indexFrom]);
        for (int i = indexFrom + 1; i <= indexTo; i++) {
            builder.append(" " + args[i]);
        }
        return builder.toString();
    }

    //only for /trg command
    public List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (!sender.hasPermission(permission))
            return Collections.singletonList("permission denied.");

        List<String> completions = Arrays.stream(args).collect(Collectors.toList());
        return triggerCommand.onTab(completions.listIterator());
    }
}
