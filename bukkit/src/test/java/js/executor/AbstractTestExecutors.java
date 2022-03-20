package js.executor;

//import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;

import io.github.wysohn.triggerreactor.bukkit.main.BukkitBungeeCordHelper;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactor;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import js.AbstractTestJavaScripts;
import js.ExecutorTest;
import js.JsTest;
import junit.framework.Assert;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Lever;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.wysohn.triggerreactor.core.utils.TestUtil.assertJSError;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * Test driving class for testing Executors.
 */

public abstract class AbstractTestExecutors extends AbstractTestJavaScripts {
    @Test
    public abstract void testMoney() throws Exception;

    @Test
    public abstract void testRotateBlock() throws Exception;

    @Test
    public abstract void testSignEdit() throws Exception;

    @Test
    public void testActionBar() throws Exception {
        //TODO
    }

    @Test
    public void testBroadcast() throws Exception {
        Collection<Player> players = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            players.add(mock(Player.class));
        }

        String message = "&cHey all";
        String colored = ChatColor.translateAlternateColorCodes('&', message);

        doReturn(players).when(server).getOnlinePlayers();

        new ExecutorTest(localContext, "BROADCAST").withArgs(message).test();
        for (Player mockPlayer : players) {
            verify(mockPlayer).sendMessage(argThat((String s) -> colored.equals(s)));
        }
    }

    @Test
    public void testBurn() throws Exception {
        //happy cases

        Player mockPlayer = mock(Player.class);
        Entity mockEntity = mock(Entity.class);
        JsTest test = new ExecutorTest(localContext, "BURN").addVariable("player", mockPlayer);

        test.withArgs(3).test();
        verify(mockPlayer).setFireTicks(60);

        test.withArgs(0.101).test();
        verify(mockPlayer).setFireTicks(2);

        test.withArgs(mockEntity, 1).test();
        verify(mockEntity).setFireTicks(20);

        when(server.getPlayer("merp")).thenReturn(mockPlayer);
        test.withArgs("merp", 5).test();
        verify(mockPlayer).setFireTicks(100);

        //sad cases
        when(server.getPlayer("merp")).thenReturn(null);
        assertJSError(() -> test.withArgs(-1).test(), "The number of seconds to burn should be positive");
        assertJSError(() -> test.withArgs().test(),
                "Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]");
        assertJSError(() -> test.withArgs(1, 1, 1).test(),
                "Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]");
        assertJSError(() -> test.withArgs(true).test(), "Invalid number for seconds to burn: true");
        assertJSError(() -> test.withArgs(null, 4).test(), "player to burn should not be null");
        assertJSError(() -> test.withArgs("merp", 3).test(), "player to burn does not exist");
        assertJSError(() -> test.withArgs(3, 3).test(), "invalid entity to burn: 3");
        assertJSError(() -> test.withArgs(mockEntity, "merp").test(),
                "The number of seconds to burn should be a number");
        assertJSError(() -> test.withArgs(mockEntity, -1).test(), "The number of seconds to burn should be positive");
    }

    @Test
    public void testClearChat() throws Exception {
        Player player = mock(Player.class);
        Player player2 = mock(Player.class);
        Player nullP = null;
        JsTest test = new ExecutorTest(localContext, "CLEARCHAT").addVariable("player", player);

        //case1
        test.withArgs().test();
        verify(player, times(30)).sendMessage("");

        //case2
        test.withArgs(player2).test();
        verify(player2, times(30)).sendMessage("");

        //Unexpected Cases
        assertJSError(() -> test.withArgs(nullP).test(), "Found unexpected parameter - player: null");
        assertJSError(() -> test.withArgs(1, 2).test(),
                "Too many parameters found! CLEARCHAT accept up to one parameter.");
    }

    @Test
    public void testClearEntity() throws Exception {
        Player player = mock(Player.class);
        List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entities.add(mock(Entity.class));
        }
        JsTest test = new ExecutorTest(localContext, "CLEARENTITY").addVariable("player", player);
        when(player.getNearbyEntities(2d, 2d, 2d)).thenReturn(entities);
        test.withArgs(2).test();
        for (Entity ve : entities) {
            verify(ve).remove();
        }
        assertJSError(() -> test.withArgs().test(), "Invalid parameters! [Number]");
        assertJSError(() -> test.withArgs("NO").test(), "Invalid parameters! [Number]");
    }

    @Test
    public void testClearPotion() throws Exception {
        Player p = mock(Player.class);
        ExecutorTest test = new ExecutorTest(localContext, "CLEARPOTION");
        test.addVariable("player", p);
        test.test();

        Assert.assertEquals(0, test.getOverload());
        Assert.assertEquals(1, test.getOverload("SPEED"));

        Assert.assertFalse(test.isValid(0));
        Assert.assertFalse(test.isValid("SPEED", "SPEED"));
    }

    @Test
    public void testCloseGUI() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "CLOSEGUI").addVariable("player", player);

        //only happy case
        test.withArgs().test();
        verify(player).closeInventory();
    }

    @Test
    public void testCmd() throws Exception {
        Player player = mock(Player.class);
        when(player.getServer()).thenReturn(server);

        JsTest test = new ExecutorTest(localContext, "CMD").addVariable("player", player);

        //only happy case
        test.withArgs("some command line").test();
        verify(server).dispatchCommand(player, "some command line");
    }

    @Test
    public void testCmdCon() throws Exception {
        Player player = mock(Player.class);
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);

        when(player.getServer()).thenReturn(server);
        when(server.getConsoleSender()).thenReturn(sender);

        new ExecutorTest(localContext, "CMDCON").withArgs("some command line").test();

        verify(server).dispatchCommand(sender, "some command line");
    }

    @Test
    public void testDoorClose() throws Exception {
        //TODO
    }

    @Test
    public void testDoorOpen() throws Exception {
        //TODO
    }

    @Test
    public void testDoorToggle() throws Exception {
        //TODO
    }

    @Test
    public void testDropItem() throws Exception {
        ItemFactory factory = mock(ItemFactory.class);
        Player player = mock(Player.class);
        World world = mock(World.class);

        when(server.getItemFactory()).thenReturn(factory);
        when(player.getWorld()).thenReturn(world);
        when(factory.equals(any(), any())).thenReturn(true);

        new ExecutorTest(localContext, "DROPITEM").addVariable("player", player)
                .withArgs("STONE", 1, "None", -204, 82, 266)
                .test();

        verify(world).dropItem(new Location(world, -204, 82, 266), new ItemStack(Material.STONE));
    }

    @Test
    public void testExplosion() throws Exception {
        World world = mock(World.class);

        when(server.getWorld(anyString())).thenReturn(world);

        new ExecutorTest(localContext, "EXPLOSION").withArgs("world", 101.2, 33.4, 55, 2.9, false).test();

        verify(world).createExplosion(new Location(world, 101.2, 33.4, 55), 2.9F, false);
    }

    @Test
    public void testFallingBlock() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);

        when(player.getWorld()).thenReturn(world);

        new ExecutorTest(localContext, "FALLINGBLOCK").addVariable("player", player)
                .withArgs("STONE", 44.5, 6, 78.9)
                .test();

        verify(world).spawnFallingBlock(new Location(world, 44.5, 6, 78.9), Material.STONE, (byte) 0);
    }

    private static class MockAPI{
        public InventoryTriggerManager getInventoryTriggerManager() {
            return null;
        }
    }

    @Test
    public void testGUI() throws Exception {
        IPlayer vip = mock(IPlayer.class);
        MockAPI api = mock(MockAPI.class);
        InventoryTriggerManager invManager = mock(InventoryTriggerManager.class);
        IInventory iInv = mock(IInventory.class);
        JsTest test = new ExecutorTest(localContext, "GUI").addVariable("player", vip).addVariable("api", api);

        when(api.getInventoryTriggerManager()).thenReturn(invManager);
        when(invManager.openGUI(vip, "Hi")).thenReturn(iInv);
        test.withArgs("Hi").test();
        verify(invManager).openGUI(vip, "Hi");

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [String]");
        when(invManager.openGUI(vip, "hello")).thenReturn(null);
        assertJSError(() -> test.withArgs("hello").test(), "No such Inventory Trigger named hello");
    }

    @Test
    public void testGive() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory playerInv = mock(PlayerInventory.class);
        ItemStack vItem = mock(ItemStack.class);
        JsTest test = new ExecutorTest(localContext, "GIVE").addVariable("player", player);

        when(player.getInventory()).thenReturn(playerInv);
        when(playerInv.firstEmpty()).thenReturn(4);
        test.withArgs(vItem).test();
        verify(playerInv).addItem(vItem);

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [ItemStack]");
        when(playerInv.firstEmpty()).thenReturn(-1);
        assertJSError(() -> test.withArgs(vItem).test(), "Player has no empty slot.");
        when(playerInv.firstEmpty()).thenReturn(7);
        assertJSError(() -> test.withArgs("hi").test(), "Invalid ItemStack: hi");
    }

    @Test
    public void testItemFrameRotate() throws Exception {
        Location vLoc = mock(Location.class);
        Location vLoc2 = mock(Location.class);
        Block vBlock = mock(Block.class);
        World vWorld = mock(World.class);
        List<Entity> vEntities = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            vEntities.add(mock(ItemFrame.class));
        }
        JsTest test = new ExecutorTest(localContext, "ITEMFRAMEROTATE");

        when(vLoc.getBlock()).thenReturn(vBlock);
        when(vBlock.getWorld()).thenReturn(vWorld);
        when(vBlock.getLocation()).thenReturn(vLoc2);
        when(vWorld.getNearbyEntities(vLoc2, 2.0, 2.0, 2.0)).thenReturn(vEntities);

        test.withArgs("NOne", vLoc).test();
        for (Entity entity : vEntities) {
            verify((ItemFrame) entity).setRotation(Rotation.valueOf("NOne".toUpperCase()));
        }
        assertJSError(() -> test.withArgs().test(),
                "Invalid parameters. Need [Rotation<string>, Location<location or number number number>]");

        //TODO - need test for the situation of args.length == 4
    }

    @Test
    public void testItemFrameSet() throws Exception {
        //TODO
    }

    @Test
    public void testKick() throws Exception {

        Player player = mock(Player.class);
        Player player2 = mock(Player.class);
        Player nullP = null;
        String msg = ChatColor.translateAlternateColorCodes('&', "&c[TR] You've been kicked from the server.");
        String msg2 = ChatColor.translateAlternateColorCodes('&', "&cKICKED");

        //case1
        ExecutorTest test = new ExecutorTest(localContext, "KICK");
        test.addVariable("player", player);
        test.withArgs().test();
        verify(player).kickPlayer(msg);

        //case2
        test.withArgs(msg2).test();
        verify(player).kickPlayer(msg2);

        //case3
        test.withArgs(player2).test();
        verify(player2).kickPlayer(msg);

        //case4
        test.withArgs(player2, msg2).test();
        verify(player2).kickPlayer(msg2);

        //Unexpected Exception Cases
        test.assertInvalid(1);
        test.assertInvalid(player, 232);
        test.assertInvalid(1, 2, 3);
        test.addVariable("player", null);

        test.assertInvalid(null, "msg");
        test.assertInvalid(nullP);
        assertJSError(() -> test.withArgs().test(),
                "Too few arguments! You should enter at least on argument if you use KICK executor from console.");
    }

    @Test
    public void testKill() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "KILL").addVariable("player", player);

        test.withArgs().test();
        verify(player).setHealth(0d);
    }

    @Test
    public void testLeverOff() throws Exception {

        Location vLoc = mock(Location.class);
        Block vBlock = mock(Block.class);
        BlockState vBS = mock(BlockState.class);
        Lever vLever = mock(Lever.class);
        JsTest test = new ExecutorTest(localContext, "LEVEROFF");

        when(vLoc.getBlock()).thenReturn(vBlock);
        when(vBlock.getState()).thenReturn(vBS);
        when(vBS.getData()).thenReturn(vLever);
        test.withArgs(vLoc).test();
        verify(vLever).setPowered(false);

        assertJSError(() -> test.withArgs().test(),
                "Invalid parameters. Need [Location<location or number number number>]");
        //TODO - need test for the situation of args.length == 3
    }

    @Test
    public void testLeverOn() throws Exception {

        Location vLoc = mock(Location.class);
        Block vBlock = mock(Block.class);
        BlockState vBS = mock(BlockState.class);
        Lever vLever = mock(Lever.class);
        JsTest test = new ExecutorTest(localContext, "LEVERON");

        when(vLoc.getBlock()).thenReturn(vBlock);
        when(vBlock.getState()).thenReturn(vBS);
        when(vBS.getData()).thenReturn(vLever);
        test.withArgs(vLoc).test();
        verify(vLever).setPowered(true);

        assertJSError(() -> test.withArgs().test(),
                "Invalid parameters. Need [Location<location or number number number>]");
        //TODO - need test for the situation of args.length == 3
    }

    @Test
    public void testLeverToggle() throws Exception {

        Location vLoc = mock(Location.class);
        Block vBlock = mock(Block.class);
        BlockState vBS = mock(BlockState.class);
        Lever vLever = mock(Lever.class);
        JsTest test = new ExecutorTest(localContext, "LEVERTOGGLE");

        when(vLoc.getBlock()).thenReturn(vBlock);
        when(vBlock.getState()).thenReturn(vBS);
        when(vBS.getData()).thenReturn(vLever);

        //case1
        when(vLever.isPowered()).thenReturn(false);
        test.withArgs(vLoc).test();
        verify(vLever).setPowered(true);
        //case2
        when(vLever.isPowered()).thenReturn(true);
        test.withArgs(vLoc).test();
        verify(vLever).setPowered(false);

        assertJSError(() -> test.withArgs().test(),
                "Invalid parameters. Need [Location<location or number number number>]");
        //TODO - need test for the situation of args.length == 3
    }

    @Test
    public void testLightning() throws Exception {
        Location vLoc = mock(Location.class);
        World vWorld = mock(World.class);
        JsTest test = new ExecutorTest(localContext, "LIGHTNING");

        when(vLoc.getWorld()).thenReturn(vWorld);
        test.withArgs(vLoc).test();
        verify(vWorld).strikeLightning(vLoc);

        assertJSError(() -> test.withArgs().test(),
                "Invalid parameters! [String, Number, Number, Number] or [Location]");
        assertJSError(() -> test.withArgs("hff").test(),
                "Invalid parameters! [String, Number, Number, Number] or [Location]");
        //TODO - need test for the situation of args.length == 4
    }

    @Test
    public void testLog() throws Exception {
        //no way to test window.print()
    }

    @Test
    public void testMessage() throws Exception {
        Player mockPlayer = mock(Player.class);

        new ExecutorTest(localContext, "MESSAGE").addVariable("player", mockPlayer).withArgs("&cTest Message").test();

        String expected = ChatColor.translateAlternateColorCodes('&', "&cTest Message");
        verify(mockPlayer).sendMessage(argThat((String str) -> expected.equals(str)));
    }

    @Test
    public void testMessageMultiThreaded() throws Exception {
        Player mockPlayer = mock(Player.class);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        ScriptEngineManager manager = new ScriptEngineManager();

        when(taskSupervisor.isServerThread()).thenReturn(false);
        when(mockMain.callSyncMethod(any(Callable.class))).then(invocation -> {
            Callable call = invocation.getArgument(0);
            return pool.submit(call);
        });

        // represents one trigger executing an executor
        InterpreterLocalContext context1 = new InterpreterLocalContext(Timings.LIMBO);
        context1.setExtra(Interpreter.SCRIPT_ENGINE_KEY, manager.getEngineByExtension("js"));
        Runnable run1 = () -> {
            JsTest test = null;
            try {
                test = new ExecutorTest(context1, "MESSAGE")
                        .addVariable("player", mockPlayer)
                        .withArgs("&cTest Message");
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 1000; i++) {
                try {
                    test.test();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // represents one trigger executing an executor
        InterpreterLocalContext context2 = new InterpreterLocalContext(Timings.LIMBO);
        context2.setExtra(Interpreter.SCRIPT_ENGINE_KEY, manager.getEngineByExtension("js"));
        assertNotEquals(context1.getExtra(Interpreter.SCRIPT_ENGINE_KEY),
                context2.getExtra(Interpreter.SCRIPT_ENGINE_KEY));
        Runnable run2 = () -> {
            JsTest test = null;
            try {
                test = new ExecutorTest(context2, "MESSAGE")
                        .addVariable("player", mockPlayer)
                        .withArgs("&cTest Message");
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 1000; i++) {
                try {
                    test.test();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread.UncaughtExceptionHandler handler = mock(Thread.UncaughtExceptionHandler.class);

        Thread thread1 = new Thread(run1);
        thread1.setUncaughtExceptionHandler(handler);
        Thread thread2 = new Thread(run2);
        thread2.setUncaughtExceptionHandler(handler);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        verify(handler, never()).uncaughtException(any(), any());

        String expected = ChatColor.translateAlternateColorCodes('&', "&cTest Message");
        verify(mockPlayer, times(2000)).sendMessage(argThat((ArgumentMatcher<String>) expected::equals));
    }

    @Test
    public void testMessageNull() throws Exception {
        Player mockPlayer = mock(Player.class);

        new ExecutorTest(localContext, "MESSAGE").addVariable("player", mockPlayer).withArgs(new Object[]{null}).test();

        String expected = "null";
        verify(mockPlayer).sendMessage(argThat((String str) -> expected.equals(str)));
    }

    @Test
    public void testModifyHeldItem() throws Exception {
        Player player = mock(Player.class);
        ItemStack held = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);

        when(player.getItemInHand()).thenReturn(held);
        when(held.getType()).thenReturn(Material.STONE);
        when(held.getItemMeta()).thenReturn(meta);

        new ExecutorTest(localContext, "MODIFYHELDITEM").addVariable("player", player)
                .withArgs("TITLE", "some title")
                .test();

        verify(meta).setDisplayName("some title");
    }

    @Test
    public void testModifyPlayer() throws Exception {
        //No longer supported
    }

    @Test
    public void testMysql() throws Exception {
        //TODO
    }

    @Test
    public void testPermission() throws Exception {
        //TODO
    }

    @Test
    public void testPlayer_SetFlyMode() throws Exception {
        Player mockPlayer = mock(Player.class);

        JsTest test = new ExecutorTest(localContext, "SETFLYMODE").addVariable("player", mockPlayer);

        for (boolean b : new boolean[]{true, false}) {
            test.withArgs(b).test();
            verify(mockPlayer).setAllowFlight(eq(b));
            verify(mockPlayer).setFlying(eq(b));
        }

        assertJSError(() -> test.withArgs(true, true).test(), "Incorrect number of arguments for executor SETFLYMODE");
        assertJSError(() -> test.withArgs("merp").test(), "Invalid argument for executor SETFLYMODE: merp");
    }

    @Test
    public void testPlayer_SetFlySpeed() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETFLYSPEED").addVariable("player", player);

        //only case
        test.withArgs(0.5).test();
        verify(player).setFlySpeed(0.5F);

        //Unexpected cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETFLYSPEED");
        assertJSError(() -> test.withArgs(0.5, 13).test(), "Incorrect Number of arguments for Executor SETFLYSPEED");
        assertJSError(() -> test.withArgs("HI").test(), "Invalid argument for SETFLYSPEED: HI");
        assertJSError(() -> test.withArgs(4).test(), "Argument for Executor SETFLYSPEED is outside of range -1..1");
        assertJSError(() -> test.withArgs(-4).test(), "Argument for Executor SETFLYSPEED is outside of range -1..1");
    }

    @Test
    public void testPlayer_SetFood() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETFOOD").addVariable("player", player);


        //case1
        test.withArgs(3).test();
        verify(player).setFoodLevel(3);

        //case2
        test.withArgs(4.0).test();
        verify(player).setFoodLevel(4);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETFOOD");
        assertJSError(() -> test.withArgs("HI").test(), "Invalid argument for Executor SETFOOD: HI");
        assertJSError(() -> test.withArgs(3.4).test(), "Argument for Executor SETFOOD should be a whole number");
        assertJSError(() -> test.withArgs(-3.0).test(), "Argument for Executor SETFOOD should not be negative");
    }

    @Test
    public void testPlayer_SetGameMode() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETGAMEMODE").addVariable("player", player);

        //only case
        test.withArgs("creative").test();
        verify(player).setGameMode(GameMode.valueOf("CREATIVE"));

        //case2
        test.withArgs(2).test();
        verify(player).setGameMode(GameMode.valueOf("ADVENTURE"));

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect number of arguments for executor SETGAMEMODE");
        assertJSError(() -> test.withArgs(34).test(), "Invalid argument for Executor SETGAMEMODE: 34");
        assertJSError(() -> test.withArgs("hElLo").test(), "Unknown GAEMMODE value hElLo");
    }

    @Test
    public void testPlayer_SetHealth() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETHEALTH").addVariable("player", player);
        when(player.getMaxHealth()).thenReturn(20.0);

        //case1
        test.withArgs(2).test();
        verify(player).setHealth(2.0);

        //case2
        test.withArgs(3.0).test();
        verify(player).setHealth(3.0);

        //Unexpected Cases
        assertJSError(() -> test.withArgs(1, 334).test(), "Incorrect Number of arguments for executor SETHEALTH");
        assertJSError(() -> test.withArgs("yeah").test(), "Invalid argument for SETHEALTH: yeah");
        assertJSError(() -> test.withArgs(-17).test(), "Argument for Exector SETHEALTH should not be negative");
        assertJSError(() -> test.withArgs(50).test(), "Argument for Executor SETHEALTH is greater than the max health");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPlayer_SetMaxHealth() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETMAXHEALTH").addVariable("player", player);

        //case1
        test.withArgs(30).test();
        verify(player).setMaxHealth(30.0);

        //case2
        test.withArgs(35.4).test();
        verify(player).setMaxHealth(35.4);

        //Unexpected Cases
        assertJSError(() -> test.withArgs(20, 33).test(), "Incorrect Number of arguments for Executor SETMAXHEALTH");
        assertJSError(() -> test.withArgs("NONO").test(), "Invalid argument for SETMAXHEALTH: NONO");
        assertJSError(() -> test.withArgs(-30).test(),
                "Argument for Executor SETMAXHEALTH should not be negative or zero");
        assertJSError(() -> test.withArgs(2098).test(), "Maximum health cannot be greater than 2048");
    }

    @Test
    public void testPlayer_SetSaturation() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETSATURATION").addVariable("player", player);

        //case1
        test.withArgs(25).test();
        verify(player).setSaturation(25.0F);

        //case2
        test.withArgs(44.0).test();
        verify(player).setSaturation(44.0F);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETSATURATION");
        assertJSError(() -> test.withArgs("Hi").test(), "Invalid argument for SETSATURATION: Hi");
        assertJSError(() -> test.withArgs(-45).test(), "Argument for Executor SETSATURATION should not be negative");
    }

    @Test
    public void testPlayer_SetWalkSpeed() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETWALKSPEED").addVariable("player", player);
        //case1
        test.withArgs(1).test();
        verify(player).setWalkSpeed(1.0F);

        //case2
        test.withArgs(0.7).test();
        verify(player).setWalkSpeed(0.7F);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETWALKSPEED");
        assertJSError(() -> test.withArgs("NUU").test(), "Invalid argument for SETWALKSPEED: NUU");
        assertJSError(() -> test.withArgs(-3).test(),
                "Argument for Executor SETWALKSPEED is outside of the allowable range -1..1");
    }

    @Test
    public void testPlayer_SetXp() throws Exception {
        Player player = mock(Player.class);
        JsTest test = new ExecutorTest(localContext, "SETXP").addVariable("player", player);

        //case1
        test.withArgs(0.3).test();
        verify(player).setExp(0.3F);

        //case2
        test.withArgs(1).test();
        verify(player).setExp(1.0F);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect number of arguments for executor SETXP");
        assertJSError(() -> test.withArgs("lmao").test(), "Invalid argument for SETXP: lmao");
        assertJSError(() -> test.withArgs(33).test(),
                "33 is outside of the allowable range of 0..1 for executor SETXP");

    }

    @Test
    public void testPotion() throws Exception {
//        Player player = mock(Player.class);
//
//        new ExecutorTest(localContext, "POTION")
//                .addVariable("player", player)
//                .withArgs("LUCK", 100, 1)
//                .test();
//
//        verify(player).addPotionEffect(new PotionEffect(PotionEffectType.LUCK,
//                100, 1), true);
    }

    @Test
    public void testPush() throws Exception {
        //TODO
    }

    @Test
    public void testScoreboard() throws Exception {
        Player player = mock(Player.class);
        Scoreboard scoreboard = mock(Scoreboard.class);
        Team team = mock(Team.class);

        when(player.getScoreboard()).thenReturn(scoreboard);
        when(scoreboard.getTeam(anyString())).thenReturn(team);

        new ExecutorTest(localContext, "SCOREBOARD").withArgs("TEAM", "someteam", "ADD", "wysohn")
                .addVariable("player", player)
                .test();

        verify(team).addEntry("wysohn");
    }

    @Test
    public void testServer() throws Exception {
        BukkitTriggerReactor plugin = mock(BukkitTriggerReactor.class);
        BukkitBungeeCordHelper helper = mock(BukkitBungeeCordHelper.class);
        Player player = mock(Player.class);

        when(plugin.getBungeeHelper()).thenReturn(helper);

        new ExecutorTest(localContext, "SERVER").addVariable("player", player)
                .addVariable("plugin", plugin)
                .withArgs("someServer")
                .test();

        verify(helper).sendToServer(player, "someServer");
    }

    @Test
    public void testSetBlock() throws Exception {
        // {block id} and is used in Block related event
        World mockWorld = mock(World.class);
        Block mockBlock = mock(Block.class);

        JsTest test = new ExecutorTest(localContext, "SETBLOCK");
        test.addVariable("block", mockBlock);

        when(server.getWorld("world")).thenReturn(mockWorld);

        test.withArgs("STONE").test();

        verify(mockBlock).setType(eq(Material.STONE));
    }

    @Test
    public void testSetBlock1() throws Exception {
        // {block id} {x} {y} {z}
        World mockWorld = mock(World.class);
        Player player = mock(Player.class);

        when(player.getWorld()).thenReturn(mockWorld);
        when(server.getWorld("world")).thenReturn(mockWorld);

        assertJSError(() -> new ExecutorTest(localContext, "SETBLOCK").withArgs("STONE", 22, 80, 33).test(),
                "cannot use #SETBLOCK in non-player related event. Or use Location instance.");
    }

    @Test
    public void testSetBlock1_1() throws Exception {
        // {block id} {x} {y} {z}
        World mockWorld = mock(World.class);
        Player player = mock(Player.class);
        Block block = mock(Block.class);

        when(player.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getBlockAt(any(Location.class))).thenReturn(block);
        when(server.getWorld("world")).thenReturn(mockWorld);

        new ExecutorTest(localContext, "SETBLOCK").addVariable("player", player).withArgs("GLASS", 33, 96, -15).test();

        verify(block).setType(eq(Material.GLASS));
    }

    @Test
    public void testSetBlock1_2() throws Exception {
        // {block id} {block data} {x} {y} {z}
        World mockWorld = mock(World.class);
        Player player = mock(Player.class);
        Block block = mock(Block.class);

        when(player.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getBlockAt(any(Location.class))).thenReturn(block);
        when(server.getWorld("world")).thenReturn(mockWorld);

        new ExecutorTest(localContext, "SETBLOCK").addVariable("player", player)
                .withArgs("GLASS", 3, 33, 96, -15)
                .test();

        verify(block).setType(eq(Material.GLASS));
    }

    @Test
    public void testSetBlock2() throws Exception {
        // {block id} {Location instance}
        World mockWorld = mock(World.class);
        Player player = mock(Player.class);
        Block block = mock(Block.class);

        when(player.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getBlockAt(any(Location.class))).thenReturn(block);
        when(server.getWorld("world")).thenReturn(mockWorld);

        new ExecutorTest(localContext, "SETBLOCK").addVariable("player", player)
                .withArgs("GLASS", new Location(mockWorld, 33, 96, -15))
                .test();

        verify(block).setType(eq(Material.GLASS));
    }

    @Test
    public void testSetBlock2_1() throws Exception {
        // {block id} {block data} {Location instance}
        World mockWorld = mock(World.class);
        Player player = mock(Player.class);
        Block block = mock(Block.class);

        when(player.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getBlockAt(any(Location.class))).thenReturn(block);
        when(server.getWorld("world")).thenReturn(mockWorld);

        new ExecutorTest(localContext, "SETBLOCK").addVariable("player", player)
                .withArgs("GLASS", 2, new Location(mockWorld, 33, 96, -15))
                .test();

        verify(block).setType(eq(Material.GLASS));
    }

    @Test
    public void testSetCount() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        when(vItem.getType()).thenReturn(stone);

        ExecutorTest test = new ExecutorTest(localContext, "SETCOUNT");

        test.withArgs(3, vItem).test();

        verify(vItem).setAmount(3);

        test.assertValid(1, vItem);
        test.assertInvalid(1, 3);
        test.assertInvalid("h", "d");
        test.assertInvalid(vItem, 2);
    }

    @Test
    public void testSetHeldItem() throws Exception {
        Player player = mock(Player.class);
        ItemStack vItem = mock(ItemStack.class);
        PlayerInventory piv = mock(PlayerInventory.class);
        ExecutorTest test = new ExecutorTest(localContext, "SETHELDITEM");
        test.addVariable("player", player);
        when(player.getInventory()).thenReturn(piv);
        test.withArgs(vItem).test();
        verify(piv).setItemInHand(vItem);

        Assert.assertEquals(0, test.getOverload(vItem));
        test.assertInvalid(0);
        test.assertInvalid("NUUP");
        test.assertInvalid(true);
    }

    @Test
    public void testSetItemLore() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        ItemMeta vIM = mock(ItemMeta.class);
        ExecutorTest test = new ExecutorTest(localContext, "SETITEMLORE");
        when(vItem.getItemMeta()).thenReturn(vIM);
        test.withArgs("NO\nNO", vItem).test();
        verify(vItem).setItemMeta(vIM);

        test.assertValid("herllo", vItem);
        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(0, "hu");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetItemName() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        ItemMeta vIM = mock(ItemMeta.class);
        Material stone = Material.valueOf("STONE");
        ExecutorTest test = new ExecutorTest(localContext, "SETITEMNAME");
        when(vItem.getItemMeta()).thenReturn(vIM);
        when(vItem.getType()).thenReturn(stone);
        test.withArgs("NO--NO", vItem).test();
        verify(vIM).setDisplayName("NO--NO");
        verify(vItem).setItemMeta(vIM);

        test.assertValid("herllo", vItem);
        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(0, "hu");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetOffHand() throws Exception {
        Player player = mock(Player.class);
        ItemStack vItem = mock(ItemStack.class);
        PlayerInventory vInv = mock(PlayerInventory.class);
        ExecutorTest test = new ExecutorTest(localContext, "SETOFFHAND");
        test.addVariable("player", player);
        when(player.getInventory()).thenReturn(vInv);
        test.withArgs(vItem).test();
        verify(vInv).setItemInOffHand(vItem);

        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(true);
    }

    @Test
    public void testSetPlayerInv() throws Exception {
        Player player = mock(Player.class);
        ItemStack vItem = mock(ItemStack.class);
        PlayerInventory vInv = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(vInv);
        when(vInv.getSize()).thenReturn(36);
        ExecutorTest test = new ExecutorTest(localContext, "SETPLAYERINV");
        test.addVariable("player", player);
        test.withArgs(1, vItem).test();
        verify(vInv).setItem(1, vItem);
        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(0, "hu");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetSlot() throws Exception {
        InventoryClickEvent vEvent = mock(InventoryClickEvent.class);
        Inventory vInv = mock(Inventory.class);
        ItemStack vItem = mock(ItemStack.class);
        when(vEvent.getInventory()).thenReturn(vInv);
        when(vInv.getSize()).thenReturn(36);
        ExecutorTest test = new ExecutorTest(localContext, "SETSLOT");
        test.addVariable("event", vEvent);
        test.withArgs(1, vItem).test();
        verify(vInv).setItem(1, vItem);

        test.assertValid(33, vItem);
        test.assertInvalid("hi", vItem);
        test.assertInvalid(0);
        test.assertInvalid("NOPE");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetType() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        Material newDirt = Material.valueOf("DIRT");
        when(vItem.getType()).thenReturn(stone);

        ExecutorTest test = new ExecutorTest(localContext, "SETTYPE");

        test.withArgs("DIRT", vItem).test();

        verify(vItem).setType(newDirt);

        test.assertValid("STONE", vItem);
        test.assertInvalid(1, vItem);
        test.assertInvalid(1, 3);
        test.assertInvalid("h", "d");
        test.assertInvalid(vItem, 2);
    }

    @Test
    public void testSound() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);

        when(player.getLocation()).thenReturn(location);

        new ExecutorTest(localContext, "SOUND").withArgs(location, "BOO", 1.0, 1.0)
                .addVariable("player", player)
                .test();

        verify(player).playSound(location, "BOO", 1.0f, 1.0f);
    }

    @Test
    public void testSoundAll() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        World world = mock(World.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);

        new ExecutorTest(localContext, "SOUNDALL").withArgs(location, "BOO", 1.0, 1.0)
                .addVariable("player", player)
                .test();

        verify(world).playSound(location, "BOO", 1.0f, 1.0f);
    }

    @Test
    public void testSpawn() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);
        Location location = new Location(world, 3, 4, 5);

        when(player.getWorld()).thenReturn(world);

        new ExecutorTest(localContext, "SPAWN").addVariable("player", player)
                .withArgs(location, EntityType.CREEPER.name())
                .test();

        verify(world).spawnEntity(location, EntityType.CREEPER);
    }

    @Test
    public void testTime() throws Exception {
        World world = mock(World.class);

        when(server.getWorld(anyString())).thenReturn(world);

        new ExecutorTest(localContext, "TIME").withArgs("world", 12000).test();

        verify(world).setTime(12000L);
    }

    @Test
    public void testTp() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);

        when(player.getWorld()).thenReturn(world);

        new ExecutorTest(localContext, "TP").withArgs(1, 2.5, 3).addVariable("player", player).test();

        verify(player).teleport(new Location(world, 1, 2.5, 3));
    }

    @Test
    public void testTppos() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);
        Location location = new Location(world, 1, 1, 1);

        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(location);

        new ExecutorTest(localContext, "TPPOS").withArgs("~33 ~-2 ~9").addVariable("player", player).test();

        verify(player).teleport(new Location(world, 1 + 33, 1 - 2, 1 + 9));
    }

    @Test
    public void testVelocity() throws Exception {
        Player player = mock(Player.class);

        new ExecutorTest(localContext, "VELOCITY").withArgs(1, -2, 3).addVariable("player", player).test();

        verify(player).setVelocity(new Vector(1, -2, 3));
    }

    @Test
    public void testWeather() throws Exception {
        JsTest test = new ExecutorTest(localContext, "WEATHER");
        World mockWorld = mock(World.class);
        when(server.getWorld("merp")).thenReturn(mockWorld);

        test.withArgs("merp", true).test();
        verify(mockWorld).setStorm(true);

        when(server.getWorld("merp")).thenReturn(null);
        assertJSError(() -> test.withArgs("merp", true, true).test(), "Invalid parameters! [String, Boolean]");
        assertJSError(() -> test.withArgs("merp", 1).test(), "Invalid parameters! [String, Boolean]");
        assertJSError(() -> test.withArgs(mockWorld, false).test(), "Invalid parameters! [String, Boolean]");
        assertJSError(() -> test.withArgs("merp", true).test(), "Unknown world named merp");
    }

    @BeforeClass
    public static void begin() {
        ExecutorTest.coverage.clear();
    }

    @AfterClass
    public static void tearDown() {
        ExecutorTest.coverage.forEach((key, b) -> System.out.println(key));
        ExecutorTest.coverage.clear();
    }
}