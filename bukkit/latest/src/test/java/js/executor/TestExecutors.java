package js.executor;

import js.ExecutorTest;
import js.JsTest;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * Test environment for bukkit-latest.
 * The test should be written in parent class, AbstractTestExecutors,
 * as the test methods will be inherited to the child class, which is this class,
 * so that the same test can be performed on different platforms.
 * <p>
 * However, if some test has to be implemented differently for the each platform,
 * write the individual test in this class so that the test can be individually
 * performed.
 */

public class TestExecutors extends AbstractTestExecutors {
    protected void before() throws Exception {}

    @Test
    public void testActionBar1() throws Exception {
        Player player = Mockito.mock(Player.class);
        String message = "&aHello, world!";

        Player.Spigot spigotPlayer = Mockito.mock(Player.Spigot.class);
        when(player.spigot()).thenReturn(spigotPlayer);

        JsTest test = new ExecutorTest(engine, "ACTIONBAR")
                .addVariable("player", player);

        test.withArgs(message).test();
        Mockito.verify(spigotPlayer)
                .sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.translateAlternateColorCodes('&', message))
                );

        Assert.assertEquals(0, test.getOverload(message));
    }

    @Test
    public void testActionBar2() throws Exception {
        Player player = mock(Player.class);
        String message = "&aHello, world!";
        String colorized = ChatColor.translateAlternateColorCodes('&', message);

        Player.Spigot spigotPlayer = mock(Player.Spigot.class);
        when(player.spigot()).thenReturn(spigotPlayer);

        JsTest test = new ExecutorTest(engine, "ACTIONBAR");

        test.withArgs(player, message).test();
        verify(spigotPlayer)
                .sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(colorized)
                );

        Assert.assertEquals(1, test.getOverload(player, message));
    }
}
