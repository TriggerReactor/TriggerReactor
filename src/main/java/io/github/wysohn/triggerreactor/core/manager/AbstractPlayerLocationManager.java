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

    protected abstract SimpleLocation onMove(IPlayer player, SimpleLocation from, SimpleLocation to);

    public SimpleLocation getCurrentBlockLocation(UUID uuid){
        return locations.get(uuid);
    }

    protected void setCurrentBlockLocation(UUID uuid, SimpleLocation sloc) {
        locations.put(uuid, sloc);
    }

    protected void removeCurrentBlockLocation(UUID uuid) {
        locations.remove(uuid);
    }
}