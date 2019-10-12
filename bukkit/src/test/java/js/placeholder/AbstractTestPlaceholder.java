package js.placeholder;

import js.AbstractTestJavaScripts;
import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static io.github.wysohn.triggerreactor.core.utils.TestUtil.assertError;
import static org.junit.Assert.assertEquals;

/**
 * Test driving class for testing Placeholders
 */
public abstract class AbstractTestPlaceholder extends AbstractTestJavaScripts {
    @Test
    public void testPlayername() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);
        Mockito.when(mockPlayer.getName()).thenReturn("wysohn");

        Object result = new PlaceholderTest(engine, "playername")
                .addVariable("player", mockPlayer)
                .test();

        assertEquals("wysohn", result);
    }
    /*
    @Test
    public void testIsNumber() throws Exception{
        JsTest output = new PlaceholderTest(engine, "isnumber");

        output.withArgs("3").test();
       // assertEquals(true, output); TODO
    }
    */
}
