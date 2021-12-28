package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * Test environment for bukkit-legacy.
 * The test should be written in parent class, AbstractTestCommonFunctions,
 * as the test methods will be inherited to the child class, which is this class,
 * so that the same test can be performed on different platforms.
 * <p>
 * However, if some test has to be implemented differently for the each platform,
 * write the individual test in this class so that the test can be individually
 * performed.
 * <p>
 * For example, the takeItem() method still can use numeric value instead of Material enum
 * in legacy bukkit to instantiate an ItemStack, yet it's completely deleted in the latest bukkit.
 */
public class TestCommonFunctions extends AbstractTestCommonFunctions {
    public TestCommonFunctions(CommonFunctions fn) {
        super(fn);
    }

    @Test
    public void testSerializeLocation() {
        World vWorld = Mockito.mock(World.class);
        Location loc1 = new Location(vWorld, 1, 2, 3);
        double testX = 3.4;
        Assert.assertTrue(fn.serializeLocation(vWorld, 1, 2, 3) instanceof ConfigurationSerializable);
        Assert.assertSame(((ConfigurationSerializable) fn.serializeLocation(vWorld, 1, 2, 3)).serialize().get("world"),
                vWorld.getName());

    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTakeItem() {
        ItemStack IS = new ItemStack(Material.STONE, 64);
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
    }    @Override
    protected boolean isEqual(ItemStack IS1, ItemStack IS2) {
        return IS1.getType() == IS2.getType() && IS1.getDurability() == IS2.getDurability()
                && IS1.getAmount() == IS2.getAmount();
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{{new CommonFunctions()}});
    }

    @Override
    protected boolean isSimilar(ItemStack IS1, ItemStack IS2) {
        return IS1.getType() == IS2.getType() && IS1.getDurability() == IS2.getDurability();
    }



    @Override
    public void testGetPlayers() {
        Assert.assertTrue(fn.getPlayers().contains(mockPlayer));
    }

    @Override
    public void testHeadForName() {
        //TODO: not testable?
    }

    @Override
    public void testHeadForValue() {
        //TODO: not testable?
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testItem() {
        ItemStack IS = new ItemStack(Material.STONE, 64);
        ItemStack IS2 = new ItemStack(Material.STONE, 63, (short) 1);

        Assert.assertTrue(isEqual(IS, fn.item("STONE", 64)));
        Assert.assertTrue(isEqual(IS2, fn.item("STONE", 63, 1)));

        Assert.assertTrue(isEqual(IS, fn.item(1, 64)));
        Assert.assertTrue(isEqual(IS2, fn.item(1, 63, 1)));
    }

    @Override
    public void testMakePotionEffect() {
        //TODO: not testable?
    }


}
