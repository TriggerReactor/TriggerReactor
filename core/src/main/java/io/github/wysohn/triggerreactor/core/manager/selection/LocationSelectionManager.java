package io.github.wysohn.triggerreactor.core.manager.selection;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@ManagerScope
public class LocationSelectionManager extends Manager {
    @Inject
    @Named("Permission")
    String permission;

    private final Map<UUID, Function<SimpleLocation, Boolean>> locationConsumerMap = new HashMap<>();

    @Inject
    LocationSelectionManager(){

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() throws RuntimeException {

    }

    /**
     * @param clicked location clicked by player
     * @param player  player
     * @param type    click type
     * @return true to allow the click event; false to cancel
     */
    public boolean onClick(SimpleLocation clicked, IPlayer player, ClickType type) {
        switch (type) {
            case LEFT_CLICK:
            case RIGHT_CLICK:
                // finish the trigger setting
                if (hasStarted(player)) {
                    // cancel selection
                    if(player.isSneaking()){
                        stopLocationSet(player);
                        return false;
                    }

                    boolean done = true;
                    try{
                        done = locationConsumerMap.get(player.getUniqueId()).apply(clicked);
                    } finally {
                        if(done)
                            stopLocationSet(player);
                    }

                    return false;
                }

                // otherwise, just normal interaction
                return true;
            default:
                return true;
        }
    }

    /**
     * Begin location setting. After invoking this method, the player is now set
     * to 'location setting mode,' so when he/she clicks a block, it will be provided
     * to the callback function.
     * @param player player
     * @param consumer callback function. return true to signal that it is done; false if
     *                 the selection is invalid so the player need to try again. To avoid
     *                 player getting stuck at setting mode, any exception thrown by the callback
     *                 function will immediately turn off the setting mode.
     * @return
     */
    public boolean startLocationSet(IPlayer player, Function<SimpleLocation, Boolean> consumer) {
        if (locationConsumerMap.containsKey(player.getUniqueId()))
            return false;

        locationConsumerMap.put(player.getUniqueId(), consumer);

        return true;
    }

    public boolean stopLocationSet(IPlayer player) {
        if (!locationConsumerMap.containsKey(player.getUniqueId()))
            return false;

        locationConsumerMap.remove(player.getUniqueId());

        return true;
    }

    /**
     * Check if the player has started and in setting mode (which can be
     * done by {@link #startLocationSet(IPlayer, Function)}).
     * @param player player
     * @return true if setting mode enabled; false otherwise.
     */
    public boolean hasStarted(IPlayer player) {
        return locationConsumerMap.containsKey(player.getUniqueId());
    }

}
