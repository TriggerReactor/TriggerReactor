package js.executor;

import js.ExecutorTest;
import js.JsTest;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

    @Test
    public void testSound1() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        String sound = "BLOCK_WOOD_BREAK";

        JsTest test = new ExecutorTest(engine, "SOUND")
            .addVariable("player", player);

        test.withArgs(location, sound).test();

        verify(player).playSound(location, Sound.valueOf(sound), 1F, 1F);

        Assert.assertEquals(0, test.getOverload(location, sound));
    }

    @Test
    public void testSound2() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        String sound = "BLOCK_WOOD_BREAK";
        float volume = 0.5F;
        float pitch = -0.5F;

        JsTest test = new ExecutorTest(engine, "SOUND")
            .addVariable("player", player);

        test.withArgs(location, sound, volume, pitch).test();

        verify(player).playSound(location, Sound.valueOf(sound), volume, pitch);

        Assert.assertEquals(2, test.getOverload(location, sound, volume, pitch));
    }

    @Test
    public void testSound3() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        Sound sound = Sound.BLOCK_WOOD_BREAK;
        float volume = 0.5F;

        JsTest test = new ExecutorTest(engine, "SOUND")
            .addVariable("player", player);

        test.withArgs(location, sound, volume).test();

        verify(player).playSound(location, sound, volume, 1F);

        Assert.assertEquals(4, test.getOverload(location, sound, volume));
    }

    @Test
    public void testSound4() throws Exception {
        Player player = mock(Player.class);
        int x = 100;
        int y = 50;
        int z = -100;
        Sound sound = Sound.BLOCK_WOOD_BREAK;
        float volume = 0.5F;
        float pitch = -0.5F;

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SOUND")
            .addVariable("player", player);

        test.withArgs(x, y, z, sound, volume, pitch).test();

        verify(player).playSound(any(Location.class), eq(sound), eq(volume), eq(pitch));

        Assert.assertEquals(11, test.getOverload(x, y, z, sound, volume, pitch));
    }

    @Test
    public void testSoundAll1() throws Exception {
        Location location = mock(Location.class);
        String sound = "BLOCK_WOOD_BREAK";

        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SOUNDALL");

        test.withArgs(location, sound).test();

        verify(world).playSound(location, Sound.valueOf(sound), 1F, 1F);

        Assert.assertEquals(0, test.getOverload(location, sound));
    }

    @Test
    public void testSoundAll2() throws Exception {
        Location location = mock(Location.class);
        String sound = "BLOCK_WOOD_BREAK";
        float volume = 0.5F;
        float pitch = -0.5F;

        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SOUNDALL");

        test.withArgs(location, sound, volume, pitch).test();

        verify(world).playSound(location, Sound.valueOf(sound), volume, pitch);

        Assert.assertEquals(2, test.getOverload(location, sound, volume, pitch));
    }

    @Test
    public void testSoundAll3() throws Exception {
        Location location = mock(Location.class);
        Sound sound = Sound.BLOCK_WOOD_BREAK;
        float volume = 0.5F;

        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SOUNDALL");

        test.withArgs(location, sound, volume).test();

        verify(world).playSound(location, sound, volume, 1F);

        Assert.assertEquals(4, test.getOverload(location, sound, volume));
    }

    @Test
    public void testSoundAll_string() throws Exception {
        // arrange
        Location location = mock(Location.class);
        String sound = "abc";
        float volume = 0.5F;
        float pitch = -0.5F;

        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SOUNDALL")
            .withArgs(location, sound, volume, pitch);

        // act
        test.test();

        // assert
    }

    @Test
    public void testSetOffHand1() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETOFFHAND")
            .addVariable("player", player);

        test.withArgs(item).test();

        verify(inventory).setItemInOffHand(item);

        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testSetOffHand2() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETOFFHAND");

        test.withArgs(player, item).test();

        verify(inventory).setItemInOffHand(item);

        Assert.assertEquals(1, test.getOverload(player, item));
    }
}
