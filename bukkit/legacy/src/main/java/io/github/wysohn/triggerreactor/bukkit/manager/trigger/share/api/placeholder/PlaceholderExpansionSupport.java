package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderExpansionSupport extends PlaceholderExpansion implements IVariablePlaceholder {
    private final TriggerReactorCore plugin;
    private final IVariablePlaceholder variablePlaceholder;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin The instance of our plugin.
     */
    public PlaceholderExpansionSupport(TriggerReactorCore plugin) {
        this.plugin = plugin;
        this.variablePlaceholder = new VariablePlaceholder(plugin);
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

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        return plugin.getAuthor();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "tr";
    }

    @Override
    public String getPlugin() {
        return null;
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return plugin.getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {
        return variablePlaceholder.onPlaceholderRequest(player, s);
    }
}
