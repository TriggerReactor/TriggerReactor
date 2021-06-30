package js.placeholder;

import js.AbstractTestJavaScripts;
import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.Material;
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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test driving class for testing Placeholders
 */
public abstract class AbstractTestPlaceholder extends AbstractTestJavaScripts {
    @Test
    public void testPlayername() throws Exception {
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("wysohn");

        Object result = new PlaceholderTest(engine, "playername")
                .addVariable("player", mockPlayer)
                .test();

        assertEquals("wysohn", result);
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
}
