package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import org.bukkit.entity.Player;

public class BukkitWrapper extends AbstractBukkitWrapper {
    public BukkitWrapper() {

    }

    @Override
    public IPlayer wrap(Player player) {
        return new BukkitPlayer(this, player);
    }
}
