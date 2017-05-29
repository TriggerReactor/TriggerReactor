package io.github.wysohn.triggerreactor.manager.trigger.share.api.vault;

import org.bukkit.entity.Player;

public interface IVaultSupport {
    public boolean has(Player player, Double amount);
    public boolean give(Player player, Double amount);
    public boolean take(Player player, Double amount);
    public boolean set(Player player, Double amount);
    public double balance(Player player);

}
