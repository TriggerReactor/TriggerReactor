/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package js.placeholder;

import js.AbstractTestJavaScripts;
import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test driving class for testing Placeholders
 */
public abstract class AbstractTestPlaceholder extends AbstractTestJavaScripts {

    @BeforeClass
    public static void begin(){
        PlaceholderTest.coverage.clear();
    }

    @AfterClass
    public static void tearDown(){
        PlaceholderTest.coverage.forEach((key, b) -> System.out.println(key));
        PlaceholderTest.coverage.clear();
    }

    @Test
    public void testBlockName() throws Exception {
        World world = mock(World.class);
        Block block = mock(Block.class);

        when(server.getWorld(anyString())).thenReturn(world);
        when(world.getBlockAt(0, 1, 5)).thenReturn(block);
        when(block.getType()).thenReturn(Material.DIAMOND_BLOCK);

        assertEquals("diamond_block", new PlaceholderTest(engine, "blockname")
                .withArgs("world", 0, 1, 5)
                .test());
    }

    @Test
    public void testPlayername() throws Exception {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("wysohn");

        Object result = new PlaceholderTest(engine, "playername")
                .addVariable("player", mockPlayer)
                .test();

        assertEquals("wysohn", result);
    }

    @Test
    public void testPlayernameMultiThreaded() throws Exception {
        Player mockPlayer = mock(Player.class);
        ExecutorService pool = Executors.newSingleThreadExecutor();

        when(mockPlayer.getName()).thenReturn("wysohn");
//        when(mockMain.isServerThread()).thenReturn(false);
//        when(mockMain.callSyncMethod(any(Callable.class))).then(invocation -> {
//            Callable call = invocation.getArgument(0);
//            return pool.submit(call);
//        });

        Runnable run = new Runnable() {
            final JsTest test = new PlaceholderTest(engine, "playername")
                    .addVariable("player", mockPlayer);

            @Override
            public void run() {
                for(int i = 0; i < 1000; i++){
                    Object result = null;
                    try {
                        result = test.test();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assertEquals("wysohn", result);
                }
            }
        };

        Thread.UncaughtExceptionHandler handler = mock(Thread.UncaughtExceptionHandler.class);

        Thread thread1 = new Thread(run);
        thread1.setUncaughtExceptionHandler(handler);
        Thread thread2 = new Thread(run);
        thread2.setUncaughtExceptionHandler(handler);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        verify(handler, never()).uncaughtException(any(), any());
        verify(mockPlayer, times(2000)).getName();
    }

    /*
    @Test
    public void testIsNumber() throws Exception{
        JsTest output = new PlaceholderTest(engine, "isnumber");

        output.withArgs("3").test();
       // assertEquals(true, output); TODO
    }
    */

    @Test
    public void testRandom() throws Exception {
        JsTest test = new PlaceholderTest(engine, "random");

        test.withArgs(1).test();

        test.assertValid(0).assertValid(1, 2)
                .assertInvalid().assertInvalid(1, 2, 3).assertInvalid("j").assertInvalid(4, "j");
    }

    @Test
    public void testCmdline() throws Exception {
        PlayerCommandPreprocessEvent mockEvent = mock(PlayerCommandPreprocessEvent.class);

        JsTest test = new PlaceholderTest(engine, "cmdline");
        test.addVariable("event", mockEvent);


        when(mockEvent.getMessage()).thenReturn("/mycommand");

        String line = (String) test.test();
        Mockito.verify(mockEvent).getMessage();
        Assert.assertEquals("mycommand", line);

        line = (String) test.withArgs(0).test();
        Assert.assertEquals("mycommand", line);

        line = (String) test.withArgs(0, 2).test();
        Assert.assertEquals("mycommand", line);


        when(mockEvent.getMessage()).thenReturn("/mycommand arg1 arg2");

        line = (String) test.test();
        Assert.assertEquals("mycommand arg1 arg2", line);

        line = (String) test.withArgs(1).test();
        Assert.assertEquals("arg1 arg2", line);

        line = (String) test.withArgs(0, 1).test();
        Assert.assertEquals("mycommand arg1", line);

        line = (String) test.withArgs(0, 99).test();
        Assert.assertEquals("mycommand arg1 arg2", line);

        line = (String) test.withArgs(8, 99).test();
        Assert.assertNull(line);
    }

    @Test
    public void testEntityname() throws Exception {
        EntityEvent mockEvent = mock(EntityEvent.class);
        Entity mockEntity = mock(Entity.class);

        JsTest test = new PlaceholderTest(engine, "entityname");
        test.addVariable("event", mockEvent);


        when(mockEvent.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getName()).thenReturn("SomeEntity");

        Assert.assertEquals("SomeEntity", test.test());
    }

    @Test
    public void testHeldItem() throws Exception {
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.equals(any(), any())).thenReturn(true);

        Player vp = mock(Player.class);
        PlayerInventory vInv = mock(PlayerInventory.class);
        ItemStack vItem = new ItemStack(Material.AIR);
        when(vp.getInventory()).thenReturn(vInv);
        when(vInv.getItemInMainHand()).thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "helditem");
        test.addVariable("player", vp);
        ItemStack result = (ItemStack) test.withArgs().test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
    }

    @Test
    public void testOffHandItem() throws Exception {
        Player vp = mock(Player.class);
        PlayerInventory vInv = mock(PlayerInventory.class);
        ItemStack vItem = mock(ItemStack.class);
        when(vp.getInventory()).thenReturn(vInv);
        when(vInv.getItemInOffHand()).thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "offhanditem");
        test.addVariable("player", vp);
        ItemStack result = (ItemStack) test.withArgs().test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
    }

    @Test
    public void testPlayerInv() throws Exception {
        Player vp = mock(Player.class);
        PlayerInventory vInv = mock(PlayerInventory.class);
        ItemStack vItem = mock(ItemStack.class);
        when(vp.getInventory()).thenReturn(vInv);
        when(vInv.getSize()).thenReturn(36);
        when(vInv.getItem(2)).thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "playerinv");
        test.addVariable("player", vp);
        ItemStack result = (ItemStack) test.withArgs(2).test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
        test.assertInvalid(true);

    }

    @Test
    public void testId() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        when(vItem.getType()).thenReturn(stone);
        PlaceholderTest test = new PlaceholderTest(engine, "id");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, stone);

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testIdName() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        when(vItem.getType()).thenReturn(stone);
        PlaceholderTest test = new PlaceholderTest(engine, "idname");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, stone.name());

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testName() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        ItemMeta vIM = mock(ItemMeta.class);
        when(vItem.hasItemMeta()).thenReturn(true);
        when(vItem.getItemMeta()).thenReturn(vIM);
        when(vIM.hasDisplayName()).thenReturn(true);
        when(vIM.getDisplayName()).thenReturn("awwman");

        PlaceholderTest test = new PlaceholderTest(engine, "name");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, "awwman");

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testIsNumber() throws Exception{
        assertEquals(true, new PlaceholderTest(engine, "isnumber")
                .withArgs("20342.5352")
                .test());

        assertEquals(false, new PlaceholderTest(engine, "isnumber")
                .withArgs("20343d.66")
                .test());
    }

    @Test
    public void testLore() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        ItemMeta vIM = mock(ItemMeta.class);
        List<String> lores = new ArrayList<>();
        lores.add("creeper");
        lores.add("awwman");
        lores.add("sowebackinthemine");
        when(vItem.hasItemMeta()).thenReturn(true);
        when(vItem.getItemMeta()).thenReturn(vIM);
        when(vIM.hasLore()).thenReturn(true);
        when(vIM.getLore()).thenReturn(lores);
        String loreString = "";
        for (int k = 0; k < lores.size(); k++) {
            String lore = lores.get(k);
            if (k == (lores.size() - 1))
                loreString = loreString + lore;
            else
                loreString = loreString + lore + "\n";
        }

        PlaceholderTest test = new PlaceholderTest(engine, "lore");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, loreString);

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testRound() throws Exception {
        assertEquals(1.34, new PlaceholderTest(engine, "round")
                .withArgs(1.3449, 2)
                .test());
    }

    @Test
    public void testRound2() throws Exception {
        assertEquals(1.0, new PlaceholderTest(engine, "round")
                .withArgs(1.3449, 0)
                .test());
    }

    @Test
    public void testRound2_2() throws Exception {
        assertEquals(34, new PlaceholderTest(engine, "round")
                .withArgs(34, 2)
                .test());
    }

    @Test(expected = Exception.class)
    public void testRound3() throws Exception {
        assertEquals(1.34, new PlaceholderTest(engine, "round")
                .withArgs(1.3449, 2.3)
                .test());
    }

    @Test(expected = Exception.class)
    public void testRound4() throws Exception {
        assertEquals(1.34, new PlaceholderTest(engine, "round")
                .withArgs(1.3449, -2)
                .test());
    }

    @Test
    public void testSlot() throws Exception {
        InventoryClickEvent vEvent = mock(InventoryClickEvent.class);
        ItemStack vItem = mock(ItemStack.class);
        Inventory vInv = mock(Inventory.class);
        when(vEvent.getInventory()).thenReturn(vInv);
        when(vInv.getSize()).thenReturn(36);
        when(vInv.getItem(2)).thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "slot");
        test.addVariable("event", vEvent);
        ItemStack result = (ItemStack) test.withArgs(2).test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
        test.assertInvalid(true);
    }

    @Test
    public void testCount() throws Exception {
        ItemStack vItem = mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        PlaceholderTest test = new PlaceholderTest(engine, "count");
        when(vItem.getType()).thenReturn(stone);
        when(vItem.getAmount()).thenReturn(34);

        Object result = test.withArgs(vItem).test();

        Assert.assertEquals(result, 34);
        test.assertValid(vItem);
        test.assertInvalid("hi");
        test.assertInvalid(24);
    }

    @Test
    public void testOnlinePlayers() throws Exception{
        Player[] players = new Player[]{
                mock(Player.class),
                mock(Player.class),
                mock(Player.class),
                mock(Player.class),
                mock(Player.class),
        };

        doReturn(Arrays.asList(players)).when(server).getOnlinePlayers();

        assertEquals(5, new PlaceholderTest(engine, "onlineplayers")
                .test());
    }
}
