package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import org.bukkit.entity.Player;

public interface IVariablePlaceholder {

    String onPlaceholderRequest(Player player, String identifier);

    default String getAuthor() {
        return "Professor_Snape";
    }

    default String getIdentifier() {
        return "tr";
    }

    default String getPlugin() {
        return "TriggerReactor";
    }

    default String getVersion() {
        return "1.0.0";
    }
}
