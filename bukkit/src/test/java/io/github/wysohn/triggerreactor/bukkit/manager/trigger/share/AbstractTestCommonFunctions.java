package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.TestCommonFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.HashMap;

/**
 * Test driving class for both legacy and latest bukkit.
 * Since the structure of legacy and latest bukkit yet shares a lot of similarities,
 * we don't have to write each test case for each different platforms.
 *
 * If, however, there are some tests that has to be platform specific,
 * write them in the child class instead.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({TriggerReactor.class, Bukkit.class})
public abstract class AbstractTestCommonFunctions extends TestCommonFunctions<AbstractCommonFunctions> {
    protected TriggerReactor mockMain;
    protected PluginManager mockPluginManager;
    protected World mockWorld;

    public AbstractTestCommonFunctions(AbstractCommonFunctions fn) {
        super(fn);
    }

    @Before
    public void init(){
        mockMain = Mockito.mock(TriggerReactor.class);
        mockPluginManager = Mockito.mock(PluginManager.class);
        mockWorld = Mockito.mock(World.class);

        PowerMockito.mockStatic(TriggerReactor.class);
        Mockito.when(TriggerReactor.getInstance()).thenReturn(mockMain);

        PowerMockito.mockStatic(Bukkit.class);
        Mockito.when(Bukkit.getPluginManager()).thenReturn(mockPluginManager);
        Mockito.when(Bukkit.getWorld(Mockito.anyString())).then(
                invocation -> {
                    return mockWorld;
                });
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

    public static FakeInventory fInventory(AbstractTestCommonFunctions test, ItemStack... items){
        FakeInventory inv = test.new FakeInventory();

        for(int i = 0; i < Math.min(inv.contents.length, items.length); i++){
            inv.contents[i] = items[i];
        }

        return inv;
    }

    protected abstract boolean isSimilar(ItemStack IS1, ItemStack IS2);

    protected abstract boolean isEqual(ItemStack IS1, ItemStack IS2);

    protected PlayerInventory preparePlayerInventory(Player mockPlayer, FakeInventory inv){
        PlayerInventory mockInventory = Mockito.mock(PlayerInventory.class);

        Mockito.when(mockPlayer.getInventory()).thenReturn(mockInventory);
        Mockito.when(mockInventory.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt()))
                .then(invocation -> {
                    ItemStack target = invocation.getArgument(0);
                    int amount = invocation.getArgument(1);

                    int count = 0;
                    for(ItemStack IS : inv.contents){
                        if (IS == null)
                            continue;

                        if(isSimilar(IS, target))
                            count += IS.getAmount();

                        if(count >= amount)
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
    public void testLocation(){
        Location loc1 = new Location(mockWorld, 1, 2, 3);
        Location loc2 = new Location(mockWorld, 4, 5, 6, 0.5F, 0.6F);

        Mockito.when(mockWorld.getName()).thenReturn("test");
        Assert.assertEquals(loc1, fn.location("test", 1, 2, 3));

        Mockito.when(mockWorld.getName()).thenReturn("test2");
        Assert.assertEquals(loc2, fn.location("test2", 4, 5, 6, 0.5, 0.6));
    }
}
