package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandExecutor;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.List;

public class BukkitCommand implements ICommand {
    private final PluginCommand command;

    public BukkitCommand(PluginCommand command) {
        this.command = command;
    }

    @Override
    public void setAliases(List<String> collect) {

    }

    @Override
    public void setTabCompleter(ITabCompleter[] tabCompleters) {
        command.setTabCompleter((sender, command12, alias, args) -> {
            int index = args.length - 1;
            if (index >= tabCompleters.length)
                return new ArrayList<>();

            ITabCompleter tabCompleter = tabCompleters[index];
            String partial = args[index];
            return tabCompleter.getCandidates(partial);
        });
    }

    @Override
    public void setExecutor(ICommandExecutor executor) {
        command.setExecutor(
                (sender, c, label, args) -> executor.onCommand(new BukkitCommandSender(sender), this, label, args));
    }
}
