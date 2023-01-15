package js.placeholder;

import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test environment for bukkit-latest.
 * The test should be written in parent class, AbstractTestExecutors,
 * as the test methods will be inherited to the child class, which is this class,
 * so that the same test can be performed on different platforms.
 * <p>
 * However, if some test has to be implemented differently for the each platform,
 * write the individual test in this class so that the test can be individually
 * performed.
 */
public class TestPlaceholders extends AbstractTestPlaceholder {
    public void before() throws Exception {

    }

    @Test
    public void testOffHandItem() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInOffHand()).thenReturn(item);

        JsTest test = new PlaceholderTest(engine, "offhanditem")
                .addVariable("player", player);

        ItemStack result = (ItemStack) test.test();

        Assert.assertEquals(item, result);
    }
}
