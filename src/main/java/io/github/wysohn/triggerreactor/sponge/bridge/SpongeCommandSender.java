package io.github.wysohn.triggerreactor.sponge.bridge;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;

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
        sender.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpongeCommandSender other = (SpongeCommandSender) obj;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        return true;
    }


}
