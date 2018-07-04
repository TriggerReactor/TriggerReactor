package io.github.wysohn.triggerreactor.sponge.manager;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlayerLocationManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.sponge.bridge.event.SpongePlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.sponge.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.sponge.tools.LocationUtil;

public class PlayerLocationManager extends AbstractPlayerLocationManager {

    public PlayerLocationManager(TriggerReactor plugin) {
        super(plugin);
    }

    @Listener(order = Order.FIRST)
    public void onJoin(ClientConnectionEvent.Join e){
        Player player = e.getTargetEntity();
        Location<World> loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent e){
        Player player = e.getTargetEntity();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @Listener(order = Order.BEFORE_POST)
    public void onQuit(ClientConnectionEvent.Disconnect e){
        Player player = e.getTargetEntity();
        removeCurrentBlockLocation(player.getUniqueId());
    }

    @Listener(order = Order.FIRST)
    public void onMove(MoveEntityEvent e){
        Entity entity = e.getTargetEntity();
        if(!(entity instanceof Player))
            return;

        Player player = (Player) e.getTargetEntity();

        Transform<World> transformFrom = e.getFromTransform();
        Transform<World> transformTo = e.getToTransform();

        if(transformFrom.equals(transformTo))
            return;

        SimpleLocation from = getCurrentBlockLocation(player.getUniqueId());
        SimpleLocation to = LocationUtil.convertToSimpleLocation(transformTo.getLocation());

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
        onMove(new SpongePlayerBlockLocationEvent(pble));
        if(pble.isCancelled()){
            Location<World> loc = LocationUtil.convertToBukkitLocation(from);
            transformTo.setLocation(loc);
            e.setToTransform(transformTo);
        }
    }

    @Override
    public void reload() {
        for(Player player : Sponge.getServer().getOnlinePlayers()) {
            Location<World> loc = player.getLocation();
            SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
            setCurrentBlockLocation(player.getUniqueId(), sloc);
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

}
