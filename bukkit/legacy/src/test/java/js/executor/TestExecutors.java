package js.executor;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

import static io.github.wysohn.triggerreactor.core.utils.TestUtil.assertJSError;

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
public class TestExecutors extends AbstractTestExecutors {
    protected void before() throws Exception {
        register(sem, engine, BukkitUtil.class);
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

}
