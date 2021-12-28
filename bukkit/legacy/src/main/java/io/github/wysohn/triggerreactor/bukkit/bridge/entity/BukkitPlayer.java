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
package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitLocation;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BukkitPlayer extends BukkitEntity implements IPlayer {
    private final Player player;

    public BukkitPlayer(IWrapper wrapper, Player player) {
        super(wrapper, player);
        this.player = player;
    }

    @Override
    public <T> T get() {
        return (T) player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message, boolean raw) {
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        if(raw)
            player.sendRawMessage(colored);
        else
            player.sendMessage(colored);
    }

    @Override
    public SimpleChunkLocation getChunk() {
        return LocationUtil.convertToSimpleChunkLocation(player.getLocation().getChunk());
    }

    @Override
    public IInventory getInventory() {
        return wrapper.wrap(player.getInventory());
    }

    @SuppressWarnings("deprecation")
    @Override
    public IItemStack getItemInMainHand() {
        return new BukkitItemStack(player.getInventory().getItemInHand());
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.getInventory().setItemInHand(iS.get());
    }

    @Override
    public ILocation getLocation() {
        return new BukkitLocation(player.getLocation());
    }

    @Override
    public void openInventory(IInventory inventory) {
        Inventory inv = inventory.get();
        player.openInventory(inv);
    }

    @Override
    public boolean isOp() {
        return player.isOp();
    }

    @Override
    public void setOp(boolean bool) {
        player.setOp(bool);
    }

    @Override
    public void dispatchCommand(String command) {
        player.performCommand(command);
    }
}
