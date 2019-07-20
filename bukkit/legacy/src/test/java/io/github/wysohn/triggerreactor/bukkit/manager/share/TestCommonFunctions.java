package io.github.wysohn.triggerreactor.bukkit.manager.share;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.AbstractTestCommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;

public class TestCommonFunctions extends AbstractTestCommonFunctions {
    public TestCommonFunctions(CommonFunctions fn) {
        super(fn);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { {new CommonFunctions(null)} });
    }

    @Override
    protected boolean isSimilar(ItemStack IS1, ItemStack IS2) {
        return IS1.getType() == IS2.getType()
                && IS1.getDurability() == IS2.getDurability();
    }

    @Test
    public void testTakeItem(){
        ItemStack IS = new ItemStack(Material.STONE,64);
        ItemStack IS2 = new ItemStack(Material.STONE, 64, (short) 1);
        FakeInventory inv = fInventory(this, IS, IS2);

        Player mockPlayer = Mockito.mock(Player.class);
        PlayerInventory mockInventory = preparePlayerInventory(mockPlayer, inv);
        Mockito.when(mockPlayer.getInventory()).thenReturn(mockInventory);

        fn.takeItem(mockPlayer, "STONE", 1);
        Assert.assertEquals(63, IS.getAmount());

        fn.takeItem(mockPlayer, "STONE", 2, 1);
        Assert.assertEquals(62, IS2.getAmount());

        fn.takeItem(mockPlayer, 1, 5);
        Assert.assertEquals(58, IS.getAmount());

        fn.takeItem(mockPlayer, 1, 6, 1);
        Assert.assertEquals(56, IS2.getAmount());
    }
}
