package io.github.wysohn.triggerreactor.core.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager.Area;

public abstract class AbstractAreaSelectionManager extends Manager {
    protected final Set<UUID> selecting = new HashSet<>();
    protected final Map<UUID, SimpleLocation> leftPosition = new HashMap<>();
    protected final Map<UUID, SimpleLocation> rightPosition = new HashMap<>();

    public AbstractAreaSelectionManager(TriggerReactor plugin) {
        super(plugin);
    }

    protected static SimpleLocation getSmallest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(left.getWorld(),
                Math.min(left.getX(), right.getX()),
                Math.min(left.getY(), right.getY()),
                Math.min(left.getZ(), right.getZ()));
    }

    protected static SimpleLocation getLargest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(right.getWorld(),
                Math.max(left.getX(), right.getX()),
                Math.max(left.getY(), right.getY()),
                Math.max(left.getZ(), right.getZ()));
    }

    protected abstract ClickResult onClick(ClickAction action, UUID uuid, SimpleLocation sloc);

    /**
    *
    * @param player
    * @return true if on; false if off
    */
   public boolean toggleSelection(UUID uuid){
       if(selecting.contains(uuid)){
           selecting.remove(uuid);
           resetSelections(uuid);
           return false;
       }else{
           selecting.add(uuid);
           return true;
       }
   }

   public void resetSelections(UUID uuid){
       selecting.remove(uuid);
       leftPosition.remove(uuid);
       rightPosition.remove(uuid);
   }

   /**
    *
    * @param player
    * @return null if invalid selection; Area if done (this Area's name is always null)
    */
   public Area getSelection(UUID uuid){
       SimpleLocation left = leftPosition.get(uuid);
       SimpleLocation right = rightPosition.get(uuid);

       if(left != null && right != null){
           if(!left.getWorld().equals(right.getWorld())){
               return null;
           }

           SimpleLocation smallest = getSmallest(left, right);
           SimpleLocation largest = getLargest(left, right);

           return new Area(smallest, largest);
       } else {
           return null;
       }
   }

    public enum ClickAction{
        LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK;
    }

    public enum ClickResult{
        DIFFERENTWORLD, COMPLETE, LEFTSET, RIGHTSET;
    }
}