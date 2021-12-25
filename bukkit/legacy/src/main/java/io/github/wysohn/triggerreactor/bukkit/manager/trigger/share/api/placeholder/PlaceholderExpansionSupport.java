package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderExpansionSupport extends PlaceholderExpansion implements IVariablePlaceholder {
    private final ITriggerReactorAPI api;
    private final IVariablePlaceholder variablePlaceholder;

    public PlaceholderExpansionSupport(ITriggerReactorAPI api) {
        this.api = api;
        this.variablePlaceholder = new VariablePlaceholder(api);
    }

    @Override
    public String getIdentifier() {
        return variablePlaceholder.getIdentifier();
    }

    @Override
    public String getPlugin() {
        return variablePlaceholder.getPlugin();
    }

    @Override
    public String getAuthor() {
        return variablePlaceholder.getAuthor();
    }

    @Override
    public String getVersion() {
        return variablePlaceholder.getVersion();
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {
        return variablePlaceholder.onPlaceholderRequest(player, s);
    }
}
