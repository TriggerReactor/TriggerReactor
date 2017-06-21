package io.github.wysohn.triggerreactor.manager.trigger.share.api.coreprotect;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupportException;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class CoreprotectSupport extends APISupport {
    /**
     * I'm just too lazy to add all those methods. Please use this to access directly with api.
     * http://minerealm.com/community/viewtopic.php?f=32&t=16534
     */
    private CoreProtectAPI api;
    public CoreprotectSupport(TriggerReactor plugin) {
        super(plugin, "CoreProtect");
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        if(CoreProtect.getInstance().getAPI().APIVersion() < 4){
            plugin.getLogger().warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }

        api = CoreProtect.getInstance().getAPI();
    }
}
