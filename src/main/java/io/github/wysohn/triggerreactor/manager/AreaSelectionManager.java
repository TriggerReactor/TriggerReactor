package io.github.wysohn.triggerreactor.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.manager.trigger.AreaTriggerManager.Area;

public class AreaSelectionManager extends Manager implements Listener{
    private final Set<UUID> selecting = new HashSet<>();
    private final Map<UUID, SimpleLocation> leftPosition = new HashMap<>();
    private final Map<UUID, SimpleLocation> rightPosition = new HashMap<>();

    public AreaSelectionManager(TriggerReactor plugin) {
        super(plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        resetSelections(e.getPlayer());
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

        SimpleLocation sloc = new SimpleLocation(e.getClickedBlock().getLocation());

        if(e.getAction() == Action.LEFT_CLICK_BLOCK){
            leftPosition.put(uuid, sloc);
        }else if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            rightPosition.put(uuid, sloc);
        }

        SimpleLocation left = leftPosition.get(uuid);
        SimpleLocation right = rightPosition.get(uuid);
        if(left != null && right != null){
            if(!left.getWorld().equals(right.getWorld())){
                player.sendMessage(ChatColor.RED+"Positions have different world name.");
                return;
            }

            SimpleLocation smallest = getSmallest(left, right);
            SimpleLocation largest = getLargest(left, right);

            player.sendMessage(ChatColor.LIGHT_PURPLE+"Smallest: "+smallest+" , Largest: "+largest);
        } else if (left != null){
            player.sendMessage(ChatColor.GREEN+"Left ready");
        } else if (right != null){
            player.sendMessage(ChatColor.GREEN+"Right ready");
        }
    }

    private SimpleLocation getSmallest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(left.getWorld(),
                Math.min(left.getX(), right.getX()),
                Math.min(left.getY(), right.getY()),
                Math.min(left.getZ(), right.getZ()));
    }

    private SimpleLocation getLargest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(right.getWorld(),
                Math.max(left.getX(), right.getX()),
                Math.max(left.getY(), right.getY()),
                Math.max(left.getZ(), right.getZ()));
    }

    /**
     *
     * @param player
     * @return true if on; false if off
     */
    public boolean toggleSelection(Player player){
        if(selecting.contains(player.getUniqueId())){
            selecting.remove(player.getUniqueId());
            resetSelections(player);
            return false;
        }else{
            selecting.add(player.getUniqueId());
            return true;
        }
    }

    public void resetSelections(Player player){
        selecting.remove(player.getUniqueId());
        leftPosition.remove(player.getUniqueId());
        rightPosition.remove(player.getUniqueId());
    }

    /**
     *
     * @param player
     * @return null if invalid selection; Area if done (this Area's name is always null)
     */
    public Area getSelection(Player player){
        UUID uuid = player.getUniqueId();

        SimpleLocation left = leftPosition.get(uuid);
        SimpleLocation right = rightPosition.get(uuid);

        if(left != null && right != null){
            if(!left.getWorld().equals(right.getWorld())){
                player.sendMessage(ChatColor.RED+"Positions have different world name.");
                return null;
            }

            SimpleLocation smallest = getSmallest(left, right);
            SimpleLocation largest = getLargest(left, right);

            return new Area(smallest, largest);
        } else {
            return null;
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