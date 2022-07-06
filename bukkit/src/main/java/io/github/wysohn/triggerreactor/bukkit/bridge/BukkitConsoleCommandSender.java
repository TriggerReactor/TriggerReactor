package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.core.bridge.IConsoleCommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class BukkitConsoleCommandSender implements IConsoleCommandSender {
    private final ConsoleCommandSender sender;
    public BukkitConsoleCommandSender(ConsoleCommandSender sender){
        super();
        this.sender = sender;
    }
    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public <T> T get() {
        return (T) sender;
    }
}
