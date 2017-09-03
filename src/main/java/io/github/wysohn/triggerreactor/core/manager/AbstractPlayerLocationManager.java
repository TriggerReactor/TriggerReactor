package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class AbstractPlayerLocationManager extends Manager {
    private transient Map<UUID, SimpleLocation> locations = new ConcurrentHashMap<>();

    public AbstractPlayerLocationManager(TriggerReactor plugin) {
        super(plugin);
    }

    /**
     * Called when a player moved from one block to another.
     * <b>The child class should call this method manually when a player moved from a block to another block.</b>
     * @param player
     *            the player moved
     * @param from
     *            block from
     * @param to
     *            block to
     */
    protected void onMove(IPlayerBlockLocationEvent event) {
        if(event.getFrom().equals(event.getTo()))
            return;

        plugin.callEvent(event);
        if(event.isCancelled()){
            event.setCancelled(true);
        } else {
            setCurrentBlockLocation(event.getIPlayer().getUniqueId(), event.getTo());
        }
    }

    /**
     * get location of player
     * @param uuid uuid of player
     * @return the location. If the player just logged in, it might be null.
     */
    public SimpleLocation getCurrentBlockLocation(UUID uuid){
        return locations.get(uuid);
    }

    /**
     * set current location of the player
     * @param uuid the player's uuid
     * @param sloc the location where player is at
     */
    protected void setCurrentBlockLocation(UUID uuid, SimpleLocation sloc) {
        locations.put(uuid, sloc);
    }

    /**
     * remove the current location of the player.
     * @param uuid the player's uuid
     */
    protected void removeCurrentBlockLocation(UUID uuid) {
        locations.remove(uuid);
    }
}