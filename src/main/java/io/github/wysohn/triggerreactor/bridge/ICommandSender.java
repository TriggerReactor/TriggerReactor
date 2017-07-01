package io.github.wysohn.triggerreactor.bridge;
public interface ICommandSender extends IMinecraftObject{
    public void sendMessage(String message);
    public boolean hasPermission(String permission);
}