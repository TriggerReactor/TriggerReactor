/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeItemStack;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryTriggerManager extends AbstractInventoryTriggerManager<ItemStack> {
    public InventoryTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "InventoryTrigger"), ItemStack.class, SpongeItemStack::new);
    }

    /**
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(Player player, String name) {
        Sponge.getCauseStackManager().pushCause(player);
        return openGUI(new SpongePlayer(player), name);
    }

    @Listener
    public void onOpen(InteractInventoryEvent.Open e) {
        Inventory inv = e.getTargetInventory();
        if (!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if (carrier == null)
            return;

        if (!this.hasInventoryOpen(new SpongeInventory(inventory, carrier)))
            return;

        InventoryTrigger trigger = getTriggerForOpenInventory(new SpongeInventory(inventory, carrier));

        Map<String, Object> varMap = getSharedVarsForInventory(new SpongeInventory(inventory, carrier));
        varMap.put("player", e.getCause().first(Player.class).orElse(null));
        varMap.put("trigger", "open");

        Inventory grids = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));
        varMap.put("inventory", grids.first());

        trigger.activate(e, varMap);
    }

    @Listener
    public void onClick(ClickInventoryEvent e) {
        Inventory inv = e.getTargetInventory();
        if (!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if (carrier == null)
            return;

        if (!this.hasInventoryOpen(new SpongeInventory(inventory, carrier)))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(new SpongeInventory(inventory, carrier));

        // just always cancel if it's GUI
        e.setCancelled(true);

        Player player = e.getCause().first(Player.class).orElse(null);
        if (player == null)
            return;

        int rawSlot = -1;
        SlotTransaction slotTransaction = null;

        List<SlotTransaction> transactions = e.getTransactions();
        if (!transactions.isEmpty()) {
            slotTransaction = e.getTransactions().get(0);
            Slot slot = slotTransaction.getSlot();
            SlotIndex slotIndex = slot.getInventoryProperty(SlotIndex.class).orElse(null);
            rawSlot = slotIndex.getValue();
        }

        Map<String, Object> varMap = getSharedVarsForInventory(new SpongeInventory(inventory, carrier));
        ItemStackSnapshot clickedItemOpt = slotTransaction == null ? ItemStackSnapshot.NONE : slotTransaction.getOriginal();
        varMap.put("item", clickedItemOpt.createStack());
        varMap.put("slot", rawSlot);
        varMap.put("click", e.getClass().getSimpleName());
        varMap.put("trigger", "click");

        Inventory grids = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));
        varMap.put("inventory", grids.first());

        trigger.activate(e, varMap);
    }

    @Listener
    public void onClose(InteractInventoryEvent.Close e, @First Player player) {
        if (player == null)
            return;

        Inventory inv = e.getTargetInventory();
        if (!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if (carrier == null)
            return;

        onInventoryClose(e, new SpongePlayer(player), new SpongeInventory(inv, carrier));
    }

    @Override
    protected void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        GridInventory gridInv = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));

        for (int i = 0; i < size; i++) {
            IItemStack item = trigger.getItems()[i];
            if (item != null) {
                Slot slot = gridInv.getSlot(SlotIndex.of(i)).orElse(null);
                slot.set(getColoredItem(item.get()));
            }
        }
    }

    /**
     * @param item
     * @return copy of colored item
     */
    private ItemStack getColoredItem(ItemStack item) {
        item = item.copy();

        Text displayName = item.get(Keys.DISPLAY_NAME).orElse(null);
        if (displayName != null)
            item.offer(Keys.DISPLAY_NAME, TextUtil.colorStringToText(displayName.toPlain()));

        List<Text> lores = item.get(Keys.ITEM_LORE).orElse(null);
        if (lores != null) {
            for (int i = 0; i < lores.size(); i++) {
                lores.set(i, TextUtil.colorStringToText(lores.get(i).toPlain()));
            }
            item.offer(Keys.ITEM_LORE, lores);
        }

        return item;
    }

    @Override
    protected IInventory createInventory(int size, String name) {
        name = name.replaceAll("_", " ");
        Text text = TextUtil.colorStringToText(name);
        Carrier dummy = new DummyCarrier();
        Inventory inv = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .withCarrier(dummy)
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, size / 9))
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(text))
                .build(plugin);
        return new SpongeInventory(inv, dummy);
    }

    private class DummyCarrier implements Carrier {
        private final Object uniqueObject = new Object();

        @Override
        public CarriedInventory<? extends Carrier> getInventory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int hashCode() {
            return uniqueObject.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DummyCarrier))
                return false;

            return uniqueObject.equals(((DummyCarrier) obj).uniqueObject);
        }
    }

    static {
        GsonConfigSource.registerTypeAdapter(ItemStack.class, (src, typeOfSrc, context) -> {
            DataContainer container = src.toContainer();
            Map<String, Object> map = new HashMap<>();
            container.getValues(true).forEach((dataQuery, o) -> map.put(dataQuery.toString(), o));
            return context.serialize(map);
        });

        GsonConfigSource.registerTypeAdapter(ItemStack.class, map -> {
            DataContainer container = DataContainer.createNew();
            map.forEach((s, o) -> container.set(DataQuery.of(".", s), o));
            return Sponge.getDataManager().deserialize(ItemStack.class, container)
                    .orElseThrow(() -> new RuntimeException("Cannot deserialized [" + map + "] to ItemStack."));
        });
    }
}
