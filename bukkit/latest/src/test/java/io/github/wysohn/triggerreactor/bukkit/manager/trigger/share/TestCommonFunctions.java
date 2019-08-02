package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * Test environment for bukkit-latest.
 * The test should be written in parent class, AbstractTestCommonFunctions,
 * as the test methods will be inherited to the child class, which is this class,
 * so that the same test can be performed on different platforms.
 *
 * However, if some test has to be implemented differently for the each platform,
 * write the individual test in this class so that the test can be individually
 * performed.
 *
 * For example, the takeItem() method still can use numeric value instead of Material enum
 * in legacy bukkit to instantiate an ItemStack, yet it's completely deleted in the latest bukkit.
 */
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
        return IS1.getType() == IS2.getType();
    }

    @Override
    protected boolean isEqual(ItemStack IS1, ItemStack IS2) {
        return IS1.getType() == IS2.getType() && IS1.getAmount() == IS2.getAmount();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTakeItem(){
        ItemStack IS = new ItemStack(Material.STONE,64);
        ItemStack IS2 = new ItemStack(Material.GRANITE, 64);
        FakeInventory inv = fInventory(this, IS, IS2);

        Player mockPlayer = Mockito.mock(Player.class);
        PlayerInventory mockInventory = preparePlayerInventory(mockPlayer, inv);
        Mockito.when(mockPlayer.getInventory()).thenReturn(mockInventory);

        fn.takeItem(mockPlayer, "STONE", 1);
        Assert.assertEquals(63, IS.getAmount());

        fn.takeItem(mockPlayer, "GRANITE", 2);
        Assert.assertEquals(62, IS2.getAmount());

        fn.takeItem(mockPlayer, 1, 5);
        fn.takeItem(mockPlayer, 1, 5, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testItem(){
        ItemStack IS = new ItemStack(Material.STONE,64);
        ItemStack IS2 = new ItemStack(Material.GRANITE, 63);

        Assert.assertTrue(isEqual(IS, fn.item("STONE", 64)));
        Assert.assertTrue(isEqual(IS2, fn.item("GRANITE", 63)));

        Assert.assertTrue(isEqual(IS, fn.item(0, 64)));
        Assert.assertTrue(isEqual(IS2, fn.item(0, 63, 1)));
    }
}
