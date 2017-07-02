package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

/**
 * This class is responsible for firing IPlayerPermissionCheckEvent. This might be
 * only for Bukkit API.
 * @author wysohn
 *
 */
public abstract class AbstractPermissionManager extends Manager {

    public AbstractPermissionManager(TriggerReactor plugin) {
        super(plugin);
    }

}