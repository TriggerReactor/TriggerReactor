package js.placeholder;

import js.AbstractTestJavaScripts;
import js.PlaceholderTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test driving class for testing Placeholders
 */
public abstract class AbstractTestPlaceholder extends AbstractTestJavaScripts {

    @BeforeClass
    public static void begin(){
        PlaceholderTest.coverage.clear();
    }

    @AfterClass
    public static void tearDown(){
        PlaceholderTest.coverage.forEach((key, b) -> System.out.println(key));
        PlaceholderTest.coverage.clear();
    }
}
