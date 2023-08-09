package js.executor;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Test environment for bukkit-legacy.
 * The test should be written in parent class, AbstractTestExecutors,
 * as the test methods will be inherited to the child class, which is this class,
 * so that the same test can be performed on different platforms.
 * <p>
 * However, if some test has to be implemented differently for the each platform,
 * write the individual test in this class so that the test can be individually
 * performed.
 */

public class TestExecutors extends AbstractTestExecutors {
    protected void before() throws Exception {
        register(sem, engine, BukkitUtil.class);
    }

    @Ignore("Simple test to make sure that tests ran in legacy environment")
    @Test
    public void testLegacy() {

    }

    @Test
    public void testSound1() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        String sound = "FUSE";

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
        String sound = "FUSE";
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
        Sound sound = Sound.FUSE;
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
        Sound sound = Sound.FUSE;
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
        String sound = "FUSE";

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
        String sound = "FUSE";
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
        Sound sound = Sound.FUSE;
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
        // Spigot API 1.8.8 does not have World#playSound(Location, String, float, float)
    }

    @Test
    public void testSetOffHand1() throws Exception {
        // Spigot API 1.8.8 does not have PlayerInventory#setItemInOffHand(ItemStack)
    }

    @Test
    public void testSetOffHand2() throws Exception {
        // Spigot API 1.8.8 does not have PlayerInventory#setItemInOffHand(ItemStack)
    }
}
