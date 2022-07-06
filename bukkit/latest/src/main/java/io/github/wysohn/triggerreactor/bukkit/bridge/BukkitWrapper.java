package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.bridge.IConsoleCommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class BukkitWrapper extends AbstractBukkitWrapper {

    @Override
    public IPlayer wrap(Player player) {
        return new BukkitPlayer(player);
    }

    @Override
    public IConsoleCommandSender wrap(ConsoleCommandSender sender) {return new BukkitConsoleCommandSender(sender);}

}
