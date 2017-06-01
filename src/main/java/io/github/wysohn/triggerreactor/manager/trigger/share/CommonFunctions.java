/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.manager.trigger.share;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.wysohn.triggerreactor.core.wrapper.SelfReference;

public class CommonFunctions implements SelfReference {
    private static final Random rand = new Random();

    /**
     * get a random integer value between 0 to end
     * @param end exclusive
     * @return
     */
    public int random(Integer end){
        return rand.nextInt(end);
    }

    /**
     * get a random integer value between start to end
     * @param start inclusive
     * @param end exclusive
     * @return
     */
    public int random(Integer start, Integer end){
        return start + rand.nextInt(end - start);
    }

    /**
     * take item from player.
     * @param player target player
     * @param id item id
     * @param amount amount
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, Integer id, Integer amount){
        ItemStack IS = new ItemStack(id, amount);
        if(!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }

    /**
     * take item from player.
     * @param player target player
     * @param id item id
     * @param amount amount
     * @param data data of item
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, Integer id, Integer amount, short data){
        ItemStack IS = new ItemStack(id, amount, data);
        if(!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }

    /**
     * check if two location are equal not considering their decimal points
     * @param loc1
     * @param loc2
     * @return true if equal; false if not
     */
    public boolean locationEqual(Location loc1, Location loc2){
        return loc1.getWorld() == loc2.getWorld()
                && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * matches the str by using regular expression regex
     * @param str
     * @param regex
     * @return true if str matches with regex; false if not
     */
    public boolean matches(String str, String regex){
        return str.matches(regex);
    }

    /**
     * get list of online players online
     * @return player iterator
     */
    public Collection<? extends Player> getPlayers(){
        return Bukkit.getOnlinePlayers();
    }

    public Object[] array(Integer size){
        return new Object[size];
    }
}
