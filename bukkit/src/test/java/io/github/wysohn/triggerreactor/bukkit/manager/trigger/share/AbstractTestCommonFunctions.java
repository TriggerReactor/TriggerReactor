package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.TestCommonFunctions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Test driving class for both legacy and latest bukkit.
 * Since the structure of legacy and latest bukkit yet shares a lot of similarities,
 * we don't have to write each test case for each different platforms.
 * <p>
 * If, however, there are some tests that has to be platform specific,
 * write them in the child class instead.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({TriggerReactor.class, Bukkit.class})
public abstract class AbstractTestCommonFunctions extends TestCommonFunctions<AbstractCommonFunctions> {
    protected TriggerReactor mockMain;
    protected PluginManager mockPluginManager;
    protected ItemFactory mockItemFactory;
    protected World mockWorld;
    protected Player mockPlayer;
    protected ItemMeta mockItemMeta;

    public AbstractTestCommonFunctions(AbstractCommonFunctions fn) {
        super(fn);
    }

    @Before
    public void init() throws Exception {
        mockMain = Mockito.mock(TriggerReactor.class);
        mockItemFactory = Mockito.mock(ItemFactory.class);
        mockPluginManager = Mockito.mock(PluginManager.class);
        mockWorld = Mockito.mock(World.class);
        mockPlayer = Mockito.mock(Player.class);
        mockItemMeta = Mockito.mock(ItemMeta.class);

        PowerMockito.mockStatic(TriggerReactor.class);
        Mockito.when(TriggerReactor.getInstance()).thenReturn(mockMain);

        PowerMockito.mockStatic(Bukkit.class);
        Mockito.when(Bukkit.getPluginManager()).thenReturn(mockPluginManager);
        Mockito.when(Bukkit.getItemFactory()).thenReturn(mockItemFactory);

        Mockito.when(mockItemFactory.getItemMeta(Mockito.any(Material.class))).thenReturn(mockItemMeta);
        Mockito.when(Bukkit.getWorld(Mockito.anyString())).then(
                invocation -> {
                    return mockWorld;
                });
        @SuppressWarnings("serial")
        Collection<? extends Player> players = new ArrayList<Player>() {{
            add(mockPlayer);
        }};
        PowerMockito.doReturn(players).when(Bukkit.class, "getOnlinePlayers");
        Mockito.when(Bukkit.getPlayer(Mockito.anyString())).thenReturn(mockPlayer);
        Mockito.when(Bukkit.getOfflinePlayer(Mockito.anyString())).thenReturn(mockPlayer);
    }

    protected class FakeInventory {
        protected ItemStack[] contents = new ItemStack[54];

        //copy from CraftBukkit
        protected int first(ItemStack item, boolean withAmount) {
            if (item == null) {
                return -1;
            }
            ItemStack[] inventory = contents;
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] == null) continue;

                if (withAmount ? item.equals(inventory[i]) : isSimilar(item, inventory[i])) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static FakeInventory fInventory(AbstractTestCommonFunctions test, ItemStack... items) {
        FakeInventory inv = test.new FakeInventory();

        for (int i = 0; i < Math.min(inv.contents.length, items.length); i++) {
            inv.contents[i] = items[i];
        }

        return inv;
    }

    protected abstract boolean isSimilar(ItemStack IS1, ItemStack IS2);

    protected abstract boolean isEqual(ItemStack IS1, ItemStack IS2);

    protected PlayerInventory preparePlayerInventory(Player mockPlayer, FakeInventory inv) {
        PlayerInventory mockInventory = Mockito.mock(PlayerInventory.class);

        Mockito.when(mockPlayer.getInventory()).thenReturn(mockInventory);
        Mockito.when(mockInventory.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt()))
                .then(invocation -> {
                    ItemStack target = invocation.getArgument(0);
                    int amount = invocation.getArgument(1);

                    int count = 0;
                    for (ItemStack IS : inv.contents) {
                        if (IS == null)
                            continue;

                        if (isSimilar(IS, target))
                            count += IS.getAmount();

                        if (count >= amount)
                            return true;
                    }

                    return false;
                });

        Mockito.when(mockInventory.removeItem(ArgumentMatchers.<ItemStack>any()))
                .then(invocation -> {
                    // Cody copied from CraftBukkit
                    Object[] items = invocation.getArguments();
                    HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

                    for (int i = 0; i < items.length; i++) {
                        ItemStack item = (ItemStack) items[i];
                        int toDelete = item.getAmount();

                        while (true) {
                            int first = inv.first(item, false);

                            // Drat! we don't have this type in the inventory
                            if (first == -1) {
                                item.setAmount(toDelete);
                                leftover.put(i, item);
                                break;
                            } else {
                                ItemStack itemStack = inv.contents[first];
                                int amount = itemStack.getAmount();

                                if (amount <= toDelete) {
                                    toDelete -= amount;
                                    // clear the slot, all used up
                                    inv.contents[first] = null;
                                } else {
                                    // split the stack and store
                                    itemStack.setAmount(amount - toDelete);
                                    inv.contents[first] = itemStack;
                                    toDelete = 0;
                                }
                            }

                            // Bail when done
                            if (toDelete <= 0) {
                                break;
                            }
                        }
                    }
                    return leftover;
                });

        return mockInventory;
    }

    @Test
    public void testLocation() {
        Location loc1 = new Location(mockWorld, 1, 2, 3);
        Location loc2 = new Location(mockWorld, 4, 5, 6, 0.5F, 0.6F);

        Mockito.when(mockWorld.getName()).thenReturn("test");
        Assert.assertEquals(loc1, fn.location("test", 1, 2, 3));

        Mockito.when(mockWorld.getName()).thenReturn("test2");
        Assert.assertEquals(loc2, fn.location("test2", 4, 5, 6, 0.5, 0.6));
    }

    @Test
    public void testBlock() {
        Block mockBlock = Mockito.mock(Block.class);
        Mockito.when(mockWorld.getBlockAt(Mockito.any(Location.class)))
                .thenReturn(mockBlock);

        Assert.assertEquals(mockBlock, fn.block("test", 1, 2, 3));
    }

    @Test
    public void testLocationEqual() {
        Location loc1 = new Location(mockWorld, 1, 2, 3);
        Location loc2 = new Location(mockWorld, 4, 5, 6, 0.5F, 0.6F);
        Location loc3 = new Location(mockWorld, 1, 2, 3, 0.7F, 0.8F);
        Location loc4 = new Location(mockWorld, 4, 5, 6, 0.1F, 0.2F);

        Assert.assertFalse(fn.locationEqual(loc1, loc2));
        Assert.assertTrue(fn.locationEqual(loc1, loc3));
        Assert.assertFalse(fn.locationEqual(loc2, loc3));
        Assert.assertTrue(fn.locationEqual(loc2, loc4));
    }

    @Test
    public abstract void testMakePotionEffect();

    @Test
    public void testPlayer() {
        Assert.assertEquals(mockPlayer, fn.player("wysohn"));
    }

    @Test
    public void testOPlayer() {
        Assert.assertEquals(mockPlayer, fn.oplayer("wysohn"));
    }

    @Test
    public abstract void testGetPlayers();

    @Test
    public void testCurrentArea() {
        //TODO: testable?
    }

    @Test
    public void testCurrentAreaAt() {
        //TODO: testable?
    }

    @Test
    public void testCurrentAreas() {
        //TODO: testable?
    }

    @Test
    public void testCurrentAreasAt() {
        //TODO: testable?
    }

    @Test
    public void testGetEntitiesInArea() {
        //TODO: testable?
    }

    @Test
    public void testColor() {
        Assert.assertEquals(ChatColor.RED + "My message",
                fn.color("&cMy message"));
    }

    @Test
    public void testBukkitColor() {
        Color expect = Color.fromRGB(3, 6, 8);
        Color result = fn.bukkitColor(3, 6, 8);
        Assert.assertEquals(expect.asBGR(), result.asBGR());
    }

    @Test
    public abstract void testItem();

    private String title;

    @Test
    public void testGetItemTitle() {
        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> "abc").when(mockItemMeta).getDisplayName();

        Assert.assertEquals("abc", fn.getItemTitle(IS));
    }

    @Test
    public void testSetItemTitle() {
        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> {
            title = invocation.getArgument(0);
            return null;
        }).when(mockItemMeta).setDisplayName(Mockito.anyString());

        fn.setItemTitle(IS, "xyz");
        Assert.assertEquals("xyz", title);
    }

    @Test
    public void testHasLore() {
        List<String> lores = new ArrayList<>();
        lores.add("abab");
        lores.add("cdcd");

        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> lores).when(mockItemMeta).getLore();

        Assert.assertTrue(fn.hasLore(IS, "abab"));
        Assert.assertFalse(fn.hasLore(IS, "hoho"));
    }

    @Test
    public void testGetLore() {
        List<String> lores = new ArrayList<>();
        lores.add("abab");
        lores.add("cdcd");

        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> lores).when(mockItemMeta).getLore();

        Assert.assertEquals("abab", fn.getLore(IS, 0));
        Assert.assertEquals("cdcd", fn.getLore(IS, 1));
        Assert.assertNull(fn.getLore(IS, 2));
        Assert.assertNull(fn.getLore(IS, -1));
    }

    @Test
    public void testAddLore() {
        List<String> lores = new ArrayList<>();
        lores.add("abab");
        lores.add("cdcd");

        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> lores).when(mockItemMeta).getLore();

        fn.addLore(IS, "efef");
        Assert.assertEquals(3, lores.size());
        Assert.assertEquals("efef", lores.get(lores.size() - 1));
    }

    @Test
    public void testSetLore() {
        List<String> lores = new ArrayList<>();
        lores.add("abab");
        lores.add("cdcd");

        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> lores).when(mockItemMeta).getLore();

        fn.setLore(IS, 0, "pqpq");
        Assert.assertEquals("pqpq", lores.get(0));
        fn.setLore(IS, 5, "ffee");
        Assert.assertEquals(6, lores.size());
        Assert.assertEquals("ffee", lores.get(5));
    }

    @Test
    public void testRemoveLore() {
        List<String> lores = new ArrayList<>();
        lores.add("abab");
        lores.add("cdcd");
        lores.add("efef");

        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> lores).when(mockItemMeta).getLore();

        fn.removeLore(IS, 2);
        Assert.assertEquals(2, lores.size());
        Assert.assertFalse(lores.contains("efef"));

        fn.removeLore(IS, 0);
        Assert.assertEquals(1, lores.size());
        Assert.assertFalse(lores.contains("abab"));
        Assert.assertEquals("cdcd", lores.get(0));
    }

    @Test
    public void testClearLore() {
        ItemStack IS = new ItemStack(Material.STONE);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        fn.clearLore(IS);
        Mockito.verify(mockItemMeta).setLore(captor.capture());
        Assert.assertEquals(0, captor.getValue().size());
    }

    @Test
    public void testLoreSize() {
        List<String> lores = new ArrayList<>();
        lores.add("abab");
        lores.add("cdcd");
        lores.add("efef");

        ItemStack IS = new ItemStack(Material.STONE);
        Mockito.doAnswer(invocation -> lores).when(mockItemMeta).getLore();

        Assert.assertEquals(3, fn.loreSize(IS));
    }

    @Test
    public void testFormatCurrency() {
        Assert.assertEquals("$3,234,463.44", fn.formatCurrency(3234463.44));
        Assert.assertEquals("$3,234,463.44", fn.formatCurrency(3234463.44, "en", "US"));
        Assert.assertEquals("\u00a33,234,463.44", fn.formatCurrency(3234463.44, "en", "GB"));
    }

    @Test
    public void testGetTargetBlock() {
        fn.getTargetBlock(mockPlayer, 30);
        Mockito.verify(mockPlayer).getTargetBlock(null, 30);
    }

    @Test
    public abstract void testHeadForName();

    @Test
    public abstract void testHeadForValue();
}
