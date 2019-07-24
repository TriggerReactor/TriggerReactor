package js.executor;

import js.AbstractTestJavaScripts;
import js.JsTest;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test driving class for testing Executors.
 *
 */
public class AbstractTestExecutors extends AbstractTestJavaScripts {
    @Test
    public void testPlayer_SetFlyMode() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(true)
                .test(engine);
        Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(true));

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(true)
                .test(engine);
        Mockito.verify(mockPlayer).setFlying(Mockito.eq(true));

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(false)
                .test(engine);
        Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(false));

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(false)
                .test(engine);
        Mockito.verify(mockPlayer).setFlying(Mockito.eq(false));
    }

    @Test
    public void testActionBar() throws Exception{
        //TODO
    }

    @Test
    public void testMessage() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);

        JsTest.JsTester.executorTestOf("MESSAGE")
                .addVariable("player", mockPlayer)
                .withArgs("&cTest Message")
                .test(engine);

        String expected = ChatColor.translateAlternateColorCodes('&', "&cTest Message");
        Mockito.verify(mockPlayer).sendMessage(Mockito.argThat((String str) -> expected.equals(str)));
    }
}
