/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import javax.inject.Inject;
import java.util.*;

public class AreaSelectionManager extends Manager {
    protected final Set<UUID> selecting = new HashSet<>();
    protected final Map<UUID, SimpleLocation> leftPosition = new HashMap<>();
    protected final Map<UUID, SimpleLocation> rightPosition = new HashMap<>();

    @Inject
    AreaSelectionManager() {
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

    @Override
    public void saveAll() {

    }

    /**
     * @return null if invalid selection; Area if done
     */
    public Area getSelection(UUID uuid) {
        SimpleLocation left = leftPosition.get(uuid);
        SimpleLocation right = rightPosition.get(uuid);

        if (left != null && right != null) {
            if (!left.getWorld().equals(right.getWorld())) {
                return null;
            }

            SimpleLocation smallest = SimpleLocation.getSmallest(left, right);
            SimpleLocation largest = SimpleLocation.getLargest(left, right);

            return new Area(smallest, largest);
        } else {
            return null;
        }
    }

    public boolean hasSelection(UUID uuid) {
        return selecting.contains(uuid);
    }

    public SimpleLocation getLeftPosition(UUID uuid) {
        return leftPosition.get(uuid);
    }

    public SimpleLocation getRightPosition(UUID uuid) {
        return rightPosition.get(uuid);
    }

    /**
     * gets called when player clicks on a block.
     * <b>This should be called manually by the child class upon player interaction event.</b>
     *
     * @param action the {@link ClickAction} associated with this player interaction.
     * @param uuid   the uuid of player
     * @param sloc   location where interaction occurred
     * @return the result as {@link ClickResult}
     */
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