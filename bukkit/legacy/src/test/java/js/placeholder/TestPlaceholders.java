package js.placeholder;

import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test environment for bukkit-legacy.
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
    public void testHeldItemId() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        Material material = Material.STONE;

        when(player.getItemInHand()).thenReturn(item);
        when(item.getType()).thenReturn(material);

        JsTest test = new PlaceholderTest(engine, "helditemid")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(material.getId(), result);
    }
}
