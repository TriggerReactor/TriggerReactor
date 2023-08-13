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
package io.github.wysohn.triggerreactor.bukkit.tools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class BukkitUtil {
    private static boolean getOnlinePlayersMethodFound = true;

    /**
     * Get list of players. For old version like 1.5.2, the return value of Bukkit.getOnlinePlayers() is array
     * instead of Collection. For that case, this method automatically create a new Collection of players.
     * If it's newer version, Bukkit.getOnlinePlayers() simply return Collection of players already.
     *
     * @return Collection of players.
     */
    public static Collection<? extends Player> getOnlinePlayers() {
        if (!getOnlinePlayersMethodFound) {
            return Bukkit.getOnlinePlayers();
        } else {
            try {
                Method method = Bukkit.class.getDeclaredMethod("getOnlinePlayers");

                method.setAccessible(true);
                Object out = method.invoke(null);

                if (out.getClass().isArray()) {
                    Collection<Player> players = new ArrayList<>();
                    for (int i = 0; i < Array.getLength(out); i++) {
                        players.add((Player) Array.get(out, i));
                    }
                    return players;
                } else {
                    return (Collection<? extends Player>) out;
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                getOnlinePlayersMethodFound = false;
                e.printStackTrace();
            }

            return Bukkit.getOnlinePlayers();
        }
    }

    private static boolean getHandMethodFound = true;

    /**
     * Check if the provided PlayerInteractEvent is caused by main hand. For the old versions, there was no concept of
     * 'using both hand,' so it will return true always. If it is new version, It will check to see if the left hand
     * is involved in this interaction event.
     *
     * @param e PlayerInteractEvent to check
     * @return true if left hand; false otherwise.
     */
    public static boolean isLeftHandClick(PlayerInteractEvent e) {
        if (!getHandMethodFound) { // if method does not exists, it is old version.
            return true; //always left hand in old versions
        } else {
            try {
                Method method = e.getClass().getMethod("getHand");

                method.setAccessible(true);
                Object out = method.invoke(e);

                //how is it possible ?_?
                if (out == null)
                    return false; // just process it as noo left hand click

                Class<?> clazz = Class.forName("org.bukkit.inventory.EquipmentSlot");
                if (!clazz.isEnum()) {//This is not likely the case but just for safety
                    getHandMethodFound = false;
                    return true;
                }

                Object enumHand = Enum.valueOf((Class<? extends Enum>) clazz, "HAND");

                return out.equals(enumHand);
            } catch (NoSuchMethodException ex) {
                getHandMethodFound = false;
                return true;
            } catch (Exception ex) {
                getHandMethodFound = false;
                ex.printStackTrace();
                return true;
            }
        }
    }

    public static ItemStack getPlayerHeadItem() {
        try {
            return new ItemStack(Material.valueOf("PLAYER_HEAD"), 1, (short) 3);
        } catch (IllegalArgumentException ex) {
            return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }
    }
}
