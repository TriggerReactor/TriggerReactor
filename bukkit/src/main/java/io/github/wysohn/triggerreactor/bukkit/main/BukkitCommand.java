package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandExecutor;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import org.bukkit.command.PluginCommand;

import java.util.Optional;

public class BukkitCommand implements ICommand {
    private final PluginCommand command;

    public BukkitCommand(PluginCommand command) {
        this.command = command;
    }

    @Override
    public void setTabCompleters(ITabCompleter[] tabCompleters) {
        command.setTabCompleter((sender, command12, alias, args) -> {
            ITabCompleter tabCompleter = Optional.ofNullable(tabCompleters)
                    .filter(iTabCompleters -> iTabCompleters.length >= args.length)
                    .map(iTabCompleters -> iTabCompleters[args.length - 1])
                    .orElse(ITabCompleter.Builder.of().build());

            String partial = args[args.length - 1];
            if (partial.length() < 1) { // show hint if nothing is entered yet
                return tabCompleter.getHint();
            } else {
                return tabCompleter.getCandidates(partial);
            }
        });
    }

    @Override
    public void setExecutor(ICommandExecutor executor) {
        command.setExecutor((sender, command1, label, args) -> {
            executor.execute(new BukkitCommandSender(sender), label, args);
            return true;
        });
    }
}
