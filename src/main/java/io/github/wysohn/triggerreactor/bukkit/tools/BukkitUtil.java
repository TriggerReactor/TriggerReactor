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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitUtil {
    private static boolean methodFound = true;
    public static Collection<? extends Player> getOnlinePlayers(){
        if(!methodFound){
            return Bukkit.getOnlinePlayers();
        }else{
            try {
                Method method = Bukkit.class.getDeclaredMethod("getOnlinePlayers");

                method.setAccessible(true);
                Object out = method.invoke(null);

                if(out.getClass().isArray()){
                    Collection<Player> players = new ArrayList<>();
                    for(int i = 0; i < Array.getLength(out); i++){
                        players.add((Player) Array.get(out, i));
                    }
                    return players;
                } else {
                    return (Collection<? extends Player>) out;
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                methodFound = false;
                e.printStackTrace();
            }

            return Bukkit.getOnlinePlayers();
        }
    }
}
