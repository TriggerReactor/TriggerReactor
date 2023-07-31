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
package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitLocation;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BukkitPlayer extends BukkitEntity implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public <T> T get() {
        return (T) player;
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public IInventory getInventory() {
        return BukkitTriggerReactorCore.getWrapper().wrap(player.getInventory());
    }

    @Override
    public void openInventory(IInventory inventory) {
        Inventory inv = inventory.get();
        player.openInventory(inv);
    }

    @Override
    public SimpleChunkLocation getChunk() {
        return LocationUtil.convertToSimpleChunkLocation(player.getLocation().getChunk());
    }

    @SuppressWarnings("deprecation")
    @Override
    public IItemStack getItemInMainHand() {
        return new BukkitItemStack(player.getInventory().getItemInHand());
    }

    @Override
    public ILocation getLocation() {
        return new BukkitLocation(player.getLocation());
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.getInventory().setItemInHand(iS.get());
    }

}
