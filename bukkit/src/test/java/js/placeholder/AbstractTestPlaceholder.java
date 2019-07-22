package js.placeholder;

import js.AbstractTestJavaScripts;
import js.JsTest;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractTestPlaceholder extends AbstractTestJavaScripts {
    @Test
    public void testPlayername() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);
        Mockito.when(mockPlayer.getName()).thenReturn("wysohn");

        Object result = JsTest.JsTester.placeholderTestOf("playername")
                .addVariable("player", mockPlayer)
                .test(engine);

        Assert.assertEquals("wysohn", result);
    }
}
