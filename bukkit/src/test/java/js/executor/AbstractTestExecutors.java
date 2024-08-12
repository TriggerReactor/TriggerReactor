package js.executor;

import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.core.main.Platform;
import io.github.wysohn.triggerreactor.core.manager.PlatformManager;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import js.AbstractTestJavaScripts;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.material.Lever;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

/**
 * Test driving class for testing Executors.
 */

public abstract class AbstractTestExecutors extends AbstractTestJavaScripts {
    @BeforeClass
    public static void begin() {
        ExecutorTest.coverage.clear();
    }

    @AfterClass
    public static void tearDown() {
        ExecutorTest.coverage.forEach((key, b) -> System.out.println(key));
        ExecutorTest.coverage.clear();
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
    public void testClearPotion() throws Exception {
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
    public void testCloseGui() throws Exception {
        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "CLOSEGUI")
                .addVariable("player", player);

        test.test();

        verify(player).closeInventory();
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

    @Test
    public void testDoorClose() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Door blockData = mock(Door.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);

        JsTest test = new ExecutorTest(engine, "DOORCLOSE");

        test.withArgs(location).test();

        verify(blockData).setOpen(false);
        verify(blockState).setData(any(Door.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testDoorOpen() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Door blockData = mock(Door.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);

        JsTest test = new ExecutorTest(engine, "DOOROPEN");

        test.withArgs(location).test();

        verify(blockData).setOpen(true);
        verify(blockState).setData(any(Door.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testDoorToggle1() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Door blockData = mock(Door.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);
        when(blockData.isOpen()).thenReturn(false);

        JsTest test = new ExecutorTest(engine, "DOORTOGGLE");

        test.withArgs(location).test();

        verify(blockData).setOpen(true);
        verify(blockState).setData(any(Door.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testDoorToggle2() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Door blockData = mock(Door.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);
        when(blockData.isOpen()).thenReturn(true);

        JsTest test = new ExecutorTest(engine, "DOORTOGGLE");

        test.withArgs(location).test();

        verify(blockData).setOpen(false);
        verify(blockState).setData(any(Door.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testDropItem() throws Exception {
        ItemStack itemStack = mock(ItemStack.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "DROPITEM");

        test.withArgs(itemStack, location).test();

        verify(world).dropItem(location, itemStack);

        Assert.assertEquals(0, test.getOverload(itemStack, location));
    }

    @Test
    public void testExplosion1() throws Exception {
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "EXPLOSION");

        test.withArgs(location).test();

        verify(world).createExplosion(location, 4.0F, false);

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testExplosion2() throws Exception {
        Location location = mock(Location.class);
        World world = mock(World.class);
        float power = 4;
        boolean fire = true;

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "EXPLOSION");

        test.withArgs(location, power, fire).test();

        verify(world).createExplosion(location, power, fire);

        Assert.assertEquals(2, test.getOverload(location, power, fire));
    }

    @Test
    public void testFallingBlock1() throws Exception {
        Material material = Material.values()[0];
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "FALLINGBLOCK");

        test.withArgs(material, location).test();

        verify(world).spawnFallingBlock(location, material, (byte) 0);

        Assert.assertEquals(0, test.getOverload(material, location));
    }

    @Test
    public void testFallingBlock2() throws Exception {
        Location location = mock(Location.class);
        World world = mock(World.class);
        String material = "STONE";

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "FALLINGBLOCK");

        test.withArgs(material, location).test();

        verify(world).spawnFallingBlock(location, Material.valueOf(material), (byte) 0);

        Assert.assertEquals(2, test.getOverload(material, location));
    }

    @Test
    public void testGive1() throws Exception {
        // arrange
        Player player = mock(Player.class);
        ItemStack itemStack = mock(ItemStack.class);
        PlayerInventory inventory = mock(PlayerInventory.class);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getSize()).thenReturn(54);
        when(inventory.getItem(anyInt())).thenReturn(null);
        when(itemStack.getMaxStackSize()).thenReturn(64);

        // act
        JsTest test = new ExecutorTest(engine, "GIVE")
                .addVariable("player", player);

        test.withArgs(itemStack).test();

        // assert
        verify(inventory).addItem(itemStack);

        Assert.assertEquals(0, test.getOverload(itemStack));
    }

    @Test
    public void testGive2() throws Exception {
        // arrange
        Player player = mock(Player.class);
        ItemStack itemStack = mock(ItemStack.class);
        PlayerInventory inventory = mock(PlayerInventory.class);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getSize()).thenReturn(54);
        when(inventory.getItem(anyInt())).thenReturn(null);
        when(itemStack.getMaxStackSize()).thenReturn(64);

        // act
        JsTest test = new ExecutorTest(engine, "GIVE")
                .addVariable("player", player);

        test.withArgs(itemStack).test();

        // assert
        verify(inventory).addItem(itemStack);

        Assert.assertEquals(1, test.getOverload(player, itemStack));
    }

//    @Test
//    public void testGui1() throws Exception {
//        Player player = mock(Player.class);
//        String playerName = "TestPlayer";
//        String guiName = "TESTGUI";
//
//        AbstractJavaPlugin mockPlugin = mock(AbstractJavaPlugin.class);
//        InventoryTriggerManager inventoryTriggerManager = mock(InventoryTriggerManager.class);
//        IInventory inventory = mock(IInventory.class);
//        Injector injector = mock(Injector.class);
//
//        when(player.getName()).thenReturn(playerName);
//        when(inventoryTriggerManager.openGUI(eq(playerName), eq(guiName))).thenReturn(inventory);
//        when(injector.getInstance(InventoryTriggerManager.class)).thenReturn(inventoryTriggerManager);
//
//        JsTest test = new ExecutorTest(engine, "GUI")
//                .addVariable("player", player)
//                .addVariable("plugin", mockPlugin)
//                .addVariable("injector", injector);
//
//        test.withArgs(guiName).test();
//
//        verify(inventoryTriggerManager).openGUI(eq(playerName), eq(guiName));
//
//        Assert.assertEquals(0, test.getOverload(guiName));
//    }

//    @Test
//    public void testGui2() throws Exception {
//        Player player = mock(Player.class);
//        String playerName = "TestPlayer";
//        String guiName = "TESTGUI";
//
//        AbstractJavaPlugin mockPlugin = mock(AbstractJavaPlugin.class);
//        InventoryTriggerManager inventoryTriggerManager = mock(InventoryTriggerManager.class);
//        IInventory inventory = mock(IInventory.class);
//        Injector injector = mock(Injector.class);
//
//        when(player.getName()).thenReturn(playerName);
//        when(inventoryTriggerManager.openGUI(eq(playerName), eq(guiName))).thenReturn(inventory);
//        when(injector.getInstance(InventoryTriggerManager.class)).thenReturn(inventoryTriggerManager);
//
//        JsTest test = new ExecutorTest(engine, "GUI")
//                .addVariable("plugin", mockPlugin)
//                .addVariable("injector", injector);
//
//        test.withArgs(player, guiName).test();
//
//        verify(inventoryTriggerManager).openGUI(eq(playerName), eq(guiName));
//
//        Assert.assertEquals(1, test.getOverload(player, guiName));
//    }

    @Test
    public void testItemFrameRotate() throws Exception {
        Location location = mock(Location.class);
        World world = mock(World.class);

        ItemFrame itemFrame = mock(ItemFrame.class);
        EntityType entityType = EntityType.ITEM_FRAME;
        Rotation rotation = Rotation.NONE;

        Collection<Entity> entities = new ArrayList<Entity>() {{
            add(itemFrame);
            for (int i = 0; i < 4; i++) {
                Entity entity = mock(Entity.class);
                EntityType entityType1 = EntityType.PIG;

                add(entity);
                when(entity.getType()).thenReturn(entityType1);
            }
        }};

        when(location.getWorld()).thenReturn(world);
        when(world.getNearbyEntities(eq(location), anyDouble(), anyDouble(), anyDouble())).thenReturn(entities);
        when(itemFrame.getType()).thenReturn(entityType);
        when(itemFrame.getRotation()).thenReturn(rotation);

        JsTest test = new ExecutorTest(engine, "ITEMFRAMEROTATE");

        test.withArgs(location).test();

        verify(itemFrame).setRotation(any(Rotation.class));

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testItemFrameSet() throws Exception {
        ItemStack itemStack = mock(ItemStack.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        ItemFrame itemFrame = mock(ItemFrame.class);
        EntityType entityType = EntityType.ITEM_FRAME;

        Collection<Entity> entities = new ArrayList<Entity>() {{
            add(itemFrame);
            for (int i = 0; i < 4; i++) {
                Entity entity = mock(Entity.class);
                EntityType entityType1 = EntityType.PIG;

                add(entity);
                when(entity.getType()).thenReturn(entityType1);
            }
        }};

        when(location.getWorld()).thenReturn(world);
        when(world.getNearbyEntities(eq(location), anyDouble(), anyDouble(), anyDouble())).thenReturn(entities);
        when(itemFrame.getType()).thenReturn(entityType);

        JsTest test = new ExecutorTest(engine, "ITEMFRAMESET");

        test.withArgs(itemStack, location).test();

        verify(itemFrame).setItem(itemStack);

        Assert.assertEquals(0, test.getOverload(itemStack, location));
    }

    @Test
    public void testKick1() throws Exception {
        Player player = mock(Player.class);
        String reason = "&cReason";
        String colorized = ChatColor.translateAlternateColorCodes('&', reason);

        JsTest test = new ExecutorTest(engine, "KICK")
                .addVariable("player", player);

        test.withArgs(reason).test();

        verify(player).kickPlayer(colorized);

        Assert.assertEquals(0, test.getOverload(reason));
    }

    @Test
    public void testKick2() throws Exception {
        Player player = mock(Player.class);
        String reason = "&c[TR] You've been kicked from the server.";
        String colorized = ChatColor.translateAlternateColorCodes('&', reason);

        JsTest test = new ExecutorTest(engine, "KICK");

        test.withArgs(player).test();

        verify(player).kickPlayer(colorized);

        Assert.assertEquals(1, test.getOverload(player));
    }

    @Test
    public void testKick3() throws Exception {
        Player player = mock(Player.class);
        String reason = "&cReason";
        String colorized = ChatColor.translateAlternateColorCodes('&', reason);

        JsTest test = new ExecutorTest(engine, "KICK");

        test.withArgs(player, reason).test();

        verify(player).kickPlayer(colorized);

        Assert.assertEquals(2, test.getOverload(player, reason));
    }

    @Test
    public void testKill1() throws Exception {
        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "KILL")
                .addVariable("player", player);

        test.test();

        verify(player).setHealth(0);

        Assert.assertEquals(0, test.getOverload());
    }

    @Test
    public void testKill2() throws Exception {
        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "KILL");

        test.withArgs(player).test();

        verify(player).setHealth(0);

        Assert.assertEquals(1, test.getOverload(player));
    }

    @Test
    public void testLeverOff() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Lever blockData = mock(Lever.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);

        JsTest test = new ExecutorTest(engine, "LEVEROFF");

        test.withArgs(location).test();

        verify(blockData).setPowered(false);
        verify(blockState).setData(any(Lever.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testLeverOn() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Lever blockData = mock(Lever.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);

        JsTest test = new ExecutorTest(engine, "LEVERON");

        test.withArgs(location).test();

        verify(blockData).setPowered(true);
        verify(blockState).setData(any(Lever.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testLeverToggle1() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Lever blockData = mock(Lever.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);
        when(blockData.isPowered()).thenReturn(false);

        JsTest test = new ExecutorTest(engine, "LEVERTOGGLE");

        test.withArgs(location).test();

        verify(blockData).setPowered(true);
        verify(blockState).setData(any(Lever.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testLeverToggle2() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);
        Lever blockData = mock(Lever.class);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(blockData);
        when(blockData.isPowered()).thenReturn(true);

        JsTest test = new ExecutorTest(engine, "LEVERTOGGLE");

        test.withArgs(location).test();

        verify(blockData).setPowered(false);
        verify(blockState).setData(any(Lever.class));
        verify(blockState).update();

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testLightning() throws Exception {
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "LIGHTNING");

        test.withArgs(location).test();

        verify(world).strikeLightning(location);

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testBroadcast() throws Exception {
        // arrange
        class ExampleObject {
            private final String message;

            public ExampleObject(String message) {
                this.message = message;
            }

            @Override
            public String toString() {
                return "ExampleObject(" + message + ")";
            }
        }

        ExampleObject exampleObject = new ExampleObject("Message");
        PlatformManager platformManager = mock(PlatformManager.class);
        Platform platform = Platform.Unknown;
        Injector injector = mock(Injector.class);

        when(injector.getInstance(PlatformManager.class)).thenReturn(platformManager);
        when(platformManager.getCurrentPlatform()).thenReturn(platform);

        // act
        JsTest test = new ExecutorTest(engine, "BROADCAST")
                .addVariable("injector", injector);

        test.withArgs(exampleObject).test();

        // assert
        //TODO need to refactor javascript to test this
    }

    @Test
    public void testMessage() throws Exception {
        // arrange
        class ExampleObject {
            private final String message;

            public ExampleObject(String message) {
                this.message = message;
            }

            @Override
            public String toString() {
                return "ExampleObject(" + message + ")";
            }
        }

        Player player = mock(Player.class);
        ExampleObject exampleObject = new ExampleObject("Message");
        PlatformManager platformManager = mock(PlatformManager.class);
        Platform platform = Platform.Unknown;
        Injector injector = mock(Injector.class);

        // when(Platform.Unknown.supports(eq(Dependency.MiniMessage))).thenReturn(false);
        when(injector.getInstance(PlatformManager.class)).thenReturn(platformManager);
        when(platformManager.getCurrentPlatform()).thenReturn(platform);

        // act
        JsTest test = new ExecutorTest(engine, "MESSAGE")
                .addVariable("player", player)
                .addVariable("injector", injector);

        test.withArgs(exampleObject).test();

        // assert
        verify(player).sendMessage(exampleObject.toString());

        Assert.assertEquals(0, test.getOverload(exampleObject));
    }

    @Test
    public void testLog() throws Exception {
        // arrange
        class ExampleObject {
            private final String message;

            public ExampleObject(String message) {
                this.message = message;
            }

            @Override
            public String toString() {
                return "ExampleObject(" + message + ")";
            }
        }

        ExampleObject exampleObject = new ExampleObject("Message");
        PlatformManager platformManager = mock(PlatformManager.class);
        Platform platform = Platform.Unknown;
        Injector injector = mock(Injector.class);

        when(injector.getInstance(PlatformManager.class)).thenReturn(platformManager);
        when(platformManager.getCurrentPlatform()).thenReturn(platform);

        // act
        JsTest test = new ExecutorTest(engine, "LOG")
                .addVariable("injector", injector);

        //TODO need to refactor javascript to test this
//        test.withArgs(exampleObject).test();

        // assert
        //TODO need to refactor javascript to test this
    }

    @Test
    public void testMoney1() throws Exception {
        class FakeVault {
            public void give(Player player, int money) {
            }

            public void take(Player player, int money) {
            }
        }

        Player player = mock(Player.class);
        FakeVault vault = mock(FakeVault.class);
        int money = 1000;

        JsTest test = new ExecutorTest(engine, "MONEY")
                .addVariable("player", player)
                .addVariable("vault", vault);

        test.withArgs(money).test();

        verify(vault).give(player, money);

        Assert.assertEquals(0, test.getOverload(money));
    }

    @Test
    public void testMoney2() throws Exception {
        class FakeVault {
            public void give(Player player, int money) {
            }

            public void take(Player player, int money) {
            }
        }

        Player player = mock(Player.class);
        FakeVault vault = mock(FakeVault.class);
        int money = -1000;

        JsTest test = new ExecutorTest(engine, "MONEY")
                .addVariable("vault", vault);

        test.withArgs(player, money).test();

        verify(vault).take(player, -money);

        Assert.assertEquals(1, test.getOverload(player, money));
    }

    @Test
    public void testMysql() throws Exception {
        // arrange
        class FakeMysqlHelper {
            public void set(String key, Object value) {
            }
        }

        class FakePlugin {
            public FakeMysqlHelper getMysqlHelper() {
                return null;
            }
        }

        FakeMysqlHelper mysqlHelper = mock(FakeMysqlHelper.class);
        FakePlugin plugin = mock(FakePlugin.class);
        Injector injector = mock(Injector.class);

        String key = "testKey";
        Object value = 100D;

        when(plugin.getMysqlHelper()).thenReturn(mysqlHelper);
        when(injector.getInstance(any(Class.class))).thenReturn(mysqlHelper);

        JsTest test = new ExecutorTest(engine, "MYSQL")
                .addVariable("injector", injector)
                .addVariable("plugin", plugin);

        // act
        test.withArgs(key, value).test();

        // assert
        verify(mysqlHelper).set(key, value);

        Assert.assertEquals(0, test.getOverload(key, value));
    }

    @Test
    public void testPermission1() throws Exception {
        class FakeVault {
            public void permit(Player player, String permission) {
            }

            public void revoke(Player player, String permission) {
            }
        }

        Player player = mock(Player.class);
        FakeVault vault = mock(FakeVault.class);
        String permission = "triggerreactor.test";

        JsTest test = new ExecutorTest(engine, "PERMISSION")
                .addVariable("player", player)
                .addVariable("vault", vault);

        test.withArgs(permission).test();

        verify(vault).permit(player, permission);

        Assert.assertEquals(0, test.getOverload(permission));
    }

    @Test
    public void testPermission2() throws Exception {
        class FakeVault {
            public void permit(Player player, String permission) {
            }

            public void revoke(Player player, String permission) {
            }
        }

        Player player = mock(Player.class);
        FakeVault vault = mock(FakeVault.class);
        String permission = "-triggerreactor.test";

        JsTest test = new ExecutorTest(engine, "PERMISSION")
                .addVariable("vault", vault);

        test.withArgs(player, permission).test();

        verify(vault).revoke(player, permission.substring(1));

        Assert.assertEquals(1, test.getOverload(player, permission));
    }

    @Test
    public void testPush1() throws Exception {
        Player player = mock(Player.class);
        int x = 10;
        int y = 5;
        int z = -10;

        JsTest test = new ExecutorTest(engine, "PUSH")
                .addVariable("player", player);

        test.withArgs(x, y, z).test();

        verify(player).setVelocity(new Vector(x, y, z));

        Assert.assertEquals(0, test.getOverload(x, y, z));
    }

    @Test
    public void testPush2() throws Exception {
        Entity entity = mock(Entity.class);
        int x = 10;
        int y = 5;
        int z = -10;

        JsTest test = new ExecutorTest(engine, "PUSH");

        test.withArgs(entity, x, y, z).test();

        verify(entity).setVelocity(new Vector(x, y, z));

        Assert.assertEquals(1, test.getOverload(entity, x, y, z));
    }

    @Test
    public void testServer1() throws Exception {
        class FakeBungeeHelper {
            public void sendToServer(Player player, String server) {
            }
        }

        FakeBungeeHelper bungeeHelper = mock(FakeBungeeHelper.class);
        Injector injector = mock(Injector.class);

        Player player = mock(Player.class);
        String server = "SecondServer";

        when(injector.getInstance(any(Class.class))).thenReturn(bungeeHelper);

        JsTest test = new ExecutorTest(engine, "SERVER")
                .addVariable("injector", injector)
                .addVariable("player", player);

        test.withArgs(server).test();

        verify(bungeeHelper).sendToServer(player, server);

        Assert.assertEquals(0, test.getOverload(server));
    }

    @Test
    public void testServer2() throws Exception {
        class FakeBungeeHelper {
            public void sendToServer(Player player, String server) {
            }
        }

        FakeBungeeHelper bungeeHelper = mock(FakeBungeeHelper.class);
        Injector injector = mock(Injector.class);

        Player player = mock(Player.class);
        String server = "SecondServer";

        when(injector.getInstance(any(Class.class))).thenReturn(bungeeHelper);

        JsTest test = new ExecutorTest(engine, "SERVER")
                .addVariable("injector", injector);

        test.withArgs(player, server).test();

        verify(bungeeHelper).sendToServer(player, server);

        Assert.assertEquals(1, test.getOverload(player, server));
    }

    @Test
    public void testSetBlock() throws Exception {
        String materialName = "STONE";
        Location location = mock(Location.class);
        Block block = mock(Block.class);

        when(location.getBlock()).thenReturn(block);

        JsTest test = new ExecutorTest(engine, "SETBLOCK");

        test.withArgs(materialName, location).test();

        verify(block).setType(Material.valueOf(materialName));

        Assert.assertEquals(0, test.getOverload(materialName, location));
    }

    @Test
    public void testSetCount() throws Exception {
        ItemStack itemStack = mock(ItemStack.class);
        int amount = 32;

        JsTest test = new ExecutorTest(engine, "SETCOUNT");

        test.withArgs(amount, itemStack).test();

        verify(itemStack).setAmount(amount);

        Assert.assertEquals(0, test.getOverload(amount, itemStack));
    }

    @Test
    public void testSetFlyMode1() throws Exception {
        Player player = mock(Player.class);
        boolean isFly = true;

        JsTest test = new ExecutorTest(engine, "SETFLYMODE")
                .addVariable("player", player);

        test.withArgs(isFly).test();

        verify(player).setAllowFlight(isFly);
        verify(player).setFlying(isFly);

        Assert.assertEquals(0, test.getOverload(isFly));
    }

    @Test
    public void testSetFlyMode2() throws Exception {
        Player player = mock(Player.class);
        boolean isFly = false;

        JsTest test = new ExecutorTest(engine, "SETFLYMODE");

        test.withArgs(player, isFly).test();

        verify(player).setAllowFlight(isFly);
        verify(player).setFlying(isFly);

        Assert.assertEquals(1, test.getOverload(player, isFly));
    }

    @Test
    public void testSetFlySpeed1() throws Exception {
        Player player = mock(Player.class);
        float speed = 2.5F;

        JsTest test = new ExecutorTest(engine, "SETFLYSPEED")
                .addVariable("player", player);

        test.withArgs(speed).test();

        verify(player).setFlySpeed(speed);

        Assert.assertEquals(0, test.getOverload(speed));
    }

    @Test
    public void testSetFlySpeed2() throws Exception {
        Player player = mock(Player.class);
        float speed = 2.5F;

        JsTest test = new ExecutorTest(engine, "SETFLYSPEED");

        test.withArgs(player, speed).test();

        verify(player).setFlySpeed(speed);

        Assert.assertEquals(1, test.getOverload(player, speed));
    }

    @Test
    public void testSetFood1() throws Exception {
        Player player = mock(Player.class);
        int food = 5;

        JsTest test = new ExecutorTest(engine, "SETFOOD")
                .addVariable("player", player);

        test.withArgs(food).test();

        verify(player).setFoodLevel(food);

        Assert.assertEquals(0, test.getOverload(food));
    }

    @Test
    public void testSetFood2() throws Exception {
        Player player = mock(Player.class);
        int food = 5;

        JsTest test = new ExecutorTest(engine, "SETFOOD");

        test.withArgs(player, food).test();

        verify(player).setFoodLevel(food);

        Assert.assertEquals(1, test.getOverload(player, food));
    }

    @Test
    public void testSetGameMode1() throws Exception {
        Player player = mock(Player.class);
        int modeNum = 1;

        JsTest test = new ExecutorTest(engine, "SETGAMEMODE")
                .addVariable("player", player);

        test.withArgs(modeNum).test();

        verify(player).setGameMode(GameMode.CREATIVE);

        Assert.assertEquals(1, test.getOverload(modeNum));
    }

    @Test
    public void testSetGameMode2() throws Exception {
        Player player = mock(Player.class);
        String modeStr = "SURVIVAL";

        JsTest test = new ExecutorTest(engine, "SETGAMEMODE");

        test.withArgs(player, modeStr).test();

        verify(player).setGameMode(GameMode.valueOf(modeStr));

        Assert.assertEquals(2, test.getOverload(player, modeStr));
    }

    @Test
    public void testSetHealth1() throws Exception {
        Player player = mock(Player.class);
        double health = 10.5D;

        when(player.getMaxHealth()).thenReturn(20D);

        JsTest test = new ExecutorTest(engine, "SETHEALTH")
                .addVariable("player", player);

        test.withArgs(health).test();

        verify(player).setHealth(health);

        Assert.assertEquals(0, test.getOverload(health));
    }

    @Test
    public void testSetHealth2() throws Exception {
        Player player = mock(Player.class);
        double health = 10.5D;

        when(player.getMaxHealth()).thenReturn(20D);

        JsTest test = new ExecutorTest(engine, "SETHEALTH");

        test.withArgs(player, health).test();

        verify(player).setHealth(health);

        Assert.assertEquals(1, test.getOverload(player, health));
    }

    @Test
    public void testSetHeldItem1() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);

        when(item.getType()).thenReturn(Material.STONE);
        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETHELDITEM")
                .addVariable("player", player);

        test.withArgs(item).test();

        verify(inventory).setItemInHand(item);

        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testSetHeldItem2() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);

        when(item.getType()).thenReturn(Material.STONE);
        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETHELDITEM");

        test.withArgs(player, item).test();

        verify(inventory).setItemInHand(item);

        Assert.assertEquals(1, test.getOverload(player, item));
    }

    @Test
    public void testSetItemLore() throws Exception {
        ItemStack item = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);
        String lore = "&aFirst Line\n&bSecond Line\n\n\n&f5th Line";
        String colorized = ChatColor.translateAlternateColorCodes('&', lore);
        String[] lores = colorized.split("\n");

        when(item.getType()).thenReturn(Material.STONE);
        when(item.getItemMeta()).thenReturn(itemMeta);

        JsTest test = new ExecutorTest(engine, "SETITEMLORE");

        test.withArgs(lore, item).test();

        verify(itemMeta).setLore(Arrays.asList(lores));

        Assert.assertEquals(0, test.getOverload(lore, item));
    }

    @Test
    public void testSetItemName() throws Exception {
        ItemStack item = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);
        String title = "&aCustom Title";
        String colorized = ChatColor.translateAlternateColorCodes('&', title);

        when(item.getType()).thenReturn(Material.STONE);
        when(item.getItemMeta()).thenReturn(itemMeta);

        JsTest test = new ExecutorTest(engine, "SETITEMNAME");

        test.withArgs(title, item).test();

        verify(itemMeta).setDisplayName(colorized);

        Assert.assertEquals(0, test.getOverload(title, item));
    }

    @Test
    public void testSetMaxHealth1() throws Exception {
        Player player = mock(Player.class);
        double health = 100D;

        JsTest test = new ExecutorTest(engine, "SETMAXHEALTH")
                .addVariable("player", player);

        test.withArgs(health).test();

        verify(player).setMaxHealth(health);

        Assert.assertEquals(0, test.getOverload(health));
    }

    @Test
    public void testSetMaxHealth2() throws Exception {
        Player player = mock(Player.class);
        double health = 100D;

        JsTest test = new ExecutorTest(engine, "SETMAXHEALTH");

        test.withArgs(player, health).test();

        verify(player).setMaxHealth(health);

        Assert.assertEquals(1, test.getOverload(player, health));
    }

    @Test(expected = ValidationException.class)
    public void testSetMaxHealth3() throws Exception {
        Player player = mock(Player.class);
        double health = 10000D;

        JsTest test = new ExecutorTest(engine, "SETMAXHEALTH")
                .addVariable("player", player);

        test.withArgs(health).test();
    }

    @Test
    public void testSetPlayerInv1() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);
        int slot = 10;

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETPLAYERINV")
                .addVariable("player", player);

        test.withArgs(slot, item).test();

        verify(inventory).setItem(slot, item);

        Assert.assertEquals(0, test.getOverload(slot, item));
    }

    @Test
    public void testSetPlayerInv2() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);
        int slot = 10;

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETPLAYERINV");

        test.withArgs(player, slot, item).test();

        verify(inventory).setItem(slot, item);

        Assert.assertEquals(1, test.getOverload(player, slot, item));
    }

    @Test(expected = ValidationException.class)
    public void testSetPlayerInv3() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);
        int slot = 100;

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "SETPLAYERINV")
                .addVariable("player", player);

        test.withArgs(player, slot, item).test();
    }

    @Test
    public void testSetSaturation1() throws Exception {
        Player player = mock(Player.class);
        float saturation = 20F;

        JsTest test = new ExecutorTest(engine, "SETSATURATION")
                .addVariable("player", player);

        test.withArgs(saturation).test();

        verify(player).setSaturation(saturation);

        Assert.assertEquals(0, test.getOverload(saturation));
    }

    @Test
    public void testSetSaturation2() throws Exception {
        Player player = mock(Player.class);
        float saturation = 20F;

        JsTest test = new ExecutorTest(engine, "SETSATURATION");

        test.withArgs(player, saturation).test();

        verify(player).setSaturation(saturation);

        Assert.assertEquals(1, test.getOverload(player, saturation));
    }

    @Test(expected = ValidationException.class)
    public void testSetSaturation3() throws Exception {
        Player player = mock(Player.class);
        float saturation = -5F;

        JsTest test = new ExecutorTest(engine, "SETSATURATION")
                .addVariable("player", player);

        test.withArgs(saturation).test();
    }

    @Test
    public void testSetSlot() throws Exception {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        ItemStack item = mock(ItemStack.class);
        DoubleChestInventory inventory = mock(DoubleChestInventory.class);
        int index = 1;

        when(event.getInventory()).thenReturn(inventory);
        when(inventory.getSize()).thenReturn(36);

        JsTest test = new ExecutorTest(engine, "SETSLOT")
                .addVariable("event", event);

        test.withArgs(index, item).test();

        verify(inventory).setItem(index, item);

        Assert.assertEquals(0, test.getOverload(index, item));
    }

    @Test
    public void testSetType() throws Exception {
        ItemStack item = mock(ItemStack.class);
        String type = "STONE";
        Material material = Material.valueOf(type);

        when(item.getType()).thenReturn(material);

        JsTest test = new ExecutorTest(engine, "SETTYPE");

        test.withArgs(type, item).test();

        verify(item).setType(material);

        Assert.assertEquals(0, test.getOverload(type, item));
    }

    @Test
    public void testSetWalkSpeed1() throws Exception {
        Player player = mock(Player.class);
        Float speed = 0.5F;

        JsTest test = new ExecutorTest(engine, "SETWALKSPEED")
                .addVariable("player", player);

        test.withArgs(speed).test();

        verify(player).setWalkSpeed(speed);

        Assert.assertEquals(0, test.getOverload(speed));
    }

    @Test
    public void testSetWalkSpeed2() throws Exception {
        Player player = mock(Player.class);
        Float speed = -0.5F;

        JsTest test = new ExecutorTest(engine, "SETWALKSPEED");

        test.withArgs(player, speed).test();

        verify(player).setWalkSpeed(speed);

        Assert.assertEquals(1, test.getOverload(player, speed));
    }

    @Test(expected = ValidationException.class)
    public void testSetWalkSpeed3() throws Exception {
        Player player = mock(Player.class);
        Float speed = 10F;

        JsTest test = new ExecutorTest(engine, "SETWALKSPEED")
                .addVariable("player", player);

        test.withArgs(player, speed).test();
    }

    @Test
    public void testSetXp1() throws Exception {
        Player player = mock(Player.class);
        Float xp = 0.5F;

        JsTest test = new ExecutorTest(engine, "SETXP")
                .addVariable("player", player);

        test.withArgs(xp).test();

        verify(player).setExp(xp);

        Assert.assertEquals(0, test.getOverload(xp));
    }

    @Test
    public void testSetXp2() throws Exception {
        Player player = mock(Player.class);
        Float xp = 1F;

        JsTest test = new ExecutorTest(engine, "SETXP");

        test.withArgs(player, xp).test();

        verify(player).setExp(xp);

        Assert.assertEquals(1, test.getOverload(player, xp));
    }

    @Test(expected = ValidationException.class)
    public void testSetXp3() throws Exception {
        Player player = mock(Player.class);
        Float xp = 10F;

        JsTest test = new ExecutorTest(engine, "SETXP")
                .addVariable("player", player);

        test.withArgs(player, xp).test();
    }

    @Test
    public void testSignEdit1() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        Sign sign = mock(Sign.class);
        int line = 2;
        String text = "&aSign Edit Executor";
        String colorized = ChatColor.translateAlternateColorCodes('&', text);

        when(location.getBlock()).thenReturn(block);
        when(block.getState()).thenReturn(sign);

        JsTest test = new ExecutorTest(engine, "SIGNEDIT");

        test.withArgs(line, text, location).test();

        verify(sign).setLine(line, colorized);
        verify(sign).update();

        Assert.assertEquals(0, test.getOverload(line, text, location));
    }

    @Test(expected = ValidationException.class)
    public void testSignEdit2() throws Exception {
        Location location = mock(Location.class);
        int line = 5;
        String text = "&aSign Edit Executor";

        JsTest test = new ExecutorTest(engine, "SIGNEDIT");

        test.withArgs(line, text, location).test();
    }

    @Test
    public void testSound_string() throws Exception {
        // arrange
        Location location = mock(Location.class);
        String sound = "abc";
        float volume = 0.5F;
        float pitch = -0.5F;

        Player player = mock(Player.class);

        JsTest test = new ExecutorTest(engine, "SOUND")
                .addVariable("player", player)
                .withArgs(location, sound, volume, pitch);

        // act
        test.test();

        // assert
    }

    @Test
    public void testSpawn1() throws Exception {
        Player player = mock(Player.class);
        String entity = "pig";

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SPAWN")
                .addVariable("player", player);

        test.withArgs(entity).test();

        verify(world).spawnEntity(location, EntityType.valueOf(entity.toUpperCase()));

        Assert.assertEquals(0, test.getOverload(entity));
    }

    @Test
    public void testSpawn2() throws Exception {
        Location location = mock(Location.class);
        EntityType entity = EntityType.PIG;

        World world = mock(World.class);

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "SPAWN");

        test.withArgs(location, entity).test();

        verify(world).spawnEntity(location, entity);

        Assert.assertEquals(3, test.getOverload(location, entity));
    }

    @Test
    public void testTime1() throws Exception {
        Player player = mock(Player.class);
        int time = 1000 * 12;

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "TIME")
                .addVariable("player", player);

        test.withArgs(time).test();

        verify(world).setTime(time);

        Assert.assertEquals(0, test.getOverload(time));
    }

    @Test
    public void testTime2() throws Exception {
        World world = mock(World.class);
        int time = 1000 * 12;

        JsTest test = new ExecutorTest(engine, "TIME");

        test.withArgs(world, time).test();

        verify(world).setTime(time);

        Assert.assertEquals(2, test.getOverload(world, time));
    }

    @Test
    public void testTp1() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "TP")
                .addVariable("player", player);

        test.withArgs(location).test();

        verify(player).teleport(location);

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testTp2() throws Exception {
        Player player = mock(Player.class);
        int x = 100;
        int y = 50;
        int z = -100;

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "TP")
                .addVariable("player", player);

        test.withArgs(x, y, z).test();

        verify(player).teleport(new Location(world, x, y, z));

        Assert.assertEquals(2, test.getOverload(x, y, z));
    }

    @Test
    public void testTp3() throws Exception {
        Player player = mock(Player.class);
        int x = 100;
        int y = 50;
        int z = -100;
        float yaw = 10.5F;
        float pitch = -30.0F;

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "TP");

        test.withArgs(x, y, z, yaw, pitch, player).test();

        verify(player).teleport(new Location(world, x, y, z, yaw, pitch));

        Assert.assertEquals(6, test.getOverload(x, y, z, yaw, pitch, player));
    }

    @Test
    public void testTppos1() throws Exception {
        Player player = mock(Player.class);
        int x = 100;
        int y = 50;
        int z = -100;

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "TPPOS")
                .addVariable("player", player);

        test.withArgs(x, y, z).test();

        verify(player).teleport(new Location(world, x, y, z));

        Assert.assertEquals(0, test.getOverload(x, y, z));
    }

    @Test
    public void testTppos2() throws Exception {
        Player player = mock(Player.class);
        double px = 100;
        double py = 50;
        double pz = -100;

        String x = "~100";
        String y = "20";
        String z = "~-50";

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);
        when(location.getX()).thenReturn(px);
        when(location.getY()).thenReturn(py);
        when(location.getZ()).thenReturn(pz);

        JsTest test = new ExecutorTest(engine, "TPPOS");

        test.withArgs(player, x, y, z).test();

        verify(player).teleport(new Location(world, 200, 20, -150));

        Assert.assertEquals(3, test.getOverload(player, x, y, z));
    }

    @Test
    public void testVelocity1() throws Exception {
        Player player = mock(Player.class);
        int x = 10;
        int y = 5;
        int z = -10;

        JsTest test = new ExecutorTest(engine, "VELOCITY")
                .addVariable("player", player);

        test.withArgs(x, y, z).test();

        verify(player).setVelocity(new Vector(x, y, z));

        Assert.assertEquals(0, test.getOverload(x, y, z));
    }

    @Test
    public void testVelocity2() throws Exception {
        Entity entity = mock(Entity.class);
        int x = 10;
        int y = 5;
        int z = -10;

        JsTest test = new ExecutorTest(engine, "VELOCITY");

        test.withArgs(entity, x, y, z).test();

        verify(entity).setVelocity(new Vector(x, y, z));

        Assert.assertEquals(1, test.getOverload(entity, x, y, z));
    }

    @Test
    public void testVelocity3() throws Exception {
        Entity entity = mock(Entity.class);
        double x = 10.5;
        int y = 5;
        int z = -10;

        JsTest test = new ExecutorTest(engine, "VELOCITY");

        test.withArgs(entity, x, y, z).test();

        verify(entity).setVelocity(new Vector(x, y, z));

        Assert.assertEquals(1, test.getOverload(entity, x, y, z));
    }

    @Test
    public void testWeather1() throws Exception {
        Player player = mock(Player.class);
        boolean isStorm = true;

        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "WEATHER")
                .addVariable("player", player);

        test.withArgs(isStorm).test();

        verify(world).setStorm(isStorm);

        Assert.assertEquals(0, test.getOverload(isStorm));
    }

    @Test
    public void testWeather2() throws Exception {
        World world = mock(World.class);
        boolean isStorm = true;

        JsTest test = new ExecutorTest(engine, "WEATHER");

        test.withArgs(world, isStorm).test();

        verify(world).setStorm(isStorm);

        Assert.assertEquals(2, test.getOverload(world, isStorm));
    }
}
