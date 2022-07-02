package js.executor;

import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactorCore;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.InventoryTriggerManager;
import js.AbstractTestJavaScripts;
import js.ExecutorTest;
import js.JsTest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.material.Lever;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
}