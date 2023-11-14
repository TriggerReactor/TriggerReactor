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

import com.google.inject.Injector;
import js.AbstractTestJavaScripts;
import js.JsTest;
import js.PlaceholderTest;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test driving class for testing Placeholders
 */
public abstract class AbstractTestPlaceholder extends AbstractTestJavaScripts {

    @BeforeClass
    public static void begin() {
        PlaceholderTest.coverage.clear();
    }

    @AfterClass
    public static void tearDown() {
        PlaceholderTest.coverage.forEach((key, b) -> System.out.println(key));
        PlaceholderTest.coverage.clear();
    }

    @Test
    public void testAir() throws Exception {
        Player player = mock(Player.class);

        int remainingAir = 10;

        when(player.getRemainingAir()).thenReturn(remainingAir);

        JsTest test = new PlaceholderTest(engine, "air")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(remainingAir, result);
    }

    @Test
    public void testBiome() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        World world = mock(World.class);
        Biome biome = Biome.DESERT;

        int x = 100;
        int z = -100;

        when(player.getLocation()).thenReturn(location);
        when(player.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(x);
        when(location.getBlockZ()).thenReturn(z);
        when(world.getBiome(x, z)).thenReturn(biome);

        JsTest test = new PlaceholderTest(engine, "biome")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(biome.name(), result);
    }

    @Test
    public void testBlockName() throws Exception {
        class FakeBukkit {
            public World getWorld(String name) {
                return null;
            }
        }

        class FakeJava {
            public FakeBukkit type(String name) {
                return null;
            }
        }

        FakeJava java = mock(FakeJava.class);
        FakeBukkit bukkit = mock(FakeBukkit.class);
        World world = mock(World.class);
        Block block = mock(Block.class);
        Material material = Material.STONE;

        String worldName = "world2";

        int x = 100;
        int y = 50;
        int z = -100;

        when(java.type("org.bukkit.Bukkit")).thenReturn(bukkit);
        when(bukkit.getWorld(worldName)).thenReturn(world);
        when(world.getBlockAt(x, y, z)).thenReturn(block);
        when(block.getType()).thenReturn(material);


        JsTest test = new PlaceholderTest(engine, "blockname")
                .addVariable("Java", java);

        String result = (String) test.withArgs(worldName, x, y, z).test();

        Assert.assertEquals(material.name().toLowerCase(), result);
        Assert.assertEquals(0, test.getOverload(worldName, x, y, z));
    }

    @Test
    public void testCmdLine1() throws Exception {
        class FakeEvent {
            public String getMessage() {
                return null;
            }
        }

        FakeEvent event = mock(FakeEvent.class);

        String command = "/command args1 args2 args3 args4";
        when(event.getMessage()).thenReturn(command);

        JsTest test = new PlaceholderTest(engine, "cmdline")
                .addVariable("event", event);

        String result = (String) test.test();

        Assert.assertEquals("command args1 args2 args3 args4", result);
    }

    @Test
    public void testCmdLine2() throws Exception {
        class FakeEvent {
            public String getMessage() {
                return null;
            }
        }

        FakeEvent event = mock(FakeEvent.class);

        String command = "/command args1 args2 args3 args4";
        when(event.getMessage()).thenReturn(command);

        JsTest test = new PlaceholderTest(engine, "cmdline")
                .addVariable("event", event);

        String result = (String) test.withArgs(1).test();

        Assert.assertEquals("args1 args2 args3 args4", result);
        Assert.assertEquals(1, test.getOverload(1));
    }

    @Test
    public void testCmdLine3() throws Exception {
        class FakeEvent {
            public String getMessage() {
                return null;
            }
        }

        FakeEvent event = mock(FakeEvent.class);

        String command = "/command args1 args2 args3 args4";
        when(event.getMessage()).thenReturn(command);

        JsTest test = new PlaceholderTest(engine, "cmdline")
                .addVariable("event", event);

        String result = (String) test.withArgs(2, 4).test();

        Assert.assertEquals("args2 args3 args4", result);
        Assert.assertEquals(2, test.getOverload(2, 4));
    }

    @Test
    public void testCount() throws Exception {
        ItemStack item = mock(ItemStack.class);
        int amount = 5;

        when(item.getAmount()).thenReturn(amount);

        JsTest test = new PlaceholderTest(engine, "count");

        int result = (int) test.withArgs(item).test();

        Assert.assertEquals(amount, result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testCurrentTimeSeconds() throws Exception {
        JsTest test = new PlaceholderTest(engine, "currenttimeseconds");

        Object result = test.test();
        int res;

        try {
            res = (int) result / 10;
        } catch (ClassCastException ignored) {
            res = ((Double) result).intValue() / 10;
        }

        Assert.assertEquals(res, new Date().getTime() / 1000 / 10);
    }

    @Test
    public void testEmptySlot() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        int firstEmptySlot = 2;

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.firstEmpty()).thenReturn(firstEmptySlot);

        JsTest test = new PlaceholderTest(engine, "emptyslot")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(firstEmptySlot, result);
    }

    @Test
    public void testEmptySlots() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);

        int from = 2;
        int to = 20;

        ItemStack[] contents = new ItemStack[36];
        Arrays.fill(contents, from, to, mock(ItemStack.class));

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getContents()).thenReturn(contents);

        JsTest test = new PlaceholderTest(engine, "emptyslots")
                .addVariable("player", player);

        Object result = test.test();
        int res;

        try {
            res = (int) result;
        } catch (ClassCastException ignored) {
            res = ((Double) result).intValue();
        }

        Assert.assertEquals(contents.length - (to - from), res);
    }

    @Test
    public void testEntityName() throws Exception {
        class FakeEvent {
            public Entity getEntity() {
                return null;
            }
        }

        FakeEvent event = mock(FakeEvent.class);
        Entity entity = mock(Entity.class);
        String name = "CustomEntity";

        when(event.getEntity()).thenReturn(entity);
        when(entity.getName()).thenReturn(name);

        JsTest test = new PlaceholderTest(engine, "entityname")
                .addVariable("event", event);

        String result = (String) test.test();

        Assert.assertEquals(name, result);
    }

    @Test
    public void testExp() throws Exception {
        Player player = mock(Player.class);

        float exp = 10;

        when(player.getExp()).thenReturn(exp);

        JsTest test = new PlaceholderTest(engine, "exp")
                .addVariable("player", player);

        Object result = test.test();
        float res;

        try {
            res = (float) result;
        } catch (ClassCastException ignored) {
            res = ((Double) result).floatValue();
        }

        Assert.assertEquals(exp, res, 0);
    }

    @Test
    public void testExpLevel() throws Exception {
        Player player = mock(Player.class);

        int level = 10;

        when(player.getLevel()).thenReturn(level);

        JsTest test = new PlaceholderTest(engine, "explevel")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(level, result);
    }

    @Test
    public void testFirstGroup() throws Exception {
        class FakePermission {
            public String[] getPlayerGroups(Object any, Player player) {
                return null;
            }
        }

        class FakeVault {
            public FakePermission permission() {
                return null;
            }
        }

        FakeVault vault = mock(FakeVault.class);
        FakePermission permission = mock(FakePermission.class);
        Player player = mock(Player.class);
        String[] group = {"parent", "child"};

        when(vault.permission()).thenReturn(permission);
        when(permission.getPlayerGroups(null, player)).thenReturn(group);

        JsTest test = new PlaceholderTest(engine, "firstgroup")
                .addVariable("player", player)
                .addVariable("vault", vault);

        String result = (String) test.test();

        Assert.assertEquals(group[0], result);
    }

    @Test
    public void testFood() throws Exception {
        Player player = mock(Player.class);

        int food = 10;

        when(player.getFoodLevel()).thenReturn(food);

        JsTest test = new PlaceholderTest(engine, "food")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(food, result);
    }

    @Test
    public void testGameMode() throws Exception {
        Player player = mock(Player.class);

        GameMode gameMode = GameMode.CREATIVE;

        when(player.getGameMode()).thenReturn(gameMode);

        JsTest test = new PlaceholderTest(engine, "gamemode")
                .addVariable("player", player);

        Object result = test.test();
        System.out.println(result);

        Assert.assertEquals(gameMode.name(), result);
    }

    @Test
    public void testGroup() throws Exception {
        class FakePermission {
            public String[] getPlayerGroups(Object any, Player player) {
                return null;
            }
        }

        class FakeVault {
            public FakePermission permission() {
                return null;
            }
        }

        FakeVault vault = mock(FakeVault.class);
        FakePermission permission = mock(FakePermission.class);
        Player player = mock(Player.class);
        String[] group = {"parent", "child"};

        when(vault.permission()).thenReturn(permission);
        when(permission.getPlayerGroups(null, player)).thenReturn(group);

        JsTest test = new PlaceholderTest(engine, "group")
                .addVariable("player", player)
                .addVariable("vault", vault);

        String[] result = (String[]) test.test();

        Assert.assertArrayEquals(group, result);
    }

    @Test
    public void testHasEffect1() throws Exception {
        class FakePotionEffectType {
            public PotionEffectType getByName(String name) {
                return null;
            }
        }

        class FakeJava {
            public FakePotionEffectType type(String name) {
                return null;
            }
        }

        FakeJava java = mock(FakeJava.class);
        FakePotionEffectType potionEffectType = mock(FakePotionEffectType.class);

        Player player = mock(Player.class);
        PotionEffectType potionEffect = mock(PotionEffectType.class);

        String potionEffectName = "TEST";

        when(java.type("org.bukkit.potion.PotionEffectType")).thenReturn(potionEffectType);
        when(potionEffectType.getByName(potionEffectName)).thenReturn(potionEffect);
        when(player.hasPotionEffect(potionEffect)).thenReturn(true);

        JsTest test = new PlaceholderTest(engine, "haseffect")
                .addVariable("player", player)
                .addVariable("Java", java);

        boolean result = (boolean) test.withArgs(potionEffectName).test();

        Assert.assertTrue(result);
        Assert.assertEquals(0, test.getOverload(potionEffectName));
    }

    @Test
    public void testHasPermission() throws Exception {
        Player player = mock(Player.class);

        String permission = "world.break";
        boolean exist = false;

        when(player.hasPermission(permission)).thenReturn(exist);

        JsTest test = new PlaceholderTest(engine, "haspermission")
                .addVariable("player", player);

        boolean result = (boolean) test.withArgs(permission).test();

        Assert.assertFalse(result);
        Assert.assertEquals(0, test.getOverload(permission));
    }

    @Test
    public void testHealth() throws Exception {
        Player player = mock(Player.class);

        double health = 10;

        when(player.getHealth()).thenReturn(health);

        JsTest test = new PlaceholderTest(engine, "health")
                .addVariable("player", player);

        double result = (double) test.test();

        Assert.assertEquals(health, result, 0);
    }

    @Test
    public void testHeldItem() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInHand()).thenReturn(item);

        JsTest test = new PlaceholderTest(engine, "helditem")
                .addVariable("player", player);

        ItemStack result = (ItemStack) test.test();

        Assert.assertEquals(item, result);
    }

    @Test
    public void testHeldItemDisplayName1() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);

        String displayName = "ItemTitle";

        when(player.getItemInHand()).thenReturn(item);
        when(item.hasItemMeta()).thenReturn(true);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.hasDisplayName()).thenReturn(true);
        when(meta.getDisplayName()).thenReturn(displayName);

        JsTest test = new PlaceholderTest(engine, "helditemdisplayname")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(displayName, result);
    }

    @Test
    public void testHeldItemDisplayName2() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = new ItemStack(Material.AIR);

        when(player.getItemInHand()).thenReturn(item);

        JsTest test = new PlaceholderTest(engine, "helditemdisplayname")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals("", result);
    }

    @Test
    public void testHeldItemDisplayName3() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);

        when(player.getItemInHand()).thenReturn(item);
        when(item.hasItemMeta()).thenReturn(false);

        JsTest test = new PlaceholderTest(engine, "helditemdisplayname")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals("", result);
    }

    @Test
    public void testHeldItemDisplayName4() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);

        when(player.getItemInHand()).thenReturn(item);
        when(item.getItemMeta()).thenReturn(meta);
        when(item.hasItemMeta()).thenReturn(true);
        when(meta.hasDisplayName()).thenReturn(false);

        JsTest test = new PlaceholderTest(engine, "helditemdisplayname")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals("", result);
    }

    @Test
    public void testHeldItemLore() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);

        List<String> lore = new ArrayList<String>() {{
            add("first lore");
            add("second lore");
            add("third lore");
        }};
        int index = 1;

        when(player.getItemInHand()).thenReturn(item);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.getLore()).thenReturn(lore);

        JsTest test = new PlaceholderTest(engine, "helditemlore")
                .addVariable("player", player);

        String result = (String) test.withArgs(index).test();

        Assert.assertEquals(lore.get(index), result);
        Assert.assertEquals(0, test.getOverload(index));
    }

    @Test
    public void testHeldItemName() throws Exception {
        Player player = mock(Player.class);
        ItemStack item = mock(ItemStack.class);
        Material material = Material.STONE;

        when(player.getItemInHand()).thenReturn(item);
        when(item.getType()).thenReturn(material);

        JsTest test = new PlaceholderTest(engine, "helditemname")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(material.name(), result);
    }

    @Test
    public void testId() throws Exception {
        ItemStack item = mock(ItemStack.class);
        Material material = Material.STONE;

        when(item.getType()).thenReturn(material);

        JsTest test = new PlaceholderTest(engine, "id");

        Material result = (Material) test.withArgs(item).test();

        Assert.assertEquals(material, result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testIdName() throws Exception {
        ItemStack item = mock(ItemStack.class);
        Material material = Material.STONE;

        when(item.getType()).thenReturn(material);

        JsTest test = new PlaceholderTest(engine, "idname");

        String result = (String) test.withArgs(item).test();

        Assert.assertEquals(material.name(), result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testIsBurning1() throws Exception {
        Player player = mock(Player.class);

        when(player.getFireTicks()).thenReturn(0);

        JsTest test = new PlaceholderTest(engine, "isburning")
                .addVariable("player", player);

        boolean result = (boolean) test.test();

        Assert.assertFalse(result);
    }

    @Test
    public void testIsBurning2() throws Exception {
        Player player = mock(Player.class);

        when(player.getFireTicks()).thenReturn(10);

        JsTest test = new PlaceholderTest(engine, "isburning")
                .addVariable("player", player);

        boolean result = (boolean) test.test();

        Assert.assertTrue(result);
    }

    @Test
    public void testIsFlying() throws Exception {
        Player player = mock(Player.class);

        when(player.isFlying()).thenReturn(true);

        JsTest test = new PlaceholderTest(engine, "isflying")
                .addVariable("player", player);

        boolean result = (boolean) test.test();

        Assert.assertTrue(result);
    }

    @Test
    public void testIsNumber1() throws Exception {
        int number = 10;

        JsTest test = new PlaceholderTest(engine, "isnumber");

        boolean result = (boolean) test.withArgs(number).test();

        Assert.assertTrue(result);
        Assert.assertEquals(0, test.getOverload(number));
    }

    @Test
    public void testIsNumber2() throws Exception {
        float number = 10.5f;

        JsTest test = new PlaceholderTest(engine, "isnumber");

        boolean result = (boolean) test.withArgs(number).test();

        Assert.assertTrue(result);
        Assert.assertEquals(0, test.getOverload(number));
    }

    @Test
    public void testIsNumber3() throws Exception {
        String number = "10";

        JsTest test = new PlaceholderTest(engine, "isnumber");

        boolean result = (boolean) test.withArgs(number).test();

        Assert.assertTrue(result);
        Assert.assertEquals(1, test.getOverload(number));
    }

    @Test
    public void testIsNumber4() throws Exception {
        String number = "10.5";

        JsTest test = new PlaceholderTest(engine, "isnumber");

        boolean result = (boolean) test.withArgs(number).test();

        Assert.assertTrue(result);
        Assert.assertEquals(1, test.getOverload(number));
    }

    @Test
    public void testIsNumber5() throws Exception {
        String number = "10a.5";

        JsTest test = new PlaceholderTest(engine, "isnumber");

        boolean result = (boolean) test.withArgs(number).test();

        Assert.assertFalse(result);
        Assert.assertEquals(1, test.getOverload(number));
    }

    @Test
    public void testIsOp() throws Exception {
        Player player = mock(Player.class);

        when(player.isOp()).thenReturn(false);

        JsTest test = new PlaceholderTest(engine, "isop")
                .addVariable("player", player);

        boolean result = (boolean) test.test();

        Assert.assertFalse(result);
    }

    @Test
    public void testIsSneaking() throws Exception {
        Player player = mock(Player.class);

        when(player.isSneaking()).thenReturn(false);

        JsTest test = new PlaceholderTest(engine, "issneaking")
                .addVariable("player", player);

        boolean result = (boolean) test.test();

        Assert.assertFalse(result);
    }

    @Test
    public void testIsSprinting() throws Exception {
        Player player = mock(Player.class);

        when(player.isSprinting()).thenReturn(true);

        JsTest test = new PlaceholderTest(engine, "issprinting")
                .addVariable("player", player);

        boolean result = (boolean) test.test();

        Assert.assertTrue(result);
    }

    @Test
    public void testKillerName() throws Exception {
        class FakeEvent extends PlayerDeathEvent {
            public FakeEvent(Player player, List<ItemStack> drops, int droppedExp, String deathMessage) {
                super(player, drops, droppedExp, deathMessage);
            }

            public Player getEntity() {
                return null;
            }
        }

        FakeEvent event = mock(FakeEvent.class);
        Player killer = mock(Player.class);
        String killerName = "Killer";

        when(event.getEntity()).thenReturn(killer);
        when(killer.getName()).thenReturn(killerName);

        JsTest test = new PlaceholderTest(engine, "killername")
                .addVariable("event", event);

        String result = (String) test.test();

        Assert.assertEquals(killerName, result);
    }

    @Test
    public void testLore() throws Exception {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);

        String loreString = "First lore\nSecond lore\nThird Lore";
        List<String> lore = Arrays.stream(loreString.split("\n")).collect(Collectors.toList());

        when(item.hasItemMeta()).thenReturn(true);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.hasLore()).thenReturn(true);
        when(meta.getLore()).thenReturn(lore);

        JsTest test = new PlaceholderTest(engine, "lore");

        String result = (String) test.withArgs(item).test();

        Assert.assertEquals(loreString, result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testMaxHealth() throws Exception {
        Player player = mock(Player.class);
        double health = 20;

        when(player.getMaxHealth()).thenReturn(health);

        JsTest test = new PlaceholderTest(engine, "maxhealth")
                .addVariable("player", player);

        double result = (double) test.test();

        Assert.assertEquals(health, result, 0);
    }

    @Test
    public void testMoney() throws Exception {
        class FakeVault {
            public double balance(Player player) {
                return 0;
            }
        }

        FakeVault vault = mock(FakeVault.class);
        Player player = mock(Player.class);
        double balance = 100D;

        when(vault.balance(player)).thenReturn(balance);

        JsTest test = new PlaceholderTest(engine, "money")
                .addVariable("player", player)
                .addVariable("vault", vault);

        double result = (double) test.test();

        Assert.assertEquals(balance, result, 0);
    }

    @Test
    public void testMysql() throws Exception {
        class FakeMysqlHelper {
            public String get(String key) {
                return null;
            }
        }

        class FakePlugin {
            public FakeMysqlHelper getMysqlHelper() {
                return null;
            }
        }

        FakeMysqlHelper mysqlHelper = mock(FakeMysqlHelper.class);
        FakePlugin plugin = mock(FakePlugin.class);
        Injector injector = mock(Injector.class);
        Player player = mock(Player.class);

        String key = "testKey";
        String value = "testValue";

        when(plugin.getMysqlHelper()).thenReturn(mysqlHelper);
        when(mysqlHelper.get(key)).thenReturn(value);
        when(injector.getInstance(any(Class.class))).thenReturn(mysqlHelper);

        JsTest test = new PlaceholderTest(engine, "mysql")
                .addVariable("injector", injector)
                .addVariable("player", player)
                .addVariable("plugin", plugin);

        String result = (String) test.withArgs(key).test();

        Assert.assertEquals(value, result);
        Assert.assertEquals(0, test.getOverload(key));
    }

    @Test
    public void testName1() throws Exception {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        String displayName = "testDisplayName";

        when(item.getItemMeta()).thenReturn(meta);
        when(meta.hasDisplayName()).thenReturn(true);
        when(meta.getDisplayName()).thenReturn(displayName);

        JsTest test = new PlaceholderTest(engine, "name");

        String result = (String) test.withArgs(item).test();

        Assert.assertEquals(displayName, result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testName2() throws Exception {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        Material material = Material.STONE;

        when(item.getItemMeta()).thenReturn(meta);
        when(item.getType()).thenReturn(material);
        when(meta.hasDisplayName()).thenReturn(false);

        JsTest test = new PlaceholderTest(engine, "name");

        String result = (String) test.withArgs(item).test();

        Assert.assertEquals(material.name().toLowerCase(), result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testName3() throws Exception {
        ItemStack item = mock(ItemStack.class);
        Material material = Material.STONE;

        when(item.getItemMeta()).thenReturn(null);
        when(item.getType()).thenReturn(material);

        JsTest test = new PlaceholderTest(engine, "name");

        String result = (String) test.withArgs(item).test();

        Assert.assertEquals(material.name().toLowerCase(), result);
        Assert.assertEquals(0, test.getOverload(item));
    }

    @Test
    public void testOnlinePlayers() throws Exception {
        class FakeBukkitUtil {
            public List<Player> getOnlinePlayers() {
                return null;
            }
        }

        class FakeJava {
            public FakeBukkitUtil type(String name) {
                return null;
            }
        }

        FakeJava java = mock(FakeJava.class);
        FakeBukkitUtil bukkitUtil = mock(FakeBukkitUtil.class);
        List<Player> players = new ArrayList<Player>() {{
            for (int i = 0; i < 10; i++)
                add(mock(Player.class));
        }};

        when(java.type("io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil")).thenReturn(bukkitUtil);
        when(bukkitUtil.getOnlinePlayers()).thenReturn(players);

        JsTest test = new PlaceholderTest(engine, "onlineplayers")
                .addVariable("Java", java);

        int result = (int) test.test();

        Assert.assertEquals(players.size(), result);
    }

    @Test
    public void testPackList1() throws Exception {
        String type = "String";

        JsTest test = new PlaceholderTest(engine, "packlist");

        String[] result = (String[]) test.withArgs(type).test();

        Assert.assertArrayEquals(new String[]{}, result);
    }

    @Test
    public void testPackList2() throws Exception {
        String type = "Double";
        Double[] value = {10.0, -5.5};

        JsTest test = new PlaceholderTest(engine, "packlist");

        Double[] result = (Double[]) test.withArgs(type, value[0], value[1]).test();

        Assert.assertArrayEquals(value, result);
    }

    @Test
    public void testPitch() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        float pitch = 10;

        when(player.getLocation()).thenReturn(location);
        when(location.getPitch()).thenReturn(pitch);

        JsTest test = new PlaceholderTest(engine, "pitch")
                .addVariable("player", player);

        Object result = test.test();
        float res;

        try {
            res = (float) result;
        } catch (ClassCastException ignored) {
            res = ((Double) result).floatValue();
        }

        Assert.assertEquals(pitch, res, 0);
    }

    @Test
    public void testPlayerInv() throws Exception {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack item = mock(ItemStack.class);
        int slot = 2;

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItem(slot)).thenReturn(item);

        JsTest test = new PlaceholderTest(engine, "playerinv")
                .addVariable("player", player);

        ItemStack result = (ItemStack) test.withArgs(slot).test();

        Assert.assertEquals(item, result);
        Assert.assertEquals(0, test.getOverload(slot));
    }

    @Test
    public void testPlayerLoc() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);

        World world = mock(World.class);
        int x = 100;
        int y = 50;
        int z = -100;

        String worldName = "MainWorld";

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(x);
        when(location.getBlockY()).thenReturn(y);
        when(location.getBlockZ()).thenReturn(z);
        when(world.getName()).thenReturn(worldName);

        String expected = worldName + "@" + x + "," + y + "," + z;

        JsTest test = new PlaceholderTest(engine, "playerloc")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPlayerLocExact() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);

        World world = mock(World.class);
        double x = 100.5;
        double y = 50.5;
        double z = -100.5;

        String worldName = "MainWorld";

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);
        when(location.getX()).thenReturn(x);
        when(location.getY()).thenReturn(y);
        when(location.getZ()).thenReturn(z);
        when(world.getName()).thenReturn(worldName);

        String expected = worldName + "@" + x + "," + y + "," + z;

        JsTest test = new PlaceholderTest(engine, "playerlocexact")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPlayerName() throws Exception {
        Player player = mock(Player.class);
        String playerName = "TestPlayer";

        when(player.getName()).thenReturn(playerName);

        JsTest test = new PlaceholderTest(engine, "playername")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(playerName, result);
    }

    @Test
    public void testPlayerUUID() throws Exception {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);

        JsTest test = new PlaceholderTest(engine, "playeruuid")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(uuid.toString(), result);
    }

    @Test
    public void testPrefix() throws Exception {
        class FakeChat {
            public String getPlayerPrefix(Player player) {
                return null;
            }
        }

        class FakeVault {
            public FakeChat chat() {
                return null;
            }
        }

        FakeVault vault = mock(FakeVault.class);
        FakeChat chat = mock(FakeChat.class);
        Player player = mock(Player.class);
        String prefix = "TEST_PREFIX";

        when(vault.chat()).thenReturn(chat);
        when(chat.getPlayerPrefix(player)).thenReturn(prefix);

        JsTest test = new PlaceholderTest(engine, "prefix")
                .addVariable("player", player)
                .addVariable("vault", vault);

        String result = (String) test.test();

        Assert.assertEquals(prefix, result);
    }

    @Test
    public void testRound1() throws Exception {
        double number = 10.59291;
        double rounded = 11;

        JsTest test = new PlaceholderTest(engine, "round");

        double result = (Double) test.withArgs(number).test();

        Assert.assertEquals(rounded, result, 0);
        Assert.assertEquals(0, test.getOverload(number));
    }

    @Test
    public void testRound2() throws Exception {
        double number = 10.59291;
        double rounded = 10.59;

        JsTest test = new PlaceholderTest(engine, "round");

        double result = (Double) test.withArgs(number, 2).test();

        Assert.assertEquals(rounded, result, 0);
        Assert.assertEquals(1, test.getOverload(number, 2));
    }

    @Test
    public void testSlot() throws Exception {
        InventoryEvent event = mock(InventoryEvent.class);
        Inventory inventory = mock(Inventory.class);
        ItemStack item = mock(ItemStack.class);
        int slot = 2;

        when(event.getInventory()).thenReturn(inventory);
        when(inventory.getSize()).thenReturn(36);
        when(inventory.getItem(slot)).thenReturn(item);

        JsTest test = new PlaceholderTest(engine, "slot")
                .addVariable("event", event);

        ItemStack result = (ItemStack) test.withArgs(slot).test();

        Assert.assertEquals(item, result);
        Assert.assertEquals(0, test.getOverload(slot));
    }

    @Test
    public void testSuffix() throws Exception {
        class FakeChat {
            public String getPlayerSuffix(Player player) {
                return null;
            }
        }

        class FakeVault {
            public FakeChat chat() {
                return null;
            }
        }

        FakeVault vault = mock(FakeVault.class);
        FakeChat chat = mock(FakeChat.class);
        Player player = mock(Player.class);
        String suffix = "TEST_PREFIX";

        when(vault.chat()).thenReturn(chat);
        when(chat.getPlayerSuffix(player)).thenReturn(suffix);

        JsTest test = new PlaceholderTest(engine, "suffix")
                .addVariable("player", player)
                .addVariable("vault", vault);

        String result = (String) test.test();

        Assert.assertEquals(suffix, result);
    }

    @Test
    public void testTime() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);
        long time = 1000;

        when(player.getWorld()).thenReturn(world);
        when(world.getTime()).thenReturn(time);

        JsTest test = new PlaceholderTest(engine, "time")
                .addVariable("player", player);

        Object result = test.test();
        long res;

        try {
            res = (long) result;
        } catch (ClassCastException ignored) {
            res = ((Integer) result).longValue();
        }

        Assert.assertEquals(time, res);
    }

    @Test
    public void testTps() throws Exception {
        class FakeTpsHelper {
            public double getTPS() {
                return 0;
            }
        }

        class FakePlugin {
            public FakeTpsHelper getTpsHelper() {
                return null;
            }
        }

        FakeTpsHelper tpsHelper = mock(FakeTpsHelper.class);
        FakePlugin plugin = mock(FakePlugin.class);
        double tps = 20;

        when(plugin.getTpsHelper()).thenReturn(tpsHelper);
        when(tpsHelper.getTPS()).thenReturn(tps);

        JsTest test = new PlaceholderTest(engine, "tps")
                .addVariable("plugin", plugin);

        double result = (double) test.test();

        Assert.assertEquals(tps, result, 0);
    }

    @Test
    public void testWorld() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);

        when(player.getWorld()).thenReturn(world);

        JsTest test = new PlaceholderTest(engine, "world")
                .addVariable("player", player);

        World result = (World) test.test();

        Assert.assertEquals(world, result);
    }

    @Test
    public void testWorldName() throws Exception {
        Player player = mock(Player.class);
        World world = mock(World.class);
        String worldName = "TestWorld";

        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn(worldName);

        JsTest test = new PlaceholderTest(engine, "worldname")
                .addVariable("player", player);

        String result = (String) test.test();

        Assert.assertEquals(worldName, result);
    }

    @Test
    public void testX() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        int x = 10;

        when(player.getLocation()).thenReturn(location);
        when(location.getBlockX()).thenReturn(x);

        JsTest test = new PlaceholderTest(engine, "x")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(x, result);
    }

    @Test
    public void testY() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        int y = 10;

        when(player.getLocation()).thenReturn(location);
        when(location.getBlockY()).thenReturn(y);

        JsTest test = new PlaceholderTest(engine, "y")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(y, result);
    }

    @Test
    public void testYaw() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        float yaw = 10;

        when(player.getLocation()).thenReturn(location);
        when(location.getYaw()).thenReturn(yaw);

        JsTest test = new PlaceholderTest(engine, "yaw")
                .addVariable("player", player);

        Object result = test.test();
        float res;

        try {
            res = (float) result;
        } catch (ClassCastException ignored) {
            res = ((Double) result).floatValue();
        }

        Assert.assertEquals(yaw, res, 0);
    }

    @Test
    public void testZ() throws Exception {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        int z = 10;

        when(player.getLocation()).thenReturn(location);
        when(location.getBlockZ()).thenReturn(z);

        JsTest test = new PlaceholderTest(engine, "z")
                .addVariable("player", player);

        int result = (int) test.test();

        Assert.assertEquals(z, result);
    }
}
