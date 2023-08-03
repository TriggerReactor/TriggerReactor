/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

@Singleton
public final class AreaSelectionManager extends Manager {
    protected final Set<UUID> selecting = new HashSet<>();
    protected final Map<UUID, SimpleLocation> leftPosition = new HashMap<>();
    protected final Map<UUID, SimpleLocation> rightPosition = new HashMap<>();

    @Inject
    private AreaSelectionManager() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    public boolean isSelecting(UUID uuid) {
        return selecting.contains(uuid);
    }

    public SimpleLocation getLeftPosition(UUID uuid) {
        return leftPosition.get(uuid);
    }

    public SimpleLocation getRightPosition(UUID uuid) {
        return rightPosition.get(uuid);
    }

    /**
     * Handle interaction events on blocks for area selection.
     *
     * @param player        the player who interacted with the block
     * @param leftHandClick true if the player clicked with left hand. This is something
     *                      introduced after MC 1.8 that every interaction is considered
     *                      for both left and right hand, so this is needed to determine
     *                      which hand the player clicked with. If the MC version is lower
     *                      than 1.8, then there is only one hand, so this value is not so relevant.
     * @param eventCanceler callback that directly cancels the cancellable event.
     * @param clickedBlock  the block that the player clicked
     * @param action        the action that the player did on the block
     * @deprecated This is event handler. Must only be called from listeners or tests.
     */
    @Deprecated
    public void onInteract(IPlayer player,
                           boolean leftHandClick,
                           Consumer<Boolean> eventCanceler,
                           IBlock clickedBlock,
                           AreaSelectionManager.ClickAction action) {
        UUID uuid = player.getUniqueId();
        if (!isSelecting(uuid))
            return;

        eventCanceler.accept(true);
        if (!leftHandClick)
            return;

        SimpleLocation sloc = clickedBlock.getLocation().toSimpleLocation();

        AreaSelectionManager.ClickResult result = null;
        if (action != null)
            result = onClick(action, uuid, sloc);

        if (result != null) {
            switch (result) {
                case DIFFERENTWORLD:
                    player.sendMessage("&cPositions have different world name.");
                    break;
                case COMPLETE:
                    SimpleLocation left = getLeftPosition(uuid);
                    SimpleLocation right = getRightPosition(uuid);

                    SimpleLocation smallest = AreaSelectionManager.getSmallest(left, right);
                    SimpleLocation largest = AreaSelectionManager.getLargest(left, right);

                    player.sendMessage("&dSmallest: " + smallest + " , Largest: " + largest);
                    break;
                case LEFTSET:
                    player.sendMessage("&aLeft ready");
                    break;
                case RIGHTSET:
                    player.sendMessage("&aRight ready");
                    break;
            }
        }
    }

    /**
     * get the smallest point between two coordinates. Smallest means that the x, y, and z are all
     * the minimum value between two coordinates.
     *
     * @param left  coordinate 1
     * @param right coordinate 2
     * @return the smallest between two
     */
    public static SimpleLocation getSmallest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(left.getWorld(),
                                  Math.min(left.getX(), right.getX()),
                                  Math.min(left.getY(), right.getY()),
                                  Math.min(left.getZ(), right.getZ()));
    }

    /**
     * get the largest point between two coordinates. Largest means that the x, y, and z are all
     * the maximum value between two coordinates.
     *
     * @param left  coordinate 1
     * @param right coordinate 2
     * @return the largest between two
     */
    public static SimpleLocation getLargest(SimpleLocation left, SimpleLocation right) {
        return new SimpleLocation(right.getWorld(),
                                  Math.max(left.getX(), right.getX()),
                                  Math.max(left.getY(), right.getY()),
                                  Math.max(left.getZ(), right.getZ()));
    }

    /**
     * gets called when player clicks on a block.
     * <b>This should be called manually by the child class upon player interaction event.</b>
     *
     * @param action the {@link ClickAction} associated with this player interaction.
     * @param uuid   the uuid of player
     * @param sloc   location where interaction occurred
     * @return the result as {@link ClickResult}
     * @deprecated Event handler. Must only be used by listeners or tests.
     */
    @Deprecated
    public ClickResult onClick(ClickAction action, UUID uuid, SimpleLocation sloc) {
        if (action == ClickAction.LEFT_CLICK_BLOCK) {
            leftPosition.put(uuid, sloc);
        } else if (action == ClickAction.RIGHT_CLICK_BLOCK) {
            rightPosition.put(uuid, sloc);
        }

        SimpleLocation left = leftPosition.get(uuid);
        SimpleLocation right = rightPosition.get(uuid);
        if (left != null && right != null) {
            if (!left.getWorld().equals(right.getWorld())) {
                return ClickResult.DIFFERENTWORLD;
            }

            return ClickResult.COMPLETE;
        } else if (left != null) {
            return ClickResult.LEFTSET;
        } else if (right != null) {
            return ClickResult.RIGHTSET;
        } else {
            return null;
        }
    }

    /**
     * @param uuid
     * @return true if on; false if off
     */
    public boolean toggleSelection(UUID uuid) {
        if (selecting.contains(uuid)) {
            selecting.remove(uuid);
            resetSelections(uuid);
            return false;
        } else {
            selecting.add(uuid);
            return true;
        }
    }

    public void resetSelections(UUID uuid) {
        selecting.remove(uuid);
        leftPosition.remove(uuid);
        rightPosition.remove(uuid);
    }

    /**
     * @param uuid
     * @return null if invalid selection; Area if done
     */
    public Area getSelection(UUID uuid) {
        SimpleLocation left = leftPosition.get(uuid);
        SimpleLocation right = rightPosition.get(uuid);

        if (left != null && right != null) {
            if (!left.getWorld().equals(right.getWorld())) {
                return null;
            }

            SimpleLocation smallest = getSmallest(left, right);
            SimpleLocation largest = getLargest(left, right);

            return new Area(smallest, largest);
        } else {
            return null;
        }
    }

    public enum ClickAction {
        /**
         * Left clicked on block
         **/
        LEFT_CLICK_BLOCK,
        /**
         * Right clicked on block
         **/
        RIGHT_CLICK_BLOCK
    }

    public enum ClickResult {
        /**
         * When two selections are in different worlds
         **/
        DIFFERENTWORLD,
        /**
         * Two coordinates are ready
         **/
        COMPLETE,
        /**
         * Only left clicked coordinate is ready
         **/
        LEFTSET,
        /**
         * Only right clicked coordinated is ready
         **/
        RIGHTSET
    }
}