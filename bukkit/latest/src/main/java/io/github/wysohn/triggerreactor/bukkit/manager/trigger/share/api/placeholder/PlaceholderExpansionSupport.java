package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderExpansionSupport extends PlaceholderExpansion{
    private TriggerReactor plugin;
    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public PlaceholderExpansionSupport(TriggerReactor plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
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
    public String getIdentifier(){
        return "tr";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A Player who performed the task which contains interaction on
     *         PlaceholderAPI.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    private io.github.wysohn.triggerreactor.core.main.TriggerReactor pl;
    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        // %tr_version% -> this should return TR version, but should use PluginDescription which is modified as protected method.

        /*

        if(identifier.equals("version")){
            return plugin.getConfig().getString("version", plugin.getPluginDescription());
        }

        */

        // %tr_<variable name>%
        String variableName = identifier;
        io.github.wysohn.triggerreactor.core.main.TriggerReactor plg = pl.getInstance();
        AbstractVariableManager vm = plg.getVariableManager();
        Object value = vm.get(variableName);
        if(value == null) {
            return "";
        }
        if(value instanceof Number){
            value =  String.valueOf(value);
        }

        if(!(value instanceof String)){
            return "";
        }else{
            String output = (String) value;
            return output;
        }




        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
    }
}
