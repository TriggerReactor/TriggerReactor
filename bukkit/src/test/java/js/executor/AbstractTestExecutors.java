package js.executor;

import js.AbstractTestJavaScripts;
import js.JsTest;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractTestExecutors extends AbstractTestJavaScripts {
    @Test
    public void testMessage() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);

        JsTest.JsTester.executorTestOf("MESSAGE")
                .addVariable("player", mockPlayer)
                .withArgs("&cTest Message")
                .test(engine);

        Mockito.verify(mockPlayer)
                .sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTest Message"));
    }
}
