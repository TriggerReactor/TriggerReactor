package js.placeholder;

import js.AbstractTestJavaScripts;
import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

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
    
    @Test
    public void testRandom() throws Exception {
    	JsTest test = new PlaceholderTest(engine, "random");
    	
    	test.withArgs(1).test();
    	
    	test.assertValid(0).assertValid(1, 2)
    	    .assertInvalid().assertInvalid(1, 2, 3).assertInvalid("j").assertInvalid(4, "j");
    }
}
