package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;

public class BukkitCommandSender implements ICommandSender {
    private final org.bukkit.command.CommandSender sender;

    public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
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
