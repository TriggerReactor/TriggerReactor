package io.github.wysohn.triggerreactor.sponge.bridge;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageReceiver;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.sponge.util.ChatColorUtil;

public class SpongeCommandSender implements ICommandSender {
    private final MessageReceiver sender;

    public SpongeCommandSender(MessageReceiver sender) {
        super();
        this.sender = sender;
    }

    @Override
    public <T> T get() {
        return (T) sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(ChatColorUtil.translateColorcodes(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        //TODO: not sure if it is safe to assume all players will be child of Player
        if(sender instanceof Player){
            return ((Player) sender).hasPermission(permission);
        }else{
            return true;
        }
    }

}
