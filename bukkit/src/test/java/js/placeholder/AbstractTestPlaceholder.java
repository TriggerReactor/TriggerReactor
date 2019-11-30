package js.placeholder;

import js.AbstractTestJavaScripts;
import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.Assert;
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

    @Test
    public void testCmdline() throws Exception {
        PlayerCommandPreprocessEvent mockEvent = Mockito.mock(PlayerCommandPreprocessEvent.class);

        JsTest test = new PlaceholderTest(engine, "cmdline");
        test.addVariable("event", mockEvent);


        Mockito.when(mockEvent.getMessage()).thenReturn("/mycommand");

        String line = (String) test.test();
        Mockito.verify(mockEvent).getMessage();
        Assert.assertEquals("mycommand", line);

        line = (String) test.withArgs(0).test();
        Assert.assertEquals("mycommand", line);

        line = (String) test.withArgs(0, 2).test();
        Assert.assertEquals("mycommand", line);


        Mockito.when(mockEvent.getMessage()).thenReturn("/mycommand arg1 arg2");

        line = (String) test.test();
        Assert.assertEquals("mycommand arg1 arg2", line);

        line = (String) test.withArgs(1).test();
        Assert.assertEquals("arg1 arg2", line);

        line = (String) test.withArgs(0, 1).test();
        Assert.assertEquals("mycommand arg1", line);

        line = (String) test.withArgs(0, 99).test();
        Assert.assertEquals("mycommand arg1 arg2", line);

        line = (String) test.withArgs(8, 99).test();
        Assert.assertNull(line);
    }

    @Test
    public void testEntityname() throws Exception{
        EntityEvent mockEvent = Mockito.mock(EntityEvent.class);
        Entity mockEntity = Mockito.mock(Entity.class);

        JsTest test = new PlaceholderTest(engine, "entityname");
        test.addVariable("event", mockEvent);


        Mockito.when(mockEvent.getEntity()).thenReturn(mockEntity);
        Mockito.when(mockEntity.getName()).thenReturn("SomeEntity");

        Assert.assertEquals("SomeEntity", test.test());
    }
}
