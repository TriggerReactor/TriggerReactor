package io.github.wysohn.triggerreactor.core.wrapper;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommonFunctions {
    private static final Random rand = new Random();

    public int random(Integer end){
        return rand.nextInt(end);
    }

    public int random(Integer start, Integer end){
        return start + rand.nextInt(end - start);
    }

    public boolean takeItem(Player player, Integer id, Integer amount){
        ItemStack IS = new ItemStack(id, amount);
        if(!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }
}
