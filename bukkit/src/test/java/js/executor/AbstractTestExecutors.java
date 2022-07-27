package js.executor;

import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactorCore;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import js.AbstractTestJavaScripts;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
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

import static io.github.wysohn.triggerreactor.core.utils.TestUtil.assertJSError;
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

        String message = "&aMessage";
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

        verify(world).createExplosion(location, 4.0F);

        Assert.assertEquals(0, test.getOverload(location));
    }

    @Test
    public void testExplosion2() throws Exception {
        Location location = mock(Location.class);
        World world = mock(World.class);
        float power = 4;

        when(location.getWorld()).thenReturn(world);

        JsTest test = new ExecutorTest(engine, "EXPLOSION");

        test.withArgs(location, power).test();

        verify(world).createExplosion(location, power);

        Assert.assertEquals(1, test.getOverload(location, power));
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
        Player player = mock(Player.class);
        ItemStack itemStack = mock(ItemStack.class);
        PlayerInventory inventory = mock(PlayerInventory.class);

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "GIVE")
                .addVariable("player", player);

        test.withArgs(itemStack).test();

        verify(inventory).addItem(itemStack);

        Assert.assertEquals(0, test.getOverload(itemStack));
    }

    @Test
    public void testGive2() throws Exception {
        Player player = mock(Player.class);
        ItemStack itemStack = mock(ItemStack.class);
        PlayerInventory inventory = mock(PlayerInventory.class);

        when(player.getInventory()).thenReturn(inventory);

        JsTest test = new ExecutorTest(engine, "GIVE");

        test.withArgs(player, itemStack).test();

        verify(inventory).addItem(itemStack);

        Assert.assertEquals(1, test.getOverload(player, itemStack));
    }

    @Test
    public void testGui1() throws Exception {
        Player player = mock(Player.class);
        String guiName = "TESTGUI";

        BukkitTriggerReactorCore triggerReactorCore = mock(BukkitTriggerReactorCore.class);
        InventoryTriggerManager inventoryTriggerManager = mock(InventoryTriggerManager.class);

        when(triggerReactorCore.getInvManager()).thenReturn(inventoryTriggerManager);

        JsTest test = new ExecutorTest(engine, "GUI")
                .addVariable("player", player)
                .addVariable("plugin", triggerReactorCore);

        test.withArgs(guiName).test();

        verify(inventoryTriggerManager).openGUI(player, guiName);

        Assert.assertEquals(0, test.getOverload(guiName));
    }

    @Test
    public void testGui2() throws Exception {
        Player player = mock(Player.class);
        String guiName = "TESTGUI";

        BukkitTriggerReactorCore triggerReactorCore = mock(BukkitTriggerReactorCore.class);
        InventoryTriggerManager inventoryTriggerManager = mock(InventoryTriggerManager.class);

        when(triggerReactorCore.getInvManager()).thenReturn(inventoryTriggerManager);

        JsTest test = new ExecutorTest(engine, "GUI")
                .addVariable("plugin", triggerReactorCore);

        test.withArgs(player, guiName).test();

        verify(inventoryTriggerManager).openGUI(player, guiName);

        Assert.assertEquals(1, test.getOverload(player, guiName));
    }

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
    public void testLog() throws Exception {
        class FakeConsoleSender {
            public void sendMessage(String message) {}
        }

        class FakeBukkit {
            public synchronized FakeConsoleSender getConsoleSender() {
                return null;
            }
        }
        class FakeJava {
            public FakeBukkit type(String name) {
                return null;
            }
        }

        FakeConsoleSender consoleSender = mock(FakeConsoleSender.class);
        FakeBukkit bukkit = mock(FakeBukkit.class);
        FakeJava java = mock(FakeJava.class);

        String message = "Message";

        when(java.type("org.bukkit.Bukkit")).thenReturn(bukkit);
        when(bukkit.getConsoleSender()).thenReturn(consoleSender);

        JsTest test = new ExecutorTest(engine, "LOG")
                .addVariable("Java", java);

        test.withArgs(message).test();

        verify(consoleSender).sendMessage(message);

        Assert.assertEquals(0, test.getOverload(message));
    }

    @Test
    public void testMessage() throws Exception {
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

        JsTest test = new ExecutorTest(engine, "MESSAGE")
                .addVariable("player", player);

        test.withArgs(exampleObject).test();

        verify(player).sendMessage(exampleObject.toString());

        Assert.assertEquals(0, test.getOverload(exampleObject));
    }

    // TODO
    @Test
    public void testModifyHeldItem() throws Exception {}

    @Test
    public void testMoney1() throws Exception {
        class FakeVault {
            public void give(Player player, int money) {}

            public void take(Player player, int money) {}
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
            public void give(Player player, int money) {}

            public void take(Player player, int money) {}
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
        class FakeMysqlHelper {
            public void set(String key, Object value) {}
        }

        class FakePlugin {
            public FakeMysqlHelper getMysqlHelper() {
                return null;
            }
        }

        FakeMysqlHelper mysqlHelper = mock(FakeMysqlHelper.class);
        FakePlugin plugin = mock(FakePlugin.class);

        String key = "testKey";
        Object value = 100D;

        when(plugin.getMysqlHelper()).thenReturn(mysqlHelper);

        JsTest test = new ExecutorTest(engine, "MYSQL")
                .addVariable("plugin", plugin);

        test.withArgs(key, value).test();

        verify(mysqlHelper).set(key, value);

        Assert.assertEquals(0, test.getOverload(key, value));
    }

    @Test
    public void testPermission1() throws Exception {
        class FakeVault {
            public void permit(Player player, String permission) {}

            public void revoke(Player player, String permission) {}
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
            public void permit(Player player, String permission) {}

            public void revoke(Player player, String permission) {}
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

    // TODO
     @Test
     public void testPotion() throws Exception {}

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
        float x = 10.0F;
        float y = 5.75F;
        float z = -10.5F;

        JsTest test = new ExecutorTest(engine, "PUSH");

        test.withArgs(entity, x, y, z).test();

        verify(entity).setVelocity(new Vector(x, y, z));

        Assert.assertEquals(1, test.getOverload(entity, x, y, z));
    }

    // TODO
    @Test
    public void testRotateBlock() throws Exception {}

    // TODO
     @Test
     public void testScoreboard() throws Exception {}

    @Test
    public void testServer1() throws Exception {
        class FakeBungeeHelper {
            public void sendToServer(Player player, String server) {}
        }

        class FakePlugin {
            public FakeBungeeHelper getBungeeHelper() {
                return null;
            }
        }

        FakeBungeeHelper bungeeHelper = mock(FakeBungeeHelper.class);
        FakePlugin plugin = mock(FakePlugin.class);

        Player player = mock(Player.class);
        String server = "SecondServer";

        when(plugin.getBungeeHelper()).thenReturn(bungeeHelper);

        JsTest test = new ExecutorTest(engine, "SERVER")
                .addVariable("plugin", plugin)
                .addVariable("player", player);

        test.withArgs(server).test();

        verify(bungeeHelper).sendToServer(player, server);

        Assert.assertEquals(0, test.getOverload(server));
    }

    @Test
    public void testServer2() throws Exception {
        class FakeBungeeHelper {
            public void sendToServer(Player player, String server) {}
        }

        class FakePlugin {
            public FakeBungeeHelper getBungeeHelper() {
                return null;
            }
        }

        FakeBungeeHelper bungeeHelper = mock(FakeBungeeHelper.class);
        FakePlugin plugin = mock(FakePlugin.class);

        Player player = mock(Player.class);
        String server = "SecondServer";

        when(plugin.getBungeeHelper()).thenReturn(bungeeHelper);

        JsTest test = new ExecutorTest(engine, "SERVER")
                .addVariable("plugin", plugin);

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
}