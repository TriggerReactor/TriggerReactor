package js.executor;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

import static io.github.wysohn.triggerreactor.core.utils.TestUtil.assertJSError;
import static org.mockito.ArgumentMatchers.any;
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
public class TestExecutors extends AbstractTestExecutors {
    protected void before() throws Exception {

    }

    @Test
    public void testMoney() throws Exception {
        VaultSupport vVault = Mockito.mock(VaultSupport.class);
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "MONEY")
                .addVariable("vault", vVault)
                .addVariable("player", vp);

        test.withArgs(30).test();
        Mockito.verify(vVault).give(vp, 30);

        test.withArgs(-30).test();
        Mockito.verify(vVault).take(vp, 30);

        assertJSError(() -> test.withArgs().test(), "Invalid parameter! [Number]");
        assertJSError(() -> test.withArgs("nuu").test(), "Invalid parameter! [Number]");
    }

    @Test
    public void testSetBlock1_Legacy() throws Exception {
        // {block id} {x} {y} {z}
        World mockWorld = Mockito.mock(World.class);
        Player player = Mockito.mock(Player.class);
        Block block = mock(Block.class);

        when(player.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getBlockAt(any(Location.class))).thenReturn(block);
        when(server.getWorld("world")).thenReturn(mockWorld);

        assertJSError(() -> new ExecutorTest(engine, "SETBLOCK")
                .addVariable("player", player)
                .withArgs(1, 33, 96, -15)
                .test(), "Cannot use a number as block type after 1.12.2. Use material name directly.");
    }
}
