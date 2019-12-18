package js.executor;

//import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import js.AbstractTestJavaScripts;
import js.ExecutorTest;
import js.JsTest;
import junit.framework.Assert;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Lever;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.Collection;

import static io.github.wysohn.triggerreactor.core.utils.TestUtil.assertJSError;
import static org.mockito.Mockito.times;

/**
 * Test driving class for testing Executors.
 *
 */

public abstract class AbstractTestExecutors extends AbstractTestJavaScripts {
    @Test
    public void testPlayer_SetFlyMode() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);

        JsTest test = new ExecutorTest(engine, "SETFLYMODE")
                .addVariable("player", mockPlayer);

        for (boolean b : new boolean[]{true, false}) {
            test.withArgs(b).test();
            Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(b));
            Mockito.verify(mockPlayer).setFlying(Mockito.eq(b));
        }

        assertJSError(() -> test.withArgs(true, true).test(), "Incorrect number of arguments for executor SETFLYMODE");
        assertJSError(() -> test.withArgs("merp").test(), "Invalid argument for executor SETFLYMODE: merp");
    }

    @Test
    public void testPlayer_SetFlySpeed() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETFLYSPEED")
                .addVariable("player", vp);

        //only case
        test.withArgs(0.5).test();
        Mockito.verify(vp).setFlySpeed(0.5F);

        //Unexpected cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETFLYSPEED");
        assertJSError(() -> test.withArgs(0.5, 13).test(), "Incorrect Number of arguments for Executor SETFLYSPEED");
        assertJSError(() -> test.withArgs("HI").test(), "Invalid argument for SETFLYSPEED: HI");
        assertJSError(() -> test.withArgs(4).test(), "Argument for Executor SETFLYSPEED is outside of range -1..1");
        assertJSError(() -> test.withArgs(-4).test(), "Argument for Executor SETFLYSPEED is outside of range -1..1");
    }

    @Test
    public void testPlayer_SetFood() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETFOOD")
                .addVariable("player", vp);


        //case1
        test.withArgs(3).test();
        Mockito.verify(vp).setFoodLevel(3);

        //case2
        test.withArgs(4.0).test();
        Mockito.verify(vp).setFoodLevel(4);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETFOOD");
        assertJSError(() -> test.withArgs("HI").test(), "Invalid argument for Executor SETFOOD: HI");
        assertJSError(() -> test.withArgs(3.4).test(), "Argument for Executor SETFOOD should be a whole number");
        assertJSError(() -> test.withArgs(-3.0).test(), "Argument for Executor SETFOOD should not be negative");
    }

    @Test
    public void testPlayer_SetGameMode() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETGAMEMODE")
                .addVariable("player", vp);

        //only case
        test.withArgs("creative").test();
        Mockito.verify(vp).setGameMode(GameMode.valueOf("CREATIVE"));

        //case2
        test.withArgs(2).test();
        Mockito.verify(vp).setGameMode(GameMode.valueOf("ADVENTURE"));

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect number of arguments for executor SETGAMEMODE");
        assertJSError(() -> test.withArgs(34).test(), "Invalid argument for Executor SETGAMEMODE: 34");
        assertJSError(() -> test.withArgs("hElLo").test(), "Unknown GAEMMODE value hElLo");
    }

    @Test
    public void testPlayer_SetHealth() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETHEALTH")
                .addVariable("player", vp);
        PowerMockito.when(vp, "getMaxHealth").thenReturn(20.0);

        //case1
        test.withArgs(2).test();
        Mockito.verify(vp).setHealth(2.0);

        //case2
        test.withArgs(3.0).test();
        Mockito.verify(vp).setHealth(3.0);

        //Unexpected Cases
        assertJSError(() -> test.withArgs(1, 334).test(), "Incorrect Number of arguments for executor SETHEALTH");
        assertJSError(() -> test.withArgs("yeah").test(), "Invalid argument for SETHEALTH: yeah");
        assertJSError(() -> test.withArgs(-17).test(), "Argument for Exector SETHEALTH should not be negative");
        assertJSError(() -> test.withArgs(50).test(), "Argument for Executor SETHEALTH is greater than the max health");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPlayer_SetMaxHealth() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETMAXHEALTH")
                .addVariable("player", vp);

        //case1
        test.withArgs(30).test();
        Mockito.verify(vp).setMaxHealth(30.0);

        //case2
        test.withArgs(35.4).test();
        Mockito.verify(vp).setMaxHealth(35.4);

        //Unexpected Cases
        assertJSError(() -> test.withArgs(20, 33).test(), "Incorrect Number of arguments for Executor SETMAXHEALTH");
        assertJSError(() -> test.withArgs("NONO").test(), "Invalid argument for SETMAXHEALTH: NONO");
        assertJSError(() -> test.withArgs(-30).test(), "Argument for Executor SETMAXHEALTH should not be negative or zero");
        assertJSError(() -> test.withArgs(2098).test(), "Maximum health cannot be greater than 2048");
    }

    @Test
    public void testPlayer_SetSaturation() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETSATURATION")
                .addVariable("player", vp);

        //case1
        test.withArgs(25).test();
        Mockito.verify(vp).setSaturation(25.0F);

        //case2
        test.withArgs(44.0).test();
        Mockito.verify(vp).setSaturation(44.0F);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETSATURATION");
        assertJSError(() -> test.withArgs("Hi").test(), "Invalid argument for SETSATURATION: Hi");
        assertJSError(() -> test.withArgs(-45).test(), "Argument for Executor SETSATURATION should not be negative");
    }

    @Test
    public void testPlayer_SetWalkSpeed() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETWALKSPEED")
                .addVariable("player", vp);
        //case1
        test.withArgs(1).test();
        Mockito.verify(vp).setWalkSpeed(1.0F);

        //case2
        test.withArgs(0.7).test();
        Mockito.verify(vp).setWalkSpeed(0.7F);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect Number of arguments for Executor SETWALKSPEED");
        assertJSError(() -> test.withArgs("NUU").test(), "Invalid argument for SETWALKSPEED: NUU");
        assertJSError(() -> test.withArgs(-3).test(), "Argument for Executor SETWALKSPEED is outside of the allowable range -1..1");
    }

    @Test
    public void testPlayer_SetXp() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "SETXP")
                .addVariable("player", vp);

        //case1
        test.withArgs(0.3).test();
        Mockito.verify(vp).setExp(0.3F);

        //case2
        test.withArgs(1).test();
        Mockito.verify(vp).setExp(1.0F);

        //Unexpected Cases
        assertJSError(() -> test.withArgs().test(), "Incorrect number of arguments for executor SETXP");
        assertJSError(() -> test.withArgs("lmao").test(), "Invalid argument for SETXP: lmao");
        assertJSError(() -> test.withArgs(33).test(), "33 is outside of the allowable range of 0..1 for executor SETXP");

    }

    @Test
    public void testActionBar() throws Exception{
        //TODO
    }

    @Test
    public void testBroadcast() throws Exception{
        Collection<Player> players = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            players.add(Mockito.mock(Player.class));
        }

        String message = "&cHey all";
        String colored = ChatColor.translateAlternateColorCodes('&', message);

        PowerMockito.doReturn(players)
                .when(Bukkit.class, "getOnlinePlayers");

        new ExecutorTest(engine, "BROADCAST")
                .withArgs(message)
                .test();

        for(Player mockPlayer : players){
            Mockito.verify(mockPlayer)
                    .sendMessage(Mockito.argThat((String s) -> colored.equals(s)));
        }
    }

    @Test
    public void testBurn() throws Exception{
        //happy cases

        Player mockPlayer = Mockito.mock(Player.class);
        Entity mockEntity = Mockito.mock(Entity.class);
        JsTest test = new ExecutorTest(engine, "BURN").addVariable("player", mockPlayer);

        test.withArgs(3).test();
        Mockito.verify(mockPlayer).setFireTicks(60);

        test.withArgs(0.101).test();
        Mockito.verify(mockPlayer).setFireTicks(2);

        test.withArgs(mockEntity, 1).test();
        Mockito.verify(mockEntity).setFireTicks(20);

        PowerMockito.when(Bukkit.class, "getPlayer", "merp").thenReturn(mockPlayer);
        test.withArgs("merp", 5).test();
        Mockito.verify(mockPlayer).setFireTicks(100);

        //sad cases
        PowerMockito.when(Bukkit.class, "getPlayer", "merp").thenReturn(null);
        assertJSError(() -> test.withArgs(-1).test(), "The number of seconds to burn should be positive");
        assertJSError(() -> test.withArgs().test(), "Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]");
        assertJSError(() -> test.withArgs(1, 1, 1).test(), "Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]");
        assertJSError(() -> test.withArgs(true).test(), "Invalid number for seconds to burn: true");
        assertJSError(() -> test.withArgs(null, 4).test(), "player to burn should not be null");
        assertJSError(() -> test.withArgs("merp", 3).test(), "player to burn does not exist");
        assertJSError(() -> test.withArgs(3, 3).test(), "invalid entity to burn: 3");
        assertJSError(() -> test.withArgs(mockEntity, "merp").test(), "The number of seconds to burn should be a number");
        assertJSError(() -> test.withArgs(mockEntity, -1).test(), "The number of seconds to burn should be positive");
    }

    @Test
    public void testClearChat() throws Exception{
        Player vp = Mockito.mock(Player.class);
        Player vp2 = Mockito.mock(Player.class);
        Player nullP = null;
        JsTest test = new ExecutorTest(engine, "CLEARCHAT").addVariable("player", vp);

        //case1
        test.withArgs().test();
        Mockito.verify(vp, times(30)).sendMessage("");

        //case2
        test.withArgs(vp2).test();
        Mockito.verify(vp2, times(30)).sendMessage("");

        //Unexpected Cases
        assertJSError(() -> test.withArgs(nullP).test(), "Found unexpected parameter - player: null");
        assertJSError(() -> test.withArgs(1, 2).test(), "Too many parameters found! CLEARCHAT accept up to one parameter.");
    }

    @Test
    public void testClearEntity() throws Exception {
        Player vp = Mockito.mock(Player.class);
        Collection<Entity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entities.add(Mockito.mock(Entity.class));
        }
        JsTest test = new ExecutorTest(engine, "CLEARENTITY")
                .addVariable("player", vp);
        PowerMockito.when(vp, "getNearbyEntities", 2d, 2d, 2d).thenReturn(entities);
        test.withArgs(2).test();
        for (Entity ve : entities) {
            Mockito.verify(ve).remove();
        }
        assertJSError(() -> test.withArgs().test(), "Invalid parameters! [Number]");
        assertJSError(() -> test.withArgs("NO").test(), "Invalid parameters! [Number]");
    }

    @Test
    public void testClearPotion() throws Exception {
        Player p = Mockito.mock(Player.class);
        ExecutorTest test = new ExecutorTest(engine, "CLEARPOTION");
        test.addVariable("player", p);
        test.test();

        Assert.assertEquals(0, test.getOverload());
        Assert.assertEquals(1, test.getOverload("SPEED"));

        Assert.assertFalse(test.isValid(0));
        Assert.assertFalse(test.isValid("SPEED", "SPEED"));
    }

    @Test
    public void testCloseGUI() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "CLOSEGUI")
                .addVariable("player", vp);

        //only happy case
        test.withArgs().test();
        Mockito.verify(vp).closeInventory();
    }

    @Test
    public void testCmd() throws Exception{
        // #CMD internally creates Event which cannot be tested
    }

    @Test
    public void testCmdCon() throws Exception{
        // #CMDCON retrieve ConsoleCommandSender by static method
    }

    @Test
    public void testDoorClose() throws Exception{
        //TODO
    }

    @Test
    public void testDoorOpen() throws Exception{
        //TODO
    }

    @Test
    public void testDoorToggle() throws Exception{
        //TODO
    }

    @Test
    public void testDropItem() throws Exception{
        //TODO
    }

    @Test
    public void testExplosion() throws Exception{
        //TODO
    }

    @Test
    public void testFallingBlock() throws Exception{
        //TODO
    }

    @Test
    public void testGive() throws Exception {
        Player vp = Mockito.mock(Player.class);
        PlayerInventory vpInv = Mockito.mock(PlayerInventory.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        JsTest test = new ExecutorTest(engine, "GIVE")
                .addVariable("player", vp);

        PowerMockito.when(vp, "getInventory").thenReturn(vpInv);
        PowerMockito.when(vpInv, "firstEmpty").thenReturn(4);
        test.withArgs(vItem).test();
        Mockito.verify(vpInv).addItem(vItem);

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [ItemStack]");
        PowerMockito.when(vpInv, "firstEmpty").thenReturn(-1);
        assertJSError(() -> test.withArgs(vItem).test(), "Player has no empty slot.");
        PowerMockito.when(vpInv, "firstEmpty").thenReturn(7);
        assertJSError(() -> test.withArgs("hi").test(), "Invalid ItemStack: hi");
    }

    @Test
    public void testGUI() throws Exception {
        IPlayer vip = Mockito.mock(IPlayer.class);
        TriggerReactor tr = Mockito.mock(TriggerReactor.class);
        AbstractInventoryTriggerManager invManager = Mockito.mock(AbstractInventoryTriggerManager.class);
        IInventory iInv = Mockito.mock(IInventory.class);
        JsTest test = new ExecutorTest(engine, "GUI")
                .addVariable("player", vip)
                .addVariable("plugin", tr);

        PowerMockito.when(tr, "getInvManager").thenReturn(invManager);
        PowerMockito.when(invManager, "openGUI", vip, "Hi").thenReturn(iInv);
        test.withArgs("Hi").test();
        Mockito.verify(invManager).openGUI(vip, "Hi");

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [String]");
        PowerMockito.when(invManager, "openGUI", vip, "hello").thenReturn(null);
        assertJSError(() -> test.withArgs("hello").test(), "No such Inventory Trigger named hello");
    }

    @Test
    public void testItemFrameRotate() throws Exception {
        Location vLoc = Mockito.mock(Location.class);
        Location vLoc2 = Mockito.mock(Location.class);
        Block vBlock = Mockito.mock(Block.class);
        World vWorld = Mockito.mock(World.class);
        Collection<ItemFrame> vEntities = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            vEntities.add(Mockito.mock(ItemFrame.class));
        }
        JsTest test = new ExecutorTest(engine, "ITEMFRAMEROTATE");

        PowerMockito.when(vLoc, "getBlock").thenReturn(vBlock);
        PowerMockito.when(vBlock, "getWorld").thenReturn(vWorld);
        PowerMockito.when(vBlock, "getLocation").thenReturn(vLoc2);
        PowerMockito.when(vWorld, "getNearbyEntities", vLoc2, 2.0, 2.0, 2.0).thenReturn(vEntities);

        test.withArgs("NOne", vLoc).test();
        for (ItemFrame entity : vEntities) {
            Mockito.verify(entity).setRotation(Rotation.valueOf("NOne".toUpperCase()));
        }
        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [Rotation<string>, Location<location or number number number>]");

        //TODO - need test for the situation of args.length == 4
    }

    @Test
    public void testItemFrameSet() throws Exception{
        //TODO
    }

    @Test
    public void testKill() throws Exception {
        Player vp = Mockito.mock(Player.class);
        JsTest test = new ExecutorTest(engine, "KILL")
                .addVariable("player", vp);

        test.withArgs().test();
        Mockito.verify(vp).setHealth(0d);
    }

    @Test
    public void testLeverOff() throws Exception {

        Location vLoc = Mockito.mock(Location.class);
        Block vBlock = Mockito.mock(Block.class);
        BlockState vBS = Mockito.mock(BlockState.class);
        Lever vLever = Mockito.mock(Lever.class);
        JsTest test = new ExecutorTest(engine, "LEVEROFF");

        PowerMockito.when(vLoc, "getBlock").thenReturn(vBlock);
        PowerMockito.when(vBlock, "getState").thenReturn(vBS);
        PowerMockito.when(vBS, "getData").thenReturn(vLever);
        test.withArgs(vLoc).test();
        Mockito.verify(vLever).setPowered(false);

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [Location<location or number number number>]");
        //TODO - need test for the situation of args.length == 3
    }

    @Test
    public void testLeverOn() throws Exception {

        Location vLoc = Mockito.mock(Location.class);
        Block vBlock = Mockito.mock(Block.class);
        BlockState vBS = Mockito.mock(BlockState.class);
        Lever vLever = Mockito.mock(Lever.class);
        JsTest test = new ExecutorTest(engine, "LEVERON");

        PowerMockito.when(vLoc, "getBlock").thenReturn(vBlock);
        PowerMockito.when(vBlock, "getState").thenReturn(vBS);
        PowerMockito.when(vBS, "getData").thenReturn(vLever);
        test.withArgs(vLoc).test();
        Mockito.verify(vLever).setPowered(true);

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [Location<location or number number number>]");
        //TODO - need test for the situation of args.length == 3
    }

    @Test
    public void testLeverToggle() throws Exception {

        Location vLoc = Mockito.mock(Location.class);
        Block vBlock = Mockito.mock(Block.class);
        BlockState vBS = Mockito.mock(BlockState.class);
        Lever vLever = Mockito.mock(Lever.class);
        JsTest test = new ExecutorTest(engine, "LEVERTOGGLE");

        PowerMockito.when(vLoc, "getBlock").thenReturn(vBlock);
        PowerMockito.when(vBlock, "getState").thenReturn(vBS);
        PowerMockito.when(vBS, "getData").thenReturn(vLever);

        //case1
        PowerMockito.when(vLever, "isPowered").thenReturn(false);
        test.withArgs(vLoc).test();
        Mockito.verify(vLever).setPowered(true);
        //case2
        PowerMockito.when(vLever, "isPowered").thenReturn(true);
        test.withArgs(vLoc).test();
        Mockito.verify(vLever).setPowered(false);

        assertJSError(() -> test.withArgs().test(), "Invalid parameters. Need [Location<location or number number number>]");
        //TODO - need test for the situation of args.length == 3
    }

    @Test
    public void testLightning() throws Exception {
        Location vLoc = Mockito.mock(Location.class);
        World vWorld = Mockito.mock(World.class);
        JsTest test = new ExecutorTest(engine, "LIGHTNING");

        PowerMockito.when(vLoc, "getWorld").thenReturn(vWorld);
        test.withArgs(vLoc).test();
        Mockito.verify(vWorld).strikeLightning(vLoc);

        assertJSError(() -> test.withArgs().test(), "Invalid parameters! [String, Number, Number, Number] or [Location]");
        assertJSError(() -> test.withArgs("hff").test(), "Invalid parameters! [String, Number, Number, Number] or [Location]");
        //TODO - need test for the situation of args.length == 4
    }

    @Test
    public void testLog() throws Exception{
        //no way to test window.print()
    }

    @Test
    public void testMessage() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);

        new ExecutorTest(engine, "MESSAGE")
                .addVariable("player", mockPlayer)
                .withArgs("&cTest Message")
                .test();

        String expected = ChatColor.translateAlternateColorCodes('&', "&cTest Message");
        Mockito.verify(mockPlayer).sendMessage(Mockito.argThat((String str) -> expected.equals(str)));
    }

    @Test
    public void testModifyHeldItem() throws Exception{
        //TODO
    }

    @Test
    public void testModifyPlayer() throws Exception{
        //No longer supported
    }

    @Test
    public void testMoney() throws Exception{
        //written in each platform's test class.
    }

    @Test
    public void testMysql() throws Exception{
        //TODO
    }

    @Test
    public void testPermission() throws Exception{
        //TODO
    }

    @Test
    public void testPotion() throws Exception{
        //TODO
    }

    @Test
    public void testPush() throws Exception{
        //TODO
    }

    @Test
    public void testRotateBlock() throws Exception{
        //TODO
    }

    @Test
    public void testScoreboard() throws Exception{
        //TODO
    }

    @Test
    public void testServer() throws Exception{
        //TODO
    }

    @Test
    public void testSetBlock() throws Exception{
        //TODO
    }

    @Test
    public void testSignEdit() throws Exception{
        //TODO        
    }

    @Test
    public void testSound() throws Exception{
        //TODO
    }

    @Test
    public void testSoundAll() throws Exception{
        //TODO
    }

    @Test
    public void testSpawn() throws Exception{
        //TODO
    }

    @Test
    public void testTime() throws Exception{
        //TODO
    }

    @Test
    public void testTp() throws Exception{
        //TODO
    }

    @Test
    public void testTppos() throws Exception{
        //TODO
    }

    @Test
    public void testVelocity() throws Exception{
        //TODO
    }

    @Test
    public void testWeather() throws Exception {
        JsTest test = new ExecutorTest(engine, "WEATHER");
        World mockWorld = Mockito.mock(World.class);
        PowerMockito.when(Bukkit.class, "getWorld", "merp").thenReturn(mockWorld);

        test.withArgs("merp", true).test();
        Mockito.verify(mockWorld).setStorm(true);

        PowerMockito.when(Bukkit.class, "getWorld", "merp").thenReturn(null);
        assertJSError(() -> test.withArgs("merp", true, true).test(), "Invalid parameters! [String, Boolean]");
        assertJSError(() -> test.withArgs("merp", 1).test(), "Invalid parameters! [String, Boolean]");
        assertJSError(() -> test.withArgs(mockWorld, false).test(), "Invalid parameters! [String, Boolean]");
        assertJSError(() -> test.withArgs("merp", true).test(), "Unknown world named merp");
    }

    @Test
    public void testKick() throws Exception {

        Player vp = Mockito.mock(Player.class);
        Player vp2 = Mockito.mock(Player.class);
        Player nullP = null;
        String msg = ChatColor.translateAlternateColorCodes('&', "&c[TR] You've been kicked from the server.");
        String msg2 = ChatColor.translateAlternateColorCodes('&', "&cKICKED");

        //case1
        ExecutorTest test = new ExecutorTest(engine, "KICK");
        test.addVariable("player", vp);
        test.withArgs().test();
        Mockito.verify(vp).kickPlayer(msg);

        //case2
        test.withArgs(msg2).test();
        Mockito.verify(vp).kickPlayer(msg2);

        //case3
        test.withArgs(vp2).test();
        Mockito.verify(vp2).kickPlayer(msg);

        //case4
        test.withArgs(vp2, msg2).test();
        Mockito.verify(vp2).kickPlayer(msg2);

        //Unexpected Exception Cases
        test.assertInvalid(1);
        test.assertInvalid(vp, 232);
        test.assertInvalid(1, 2, 3);
        test.addVariable("player", null);

        test.assertInvalid(null, "msg");
        test.assertInvalid(nullP);
        assertJSError(() -> test.withArgs().test(), "Too few arguments! You should enter at least on argument if you use KICK executor from console.");
    }

    @Test
    public void testSetHeldItem() throws Exception {
        Player vp = Mockito.mock(Player.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PlayerInventory piv = Mockito.mock(PlayerInventory.class);
        ExecutorTest test = new ExecutorTest(engine, "SETHELDITEM");
        test.addVariable("player", vp);
        PowerMockito.when(vp, "getInventory").thenReturn(piv);
        test.withArgs(vItem).test();
        Mockito.verify(piv).setItemInHand(vItem);

        Assert.assertEquals(0, test.getOverload(vItem));
        test.assertInvalid(0);
        test.assertInvalid("NUUP");
        test.assertInvalid(true);
    }

    @Test
    public void testSetOffHand() throws Exception {
        Player vp = Mockito.mock(Player.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PlayerInventory vInv = Mockito.mock(PlayerInventory.class);
        ExecutorTest test = new ExecutorTest(engine, "SETOFFHAND");
        test.addVariable("player", vp);
        PowerMockito.when(vp, "getInventory").thenReturn(vInv);
        test.withArgs(vItem).test();
        Mockito.verify(vInv).setItemInOffHand(vItem);

        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(true);
    }

    @Test
    public void testSetPlayerInv() throws Exception {
        Player vp = Mockito.mock(Player.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PlayerInventory vInv = Mockito.mock(PlayerInventory.class);
        PowerMockito.when(vp, "getInventory").thenReturn(vInv);
        PowerMockito.when(vInv, "getSize").thenReturn(36);
        ExecutorTest test = new ExecutorTest(engine, "SETPLAYERINV");
        test.addVariable("player", vp);
        test.withArgs(1, vItem).test();
        Mockito.verify(vInv).setItem(1, vItem);
        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(0, "hu");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetItemLore() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        ItemMeta vIM = Mockito.mock(ItemMeta.class);
        ExecutorTest test = new ExecutorTest(engine, "SETITEMLORE");
        PowerMockito.when(vItem, "getItemMeta").thenReturn(vIM);
        test.withArgs("NO\nNO", vItem).test();
        Mockito.verify(vItem).setItemMeta(vIM);

        test.assertValid("herllo", vItem);
        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(0, "hu");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetItemName() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        ItemMeta vIM = Mockito.mock(ItemMeta.class);
        Material stone = Material.valueOf("STONE");
        ExecutorTest test = new ExecutorTest(engine, "SETITEMNAME");
        PowerMockito.when(vItem, "getItemMeta").thenReturn(vIM);
        PowerMockito.when(vItem, "getType").thenReturn(stone);
        test.withArgs("NO--NO", vItem).test();
        Mockito.verify(vIM).setDisplayName("NO--NO");
        Mockito.verify(vItem).setItemMeta(vIM);

        test.assertValid("herllo", vItem);
        test.assertInvalid(0);
        test.assertInvalid("HELLO");
        test.assertInvalid(0, "hu");
        test.assertInvalid(true, 0);
    }

    @Test
    public void testSetSlot() throws Exception {
        InventoryClickEvent vEvent = Mockito.mock(InventoryClickEvent.class);
        Inventory vInv = Mockito.mock(Inventory.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PowerMockito.when(vEvent, "getInventory").thenReturn(vInv);
        PowerMockito.when(vInv, "getSize").thenReturn(36);
        ExecutorTest test = new ExecutorTest(engine, "SETSLOT");
        test.addVariable("event", vEvent);
        test.withArgs(1, vItem).test();
        Mockito.verify(vInv).setItem(1, vItem);

        test.assertValid(33, vItem);
        test.assertInvalid("hi", vItem);
        test.assertInvalid(0);
        test.assertInvalid("NOPE");
        test.assertInvalid(true, 0);
    }
}