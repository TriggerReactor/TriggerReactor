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
package io.github.wysohn.triggerreactor.sponge.bridge.entity;

import com.flowpowered.math.vector.Vector3i;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeItemStack;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeLocation;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

public class SpongePlayer extends SpongeEntity implements IPlayer {
    private final Player player;

    public SpongePlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public <T> T get() {
        return (T) player;
    }

    @Override
    public SimpleChunkLocation getChunk() {
        World world = player.getLocation().getExtent();
        Vector3i vector = player.getLocation().getChunkPosition();
        return new SimpleChunkLocation(world.getName(), vector.getX(), vector.getZ());
    }

    @Override
    public IInventory getInventory() {
        return new SpongeInventory(player.getInventory(), player.getInventory().getCarrier().get());
    }

    @Override
    public IItemStack getItemInMainHand() {
        return new SpongeItemStack(player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty()));
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.setItemInHand(HandTypes.MAIN_HAND, iS.get());
    }

    @Override
    public ILocation getLocation() {
        return new SpongeLocation(player.getLocation());
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void openInventory(IInventory inventory) {
        player.openInventory(inventory.get()).orElse(null);
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
    }

}
