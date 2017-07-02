package io.github.wysohn.triggerreactor.bukkit.manager;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.util.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;

public class AreaSelectionManager extends AbstractAreaSelectionManager implements Listener{
    public AreaSelectionManager(TriggerReactor plugin) {
        super(plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        resetSelections(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!selecting.contains(uuid))
            return;

        e.setCancelled(true);

        if(e.getHand() != EquipmentSlot.HAND)
            return;

        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getClickedBlock().getLocation());

        ClickResult result = null;
        if(e.getAction() == Action.LEFT_CLICK_BLOCK){
            result = onClick(ClickAction.LEFT_CLICK_BLOCK, uuid, sloc);
        }else if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            result = onClick(ClickAction.RIGHT_CLICK_BLOCK, uuid, sloc);
        }

        if(result != null){
            switch(result){
            case DIFFERENTWORLD:
                player.sendMessage(ChatColor.RED+"Positions have different world name.");
                break;
            case COMPLETE:
                SimpleLocation left = leftPosition.get(uuid);
                SimpleLocation right = rightPosition.get(uuid);

                SimpleLocation smallest = getSmallest(left, right);
                SimpleLocation largest = getLargest(left, right);

                player.sendMessage(ChatColor.LIGHT_PURPLE+"Smallest: "+smallest+" , Largest: "+largest);
                break;
            case LEFTSET:
                player.sendMessage(ChatColor.GREEN+"Left ready");
                break;
            case RIGHTSET:
                player.sendMessage(ChatColor.GREEN+"Right ready");
                break;
            }
        }
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }
}