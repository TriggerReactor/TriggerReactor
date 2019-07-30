package js.executor;

import js.AbstractTestJavaScripts;
import js.ErrorProneRunnable;
import js.JsTest;
import js.ExecutorTest;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.ArrayList;

/**
 * Test driving class for testing Executors.
 *
 */

public abstract class AbstractTestExecutors extends AbstractTestJavaScripts {
	public static void assertError(ErrorProneRunnable run)
	{
		try {
			run.run();
		}
		catch (Exception e) {
			return;
		}
		Assert.fail("runnable did not throw any exception");
	}
	
	public static void assertError(ErrorProneRunnable run, String expectedMessage)
	{
		assertError(run, message -> message.equals("Error: " + expectedMessage));
	}
	
	public static void assertError(ErrorProneRunnable run, Predicate<String> messageTest)
	{
		try {
			run.run();
		}
		catch (Exception e) {
			if (messageTest.test(e.getCause().getMessage())) return;
			Assert.fail("Exeption message predicate failed to match message: \"" + e.getCause().getMessage() + "\"");
		}
		Assert.fail("runnable did not throw any exception");
	}
	
    @Test
    public void testPlayer_SetFlyMode() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);
        
        JsTest test = new ExecutorTest(engine, "SETFLYMODE", "PLAYER")
                .addVariable("player", mockPlayer);

        for (boolean b : new boolean[] {true, false})
        {
        	test.withArgs(b).test();
        	Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(b));
        	Mockito.verify(mockPlayer).setFlying(Mockito.eq(b));
        }
        
        assertError(() -> test.withArgs(true, true).test());
        assertError(() -> test.withArgs("merp").test());
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
        //TODO
    }
    
    @Test
    public void testCmdCon() throws Exception{
        //TODO
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
        //TODO
    }

}
