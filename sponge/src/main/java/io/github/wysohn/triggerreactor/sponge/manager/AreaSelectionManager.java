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
package io.github.wysohn.triggerreactor.sponge.manager;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.sponge.tools.LocationUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.UUID;

public class AreaSelectionManager extends AbstractAreaSelectionManager {
    public AreaSelectionManager(TriggerReactorCore plugin) {
        super(plugin);
    }

    @Listener
    public void onInteract(InteractBlockEvent.Primary.MainHand e) {
        onInteract(e, true);
    }

    public void onInteract(InteractBlockEvent e, boolean leftClick) {
        Player player = e.getCause().first(Player.class).orElse(null);
        if (player == null)
            return;

        UUID uuid = player.getUniqueId();

        if (!selecting.contains(uuid))
            return;

        e.setCancelled(true);

        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getTargetBlock().getLocation().orElse(null));

        ClickResult result = null;
        if (leftClick) {
            result = onClick(ClickType.LEFT_CLICK, uuid, sloc);
        } else {
            result = onClick(ClickType.RIGHT_CLICK, uuid, sloc);
        }

        if (result != null) {
            switch (result) {
                case DIFFERENTWORLD:
                    player.sendMessage(
                            Text.builder("Positions have different world name.").color(TextColors.RED).build());
                    break;
                case COMPLETE:
                    SimpleLocation left = leftPosition.get(uuid);
                    SimpleLocation right = rightPosition.get(uuid);

                    SimpleLocation smallest = getSmallest(left, right);
                    SimpleLocation largest = getLargest(left, right);

                    player.sendMessage(Text.builder("Smallest: " + smallest + " , Largest: " + largest)
                            .color(TextColors.LIGHT_PURPLE)
                            .build());
                    break;
                case LEFTSET:
                    player.sendMessage(Text.builder("Left ready").color(TextColors.GREEN).build());
                    break;
                case RIGHTSET:
                    player.sendMessage(Text.builder("Right ready").color(TextColors.GREEN).build());
                    break;
            }
        }
    }

    @Listener
    public void onInteract(InteractBlockEvent.Secondary.MainHand e) {
        onInteract(e, false);
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect e) {
        resetSelections(e.getTargetEntity().getUniqueId());
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