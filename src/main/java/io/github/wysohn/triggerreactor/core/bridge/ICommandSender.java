package io.github.wysohn.triggerreactor.core.bridge;

public interface ICommandSender extends IMinecraftObject{
    public void sendMessage(String message);
    public boolean hasPermission(String permission);
    /**
     * hashCode() and equals() are needed for ScriptEditManager
     * @return
     */
    @Override
    int hashCode();
    /**
     * hashCode() and equals() are needed for ScriptEditManager
     * @return
     */
    @Override
    boolean equals(Object obj);
}