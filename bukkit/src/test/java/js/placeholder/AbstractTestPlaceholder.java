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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test driving class for testing Placeholders
 */
public abstract class AbstractTestPlaceholder extends AbstractTestJavaScripts {
    @Test
    public void testPlayername() throws Exception {
        Player mockPlayer = Mockito.mock(Player.class);
        Mockito.when(mockPlayer.getName()).thenReturn("wysohn");

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
        PlayerCommandPreprocessEvent mockEvent = Mockito.mock(PlayerCommandPreprocessEvent.class);

        JsTest test = new PlaceholderTest(engine, "cmdline");
        test.addVariable("event", mockEvent);


        Mockito.when(mockEvent.getMessage()).thenReturn("/mycommand");

        String line = (String) test.test();
        Mockito.verify(mockEvent).getMessage();
        Assert.assertEquals("mycommand", line);

        line = (String) test.withArgs(0).test();
        Assert.assertEquals("mycommand", line);

        line = (String) test.withArgs(0, 2).test();
        Assert.assertEquals("mycommand", line);


        Mockito.when(mockEvent.getMessage()).thenReturn("/mycommand arg1 arg2");

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
        EntityEvent mockEvent = Mockito.mock(EntityEvent.class);
        Entity mockEntity = Mockito.mock(Entity.class);

        JsTest test = new PlaceholderTest(engine, "entityname");
        test.addVariable("event", mockEvent);


        Mockito.when(mockEvent.getEntity()).thenReturn(mockEntity);
        Mockito.when(mockEntity.getName()).thenReturn("SomeEntity");

        Assert.assertEquals("SomeEntity", test.test());
    }

    @Test
    public void testHeldItem() throws Exception {
        Player vp = Mockito.mock(Player.class);
        PlayerInventory vInv = Mockito.mock(PlayerInventory.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PowerMockito.when(vp, "getInventory").thenReturn(vInv);
        PowerMockito.when(vInv, "getItemInHand").thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "helditem");
        test.addVariable("player", vp);
        ItemStack result = (ItemStack) test.withArgs().test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
    }

    @Test
    public void testOffHandItem() throws Exception {
        Player vp = Mockito.mock(Player.class);
        PlayerInventory vInv = Mockito.mock(PlayerInventory.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PowerMockito.when(vp, "getInventory").thenReturn(vInv);
        PowerMockito.when(vInv, "getItemInOffHand").thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "offhanditem");
        test.addVariable("player", vp);
        ItemStack result = (ItemStack) test.withArgs().test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
    }

    @Test
    public void testPlayerInv() throws Exception {
        Player vp = Mockito.mock(Player.class);
        PlayerInventory vInv = Mockito.mock(PlayerInventory.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        PowerMockito.when(vp, "getInventory").thenReturn(vInv);
        PowerMockito.when(vInv, "getSize").thenReturn(36);
        PowerMockito.when(vInv, "getItem", 2).thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "playerinv");
        test.addVariable("player", vp);
        ItemStack result = (ItemStack) test.withArgs(2).test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
        test.assertInvalid(true);

    }

    @Test
    public void testId() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        PowerMockito.when(vItem, "getType").thenReturn(stone);
        PlaceholderTest test = new PlaceholderTest(engine, "id");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, stone);

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testIdName() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        PowerMockito.when(vItem, "getType").thenReturn(stone);
        PlaceholderTest test = new PlaceholderTest(engine, "idname");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, stone.name());

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testName() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        ItemMeta vIM = Mockito.mock(ItemMeta.class);
        PowerMockito.when(vItem, "hasItemMeta").thenReturn(true);
        PowerMockito.when(vItem, "getItemMeta").thenReturn(vIM);
        PowerMockito.when(vIM, "hasDisplayName").thenReturn(true);
        PowerMockito.when(vIM, "getDisplayName").thenReturn("awwman");

        PlaceholderTest test = new PlaceholderTest(engine, "name");
        Object result = test.withArgs(vItem).test();
        Assert.assertEquals(result, "awwman");

        test.assertInvalid("hi");
        test.assertInvalid(true);
        test.assertInvalid(35);

    }

    @Test
    public void testLore() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        ItemMeta vIM = Mockito.mock(ItemMeta.class);
        List<String> lores = new ArrayList<>();
        lores.add("creeper");
        lores.add("awwman");
        lores.add("sowebackinthemine");
        PowerMockito.when(vItem, "hasItemMeta").thenReturn(true);
        PowerMockito.when(vItem, "getItemMeta").thenReturn(vIM);
        PowerMockito.when(vIM, "hasLore").thenReturn(true);
        PowerMockito.when(vIM, "getLore").thenReturn(lores);
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
        InventoryClickEvent vEvent = Mockito.mock(InventoryClickEvent.class);
        ItemStack vItem = Mockito.mock(ItemStack.class);
        Inventory vInv = Mockito.mock(Inventory.class);
        PowerMockito.when(vEvent, "getInventory").thenReturn(vInv);
        PowerMockito.when(vInv, "getSize").thenReturn(36);
        PowerMockito.when(vInv, "getItem", 2).thenReturn(vItem);
        PlaceholderTest test = new PlaceholderTest(engine, "slot");
        test.addVariable("event", vEvent);
        ItemStack result = (ItemStack) test.withArgs(2).test();
        Assert.assertEquals(result, vItem);

        test.assertInvalid("hi");
        test.assertInvalid(true);
    }

    @Test
    public void testCount() throws Exception {
        ItemStack vItem = Mockito.mock(ItemStack.class);
        Material stone = Material.valueOf("STONE");
        PlaceholderTest test = new PlaceholderTest(engine, "count");
        PowerMockito.when(vItem, "getType").thenReturn(stone);
        PowerMockito.when(vItem, "getAmount").thenReturn(34);

        Object result = test.withArgs(vItem).test();

        Assert.assertEquals(result, 34);
        test.assertValid(vItem);
        test.assertInvalid("hi");
        test.assertInvalid(24);
    }
}
