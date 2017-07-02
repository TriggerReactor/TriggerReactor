package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class AbstractPlayerLocationManager extends Manager {
    private transient Map<UUID, SimpleLocation> locations = new ConcurrentHashMap<>();

    public AbstractPlayerLocationManager(TriggerReactor plugin) {
        super(plugin);
    }

    /**
     * Called when a player moved from one block to another.
     *
     * @param player
     *            the player moved
     * @param from
     *            block from
     * @param to
     *            block to
     * @return returns the location where the player should go back to. This is
     *         when the event is canceled by Custom Trigger or any other third
     *         party plugin; returns null if it's not cancelled.
     */
    protected abstract SimpleLocation onMove(IPlayer player, SimpleLocation from, SimpleLocation to);

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