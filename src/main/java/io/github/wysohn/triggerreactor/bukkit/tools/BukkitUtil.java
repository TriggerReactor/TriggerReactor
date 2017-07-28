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
