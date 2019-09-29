package js.executor;

import js.AbstractTestJavaScripts;
import js.JsTest;
import js.ExecutorTest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import java.util.Collection;
import static io.github.wysohn.triggerreactor.core.utils.TestUtil.*;
import static org.mockito.Mockito.times;

import java.util.ArrayList;

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

        for (boolean b : new boolean[] {true, false})
        {
        	test.withArgs(b).test();
        	Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(b));
        	Mockito.verify(mockPlayer).setFlying(Mockito.eq(b));
        }
        
        assertError(() -> test.withArgs(true, true).test(), "Incorrect number of arguments for executor SETFLYMODE");
        assertError(() -> test.withArgs("merp").test(), "Invalid argument for executor SETFLYMODE: merp");
    }
    
    @Test
    public void testPlayer_SetFlySpeed() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetFood() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetGameMode() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetHealth() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetMaxHealth() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetSaturation() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetWalkSpeed() throws Exception{
        //TODO
    }
    
    @Test
    public void testPlayer_SetXp() throws Exception{
        //TODO
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
    	assertError(() -> test.withArgs(-1).test(),                 "The number of seconds to burn should be positive");
    	assertError(() -> test.withArgs().test(),                   "Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]");
    	assertError(() -> test.withArgs(1, 1, 1).test(),            "Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]");
    	assertError(() -> test.withArgs(true).test(),               "Invalid number for seconds to burn: true");
    	assertError(() -> test.withArgs(null, 4).test(),            "player to burn should not be null");
    	assertError(() -> test.withArgs("merp", 3).test(),          "player to burn does not exist");
    	assertError(() -> test.withArgs(3, 3).test(),               "invalid entity to burn: 3");
    	assertError(() -> test.withArgs(mockEntity, "merp").test(), "The number of seconds to burn should be a number");
    	assertError(() -> test.withArgs(mockEntity, -1).test(),     "The number of seconds to burn should be positive");
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
        assertError(() -> test.withArgs(nullP).test(), "Found unexpected parameter - player: null");
        assertError(() -> test.withArgs(1, 2).test(), "Too many parameters found! CLEARCHAT accept up to one parameter.");
    }
    
    @Test
    public void testClearEntity() throws Exception{
        //TODO
    }
    
    @Test
    public void testClearPotion() throws Exception{
        //TODO
    }
    
    @Test
    public void testCloseGUI() throws Exception{
        //TODO
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
    public void testGive() throws Exception{
        //TODO
    }
    
    @Test
    public void testGUI() throws Exception{
        //TODO
    }
    
    @Test
    public void testItemFrameRotate() throws Exception{
        //TODO
    }
    
    @Test
    public void testItemFrameSet() throws Exception{
        //TODO
    }
    
    @Test
    public void testKill() throws Exception{
        //TODO
    }
    
    @Test
    public void testLeverOff() throws Exception{
        //TODO
    }
    
    @Test
    public void testLeverOn() throws Exception{
        //TODO
    }
    
    @Test
    public void testLeverToggle() throws Exception{
        //TODO
    }
    
    @Test
    public void testLightning() throws Exception{
        //TODO
    }
    
    @Test
    public void testLog() throws Exception{
        //TODO
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
        //TODO
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
    public void testWeather() throws Exception{
    	JsTest test = new ExecutorTest(engine, "WEATHER");
        World mockWorld = Mockito.mock(World.class);
        PowerMockito.when(Bukkit.class, "getWorld", "merp").thenReturn(mockWorld);
        
        test.withArgs("merp", true).test();
        Mockito.verify(mockWorld).setStorm(true);
        
        PowerMockito.when(Bukkit.class, "getWorld", "merp").thenReturn(null);
        assertError(() -> test.withArgs("merp", true, true).test(), "Invalid parameters! [String, Boolean]");
        assertError(() -> test.withArgs("merp", 1).test(), "Invalid parameters! [String, Boolean]");
        assertError(() -> test.withArgs(mockWorld, false).test(), "Invalid parameters! [String, Boolean]");
        assertError(() -> test.withArgs("merp", true).test(), "Unknown world named merp");
    }

    @Test
    public void testKick() throws Exception {

        Player vp = Mockito.mock(Player.class);
        Player vp2 = Mockito.mock(Player.class);
        Player nullP = null;
        String msg = ChatColor.translateAlternateColorCodes('&', "&c[TR] You've been kicked from the server.");
        String msg2 = ChatColor.translateAlternateColorCodes('&', "&cKICKED");

        //case1
        JsTest test = new ExecutorTest(engine, "KICK").addVariable("player", vp);
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
        assertError(() -> test.withArgs(1).test(), "Found unexpected type of argument: 1");
        assertError(() -> test.withArgs(vp, 232).test(), "Found unexpected type of argument(s) - player: " + vp + " | msg: 232");
        assertError(() -> test.withArgs(1, 2, 3).test(), "Too many arguments! KICK Executor accepts up to two arguments.");
        test.addVariable("player", null);
        assertError(() -> test.withArgs().test(), "Too few arguments! You should enter at least on argument if you use KICK executor from console.");
        assertError(() -> test.withArgs(null, "msg").test(), "Found unexpected type of argument(s) - player: null | msg: msg");
        assertError(() -> test.withArgs(nullP).test(), "Unexpected Error: parameter does not match - player: null");
    }

}
