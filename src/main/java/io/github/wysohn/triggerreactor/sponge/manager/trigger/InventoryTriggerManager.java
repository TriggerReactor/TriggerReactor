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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeItemStack;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.sponge.tools.ConfigurationUtil;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class InventoryTriggerManager extends AbstractInventoryTriggerManager implements SpongeConfigurationFileIO{
    public InventoryTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "InventoryTrigger"));
    }

    @Override
    public <T> T getData(File file, String key, T def) throws IOException {
        if(key.equals(ITEMS)) {
            int size = getData(file, SIZE, 0);
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
            ConfigurationNode conf = loader.load();

            Map<Integer, IItemStack> items = new HashMap<>();

            ConfigurationNode node = ConfigurationUtil.getNodeByKeyString(conf, ITEMS);
            if(node != null)
                parseItemsList(node, items, size);

            return (T) items;
        }else {
            return SpongeConfigurationFileIO.super.getData(file, key, def);
        }
    }

    @Override
    public void setData(File file, String key, Object value) throws IOException {
        if(key.equals(ITEMS)) {
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
            ConfigurationNode conf = loader.load();

            IItemStack[] items = (IItemStack[]) value;

            writeItemsList(ConfigurationUtil.getNodeByKeyString(conf, ITEMS), items);

            loader.save(conf);
        }else {
            SpongeConfigurationFileIO.super.setData(file, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseItemsList(ConfigurationNode itemSection, Map<Integer, IItemStack> items, int size) {
        for(int i = 0; i < size; i++){
            ConfigurationNode section = ConfigurationUtil.getNodeByKeyString(itemSection, String.valueOf(i));
            if(section.isVirtual())
                continue;

            ItemStackSnapshot IS;
            try {
                IS = section.getValue(TypeTokens.ITEM_SNAPSHOT_TOKEN);
            } catch (ObjectMappingException e) {
                e.printStackTrace();//temp
                continue;
            }

            items.put(i, new SpongeItemStack(IS.createStack()));
        }
    }

    private void writeItemsList(ConfigurationNode itemSection, IItemStack[] items) {
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;

            ItemStack item = items[i].get();

            ConfigurationNode section = ConfigurationUtil.getNodeByKeyString(itemSection, String.valueOf(i));
            if(section.isVirtual()) {
                itemSection.setValue(String.valueOf(i));
                section = ConfigurationUtil.getNodeByKeyString(itemSection, String.valueOf(i));
            }

            try {
                section.setValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, item.createSnapshot());
            } catch (ObjectMappingException e) {
                e.printStackTrace();//temp
                continue;
            }
        }
    }

    /**
    *
    * @param player
    * @param name
    * @return the opened Inventory's reference; null if no Inventory Trigger found
    */
   public IInventory openGUI(Player player, String name){
       return openGUI(new SpongePlayer(player), name);
   }

    @Listener
    public void onOpen(InteractInventoryEvent.Open e){
        Inventory inv = e.getTargetInventory();
        if(!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if(carrier == null)
            return;

        if (!this.hasInventoryOpen(new SpongeInventory(inventory, carrier)))
            return;

        InventoryTrigger trigger = getTriggerForOpenInventory(new SpongeInventory(inventory, carrier));

        Map<String, Object> varMap = getSharedVarsForInventory(new SpongeInventory(inventory, carrier));
        varMap.put("player", e.getCause().first(Player.class));
        varMap.put("trigger", "open");
        varMap.put("inventory", inv);

        trigger.activate(e, varMap);
    }

    @Listener
    public void onClick(ClickInventoryEvent e) {
        Inventory inv = e.getTargetInventory();
        if(!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if(carrier == null)
            return;

        if (!this.hasInventoryOpen(new SpongeInventory(inventory, carrier)))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(new SpongeInventory(inventory, carrier));

        // just always cancel if it's GUI
        e.setCancelled(true);

        Player player = e.getCause().first(Player.class).orElse(null);
        if(player == null)
            return;

        SlotIndex slotIndex = e.getTransactions().get(0).getSlot().getInventoryProperty(SlotIndex.class).orElse(null);
        int rawSlot = slotIndex.getValue();

        Map<String, Object> varMap = getSharedVarsForInventory(new SpongeInventory(inventory, carrier));
        if(trigger.getItems()[rawSlot] == null)
            varMap.put("item", ItemStack.of(ItemTypes.AIR, 1));
        else{
            ItemStack item = trigger.getItems()[rawSlot].get();
            varMap.put("item", item.copy());
        }
        varMap.put("slot", rawSlot);
        varMap.put("click", e.getClass().getSimpleName());
        varMap.put("trigger", "click");
        varMap.put("inventory", inv);

        trigger.activate(e, varMap);
    }

    @Listener
    public void onClose(InteractInventoryEvent.Close e, @First Player player){
        if(player == null)
            return;

        Inventory inv = e.getTargetInventory();
        if(!(inv instanceof CarriedInventory))
            return;

        CarriedInventory inventory = (CarriedInventory) inv;
        Carrier carrier = (Carrier) inventory.getCarrier().orElse(null);

        if(carrier == null)
            return;

        onInventoryClose(e, new SpongePlayer(player), new SpongeInventory(inv, carrier));
    }

    @Override
    protected void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        GridInventory gridInv = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));

        for(int i = 0; i < size; i++){
            IItemStack item = trigger.getItems()[i];
            if(item != null){
                Slot slot = gridInv.getSlot(SlotIndex.of(i)).orElse(null);
                slot.set(getColoredItem(item.get()));
            }
        }
    }

    /**
    *
    * @param item
    * @return copy of colored item
    */
   private ItemStack getColoredItem(ItemStack item) {
       item = item.copy();

       Text displayName = item.get(Keys.DISPLAY_NAME).orElse(null);
       if(displayName != null)
           item.offer(Keys.DISPLAY_NAME, TextUtil.colorStringToText(displayName.toPlain()));

       List<Text> lores = item.get(Keys.ITEM_LORE).orElse(null);
       if(lores != null){
           for(int i = 0; i < lores.size(); i++) {
               lores.set(i, TextUtil.colorStringToText(lores.get(i).toPlain()));
           }
           item.offer(Keys.ITEM_LORE, lores);
       }

       return item;
   }

    @Override
    protected void deleteInfo(Trigger trigger) {
        File yamlFile = new File(folder, trigger.getTriggerName()+".yml");
        FileUtil.delete(yamlFile);
        File triggerFile = new File(folder, trigger.getTriggerName());
        FileUtil.delete(triggerFile);
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

    private class DummyCarrier implements Carrier{
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
            if(!(obj instanceof DummyCarrier))
                return false;

            return uniqueObject.equals(((DummyCarrier)obj).uniqueObject);
        }
    }
}
