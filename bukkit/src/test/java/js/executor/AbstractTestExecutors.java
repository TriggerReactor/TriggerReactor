package js.executor;

import js.AbstractTestJavaScripts;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.*;

/**
 * Test driving class for testing Executors.
 */

public abstract class AbstractTestExecutors extends AbstractTestJavaScripts {
    @BeforeClass
    public static void begin(){
        ExecutorTest.coverage.clear();
    }

    @AfterClass
    public static void tearDown(){
        ExecutorTest.coverage.forEach((key, b) -> System.out.println(key));
        ExecutorTest.coverage.clear();
    }

    @Test
    public void testBroadcast() throws Exception {
        Collection<Player> players = new ArrayList<Player>() {{
            for (int i = 0; i < 5; i++)
                add(mock(Player.class));
        }};

        String message = "&aHello, world!";
        String colorized = ChatColor.translateAlternateColorCodes('&', message);

        doReturn(players).when(server).getOnlinePlayers();

        JsTest test = new ExecutorTest(engine, "BROADCAST");

        test.withArgs(message).test();

        for (Player player : players)
            verify(player).sendMessage(colorized);

        Assert.assertEquals(0, test.getOverload(message));
    }

    @Test
    public void testBurn1() throws Exception {
        Player player = mock(Player.class);
        int second = 5;

        JsTest test = new ExecutorTest(engine, "BURN")
                .addVariable("player", player);

        test.withArgs(second).test();
        verify(player).setFireTicks(20 * second);

        Assert.assertEquals(0, test.getOverload(second));
    }

    @Test
    public void testBurn2() throws Exception {
        Player player = mock(Player.class);
        int second = 5;

        JsTest test = new ExecutorTest(engine, "BURN");

        test.withArgs(player, second).test();
        verify(player).setFireTicks(20 * second);

        Assert.assertEquals(1, test.getOverload(player, second));
    }

    @Test
    public void testClearChat1() throws Exception {
        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "CLEARCHAT")
                .addVariable("player", player);

        test.test();
        verify(player, times(64)).sendMessage("");

        Assert.assertEquals(0, test.getOverload());
    }

    @Test
    public void testClearChat2() throws Exception {
        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "CLEARCHAT");

        test.withArgs(player).test();
        verify(player, times(64)).sendMessage("");

        Assert.assertEquals(1, test.getOverload(player));
    }

    @Test
    public void testClearEntity() throws Exception {
        Collection<Entity> entities = new ArrayList<Entity>() {{
            for (int i = 0; i < 5; i++)
                add(mock(Entity.class));
        }};

        Player player = mock(Player.class);
        double radius = 5;

        doReturn(entities).when(player).getNearbyEntities(anyDouble(), anyDouble(), anyDouble());

        JsTest test = new ExecutorTest(engine, "CLEARENTITY")
                .addVariable("player", player);

        test.withArgs(radius).test();
        for (Entity entity : entities)
            verify(entity).remove();

        Assert.assertEquals(0, test.getOverload(radius));
    }

    @Test
    public void testClearPotion1() throws Exception {
        Collection<PotionEffect> effects = new ArrayList<PotionEffect>() {{
            for (int i = 0; i < 5; i++) {
                PotionEffect effect = mock(PotionEffect.class);

                add(effect);
                when(effect.getType()).thenReturn(mock(PotionEffectType.class));
            }
        }};

        Player player = mock(Player.class);

        doReturn(effects).when(player).getActivePotionEffects();

        JsTest test = new ExecutorTest(engine, "CLEARPOTION")
                .addVariable("player", player);

        test.test();

        verify(player, times(effects.size())).removePotionEffect(any(PotionEffectType.class));

        Assert.assertEquals(0, test.getOverload());
    }

    @Test
    public void testClearPotion2() throws Exception {
        Collection<PotionEffect> effects = new ArrayList<PotionEffect>() {{
            for (int i = 0; i < 5; i++)
                add(mock(PotionEffect.class));
        }};

        Player player = mock(Player.class);
        String effect = "SPEED";

        doReturn(effects).when(player).getActivePotionEffects();

        JsTest test = new ExecutorTest(engine, "CLEARPOTION")
                .addVariable("player", player);

        test.withArgs(effect).test();

        verify(player).removePotionEffect(any());

        Assert.assertEquals(1, test.getOverload(effect));
    }

    @Test
    public void testCloseGui() throws Exception {
        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "CLOSEGUI")
                .addVariable("player", player);

        test.test();

        verify(player).closeInventory();

        Assert.assertEquals(0, test.getOverload());
    }

    @Test
    public void testCmd() throws Exception {
        Player player = mock(Player.class);
        String command = "testCommand";

        when(player.getServer()).thenReturn(server);

        JsTest test = new ExecutorTest(engine, "CMD")
                .addVariable("player", player);

        test.withArgs(command).test();

        verify(server).dispatchCommand(player, command);

        Assert.assertEquals(0, test.getOverload(command));
    }

    @Test
    public void testCmdCon() throws Exception {
        Player player = mock(Player.class);
        String command = "testCommand";

        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);

        when(player.getServer()).thenReturn(server);
        when(server.getConsoleSender()).thenReturn(sender);

        JsTest test = new ExecutorTest(engine, "CMDCON");

        test.withArgs(command).test();

        verify(server).dispatchCommand(sender, command);

        Assert.assertEquals(0, test.getOverload(command));
    }
}