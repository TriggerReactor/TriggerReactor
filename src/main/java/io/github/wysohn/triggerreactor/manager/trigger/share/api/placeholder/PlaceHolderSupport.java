package io.github.wysohn.triggerreactor.manager.trigger.share.api.placeholder;

import org.bukkit.entity.Player;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupport;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceHolderSupport extends APISupport {

    public PlaceHolderSupport(TriggerReactor plugin) {
        super(plugin, "PlaceholderAPI");
    }

    /**
     * Translate placeholders to actual string.
     * @param player
     * @param string string before the translation
     * @return translated string
     */
    public String parse(Player player, String string){
        return PlaceholderAPI.setPlaceholders(player, string);
    }
}
