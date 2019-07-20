package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import io.github.wysohn.triggerreactor.core.manager.trigger.share.TestCommonFunctions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.HashMap;

public abstract class AbstractTestCommonFunctions extends TestCommonFunctions<AbstractCommonFunctions> {
    public AbstractTestCommonFunctions(AbstractCommonFunctions fn) {
        super(fn);
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
}
