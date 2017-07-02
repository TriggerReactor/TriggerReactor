package io.github.wysohn.triggerreactor.core.manager.trigger.share.api;

import java.lang.reflect.Constructor;
import java.util.Map;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

/**
 * Abstract representation of API support. The child classes must have at least one constructor with one argument,
 *  {@link io.github.wysohn.triggerreactor.core.main.TriggerReactor}, to make it work properly.
 * @author wysohn
 *
 */
public class AbstractAPISupport {

    protected final TriggerReactor plugin;

    public AbstractAPISupport(TriggerReactor plugin) {
        super();
        this.plugin = plugin;
    }

    /**
     * Try to add a new instance of type 'clazz' to 'varName' if the 'varName' doesn't exist already.
     * @param sharedVars the map to save variables
     * @param varName the name of the variable
     * @param clazz the class that will be instanciated.
     */
    public static void addSharedVar(Map<String, AbstractAPISupport> sharedVars, String varName, Class<? extends AbstractAPISupport> clazz){
        if(!sharedVars.containsKey(varName)){
            Constructor con = null;
            try {
                con = clazz.getConstructor(TriggerReactor.class);
            } catch (NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }

            boolean initSuccess = true;
            APISupport api = null;
            try {
                api = (APISupport) con.newInstance(TriggerReactor.getInstance());
                api.init();
            } catch(APISupportException e){
                initSuccess = false;
            } catch (Exception e) {
                initSuccess = false;
                e.printStackTrace();
            } finally {
                if(api != null && initSuccess)
                    sharedVars.put(varName, api);
            }
        }
    }
}