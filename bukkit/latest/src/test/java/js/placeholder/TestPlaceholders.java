package js.placeholder;

import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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
public class TestPlaceholders extends AbstractTestPlaceholder {
    public void before() throws Exception{
        
    }
}
