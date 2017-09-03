package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class CoreprotectSupport extends APISupport {
    /**
     * I'm just too lazy to add all those methods. Please use this to access directly with api.
     * http://minerealm.com/community/viewtopic.php?f=32&t=16534
     */
    protected CoreProtectAPI api;
    public CoreprotectSupport(TriggerReactor plugin) {
        super(plugin, "CoreProtect");
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        api = CoreProtect.getInstance().getAPI();

        try{
            api.getClass().getMethod("APIVersion");
        }catch(NoSuchMethodException e){
            plugin.getLogger().warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }

        if(api.APIVersion() < 4){
            plugin.getLogger().warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }
    }
}
