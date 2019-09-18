package js.executor;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;


/**
 * Test environment for bukkit-latest.
 * The test should be written in parent class, AbstractTestExecutors,
 * as the test methods will be inherited to the child class, which is this class,
 * so that the same test can be performed on different platforms.
 *
 * However, if some test has to be implemented differently for the each platform,
 * write the individual test in this class so that the test can be individually
 * performed.
 */
public class TestExecutors extends AbstractTestExecutors{
    protected void before() throws Exception{
        register(sem, engine, BukkitUtil.class);
    }
}
