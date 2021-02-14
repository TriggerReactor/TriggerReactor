package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import org.bukkit.entity.Player;

public interface IVariablePlaceholder {
    String onPlaceholderRequest(Player player, String identifier);
}
