package js.executor;

import js.AbstractTestJavaScripts;
import js.JsTest;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test driving class for testing Executors.
 *
 */
public class AbstractTestExecutors extends AbstractTestJavaScripts {
    @Test
    public void testPlayer_SetFlyMode() throws Exception{
        Player mockPlayer = Mockito.mock(Player.class);

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(true)
                .test(engine);
        Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(true));

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(true)
                .test(engine);
        Mockito.verify(mockPlayer).setFlying(Mockito.eq(true));

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(false)
                .test(engine);
        Mockito.verify(mockPlayer).setAllowFlight(Mockito.eq(false));

        Mockito.reset(mockPlayer);
        JsTest.JsTester.executorTestOf("PLAYER", "SETFLYMODE")
                .addVariable("player", mockPlayer)
                .withArgs(false)
                .test(engine);
        Mockito.verify(mockPlayer).setFlying(Mockito.eq(false));
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
        //TODO
    }
    
    @Test
    public void testBurn() throws Exception{
        //TODO
    }
    
    @Test
    public void testClearChat() throws Exception{
        //TODO
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

        JsTest.JsTester.executorTestOf("MESSAGE")
                .addVariable("player", mockPlayer)
                .withArgs("&cTest Message")
                .test(engine);

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
