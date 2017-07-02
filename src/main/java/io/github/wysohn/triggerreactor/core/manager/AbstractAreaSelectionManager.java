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

    /**
     * get the smallest point between two coordinates. Smallest means that the x, y, and z are all
     * the minimum value between two coordinates.
     * @param left coordinate 1
     * @param right coordinate 2
     * @return the smallest between two
     */
    protected static SimpleLocation getSmallest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(left.getWorld(),
                Math.min(left.getX(), right.getX()),
                Math.min(left.getY(), right.getY()),
                Math.min(left.getZ(), right.getZ()));
    }

    /**
     * get the largest point between two coordinates. Largest means that the x, y, and z are all
     * the maximum value between two coordinates.
     * @param left coordinate 1
     * @param right coordinate 2
     * @return the largest between two
     */
    protected static SimpleLocation getLargest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(right.getWorld(),
                Math.max(left.getX(), right.getX()),
                Math.max(left.getY(), right.getY()),
                Math.max(left.getZ(), right.getZ()));
    }

    /**
     * gets called when player clicks on a block.
     * <b>This should be called manually by the child class upon player interaction event.</b>
     * @param action the {@link ClickAction} associated with this player interaction.
     * @param uuid the uuid of player
     * @param sloc location where interaction occurred
     * @return the result as {@link ClickResult}
     */
    protected ClickResult onClick(ClickAction action, UUID uuid, SimpleLocation sloc) {
        if(action == ClickAction.LEFT_CLICK_BLOCK){
            leftPosition.put(uuid, sloc);
        }else if(action == ClickAction.RIGHT_CLICK_BLOCK){
            rightPosition.put(uuid, sloc);
        }

        SimpleLocation left = leftPosition.get(uuid);
        SimpleLocation right = rightPosition.get(uuid);
        if(left != null && right != null){
            if(!left.getWorld().equals(right.getWorld())){
                return ClickResult.DIFFERENTWORLD;
            }

            return ClickResult.COMPLETE;
        } else if (left != null){
            return ClickResult.LEFTSET;
        } else if (right != null){
            return ClickResult.RIGHTSET;
        } else {
            return null;
        }
    }

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
    * @return null if invalid selection; Area if done
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
        /**Left clicked on block**/LEFT_CLICK_BLOCK, /**Right clicked on block**/RIGHT_CLICK_BLOCK;
    }

    public enum ClickResult{
        /**When two selections are in different worlds**/DIFFERENTWORLD, /**Two coordinates are ready**/COMPLETE,
        /**Only left clicked coordinate is ready**/LEFTSET, /**Only right clicked coordinated is ready**/RIGHTSET;
    }
}