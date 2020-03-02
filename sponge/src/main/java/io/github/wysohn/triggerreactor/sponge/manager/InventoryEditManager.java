package io.github.wysohn.triggerreactor.sponge.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractInventoryEditManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeItemStack;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.UUID;

public class InventoryEditManager extends AbstractInventoryEditManager {
	private static Text savePrompt;
	
	static {
		TextStyle bold = TextStyles.of().bold(true);
		Text newline = Text.of("\n");
		Text save = Text.builder()
				        .append(Text.of(CHECK + " Save"))
			         	.color(TextColors.GREEN)
			        	.style(bold)
			        	.onClick(TextActions.runCommand("/trg links inveditsave"))
		        		.build();
		Text continue_ = Text.builder()
				             .append(Text.of(PENCIL + " Continue editing"))
				             .color(TextColors.YELLOW)
				             .style(bold)
				             .onClick(TextActions.runCommand("/trg links inveditcontinue"))
				             .build();
		Text cancel = Text.builder()
				          .append(Text.of(X + " Cancel"))
				          .color(TextColors.RED)
				          .style(bold)
				          .onClick(TextActions.runCommand("/trg links inveditdiscard"))
				          .build();
		savePrompt = Text.builder().append(save, newline, continue_, newline, cancel).build();
	}

	public InventoryEditManager(TriggerReactor plugin) {
		super(plugin);
	}
	
	//adapted from InventoryTriggerManager
	private Inventory createInventory(int size, String name) {
		return Inventory.builder()
				.of(InventoryArchetypes.CHEST)
				.property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, size / 9))
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(name)))
				.build(plugin);
	}
	
	//adapted from InventoryTriggerManager
	private void fillInventory(InventoryTrigger trigger, int size, Inventory inv) {
        GridInventory gridInv = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));

        for (int i = 0; i < size; i++) {
            IItemStack item = trigger.getItems()[i];
            if (item != null) {
                Slot slot = gridInv.getSlot(SlotIndex.of(i)).orElse(null);
                slot.set(getColoredItem(item.get()));
            }
        }
    }
	
	//copied from InventoryTriggerManager
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
	public void startEdit(IPlayer player, InventoryTrigger trigger) {
		UUID u = player.getUniqueId();
		if (sessions.containsKey(u)) {
			return;
		}
		sessions.put(u, trigger);
		int size = trigger.getItems().length;
		Inventory inv = createInventory(size, trigger.getTriggerName());
		fillInventory(trigger, size, inv);
		player.openInventory(new SpongeInventory(inv, null));
	}

	@Override
	public void continueEdit(IPlayer player) {
		UUID u = player.getUniqueId();
		if (!suspended.containsKey(u)) {
			return;
		}
		
		IInventory inv = suspended.remove(u);
		player.openInventory(inv);
	}

	@Override
	public void discardEdit(IPlayer player) {
		UUID u = player.getUniqueId();
		if (!suspended.containsKey(u)) {
			return;
		}
		
		stopEdit(player);
		player.sendMessage("Discarded Edits");
	}

	@Override
	public void saveEdit(IPlayer player) {
		UUID u = player.getUniqueId();
		if (!suspended.containsKey(u)) {
			return;
		}

		Inventory inv = suspended.get(u).get();
		InventoryTrigger trigger = sessions.get(u);
		int size = inv.capacity();

		IItemStack[] iitems = new IItemStack[size];

		for (Inventory slot : inv.slots()) {
			slot.getInventoryProperty(SlotIndex.class).ifPresent(slotIndex ->
					slot.peek().ifPresent(itemStack ->
							iitems[slotIndex.getValue()] = new SpongeItemStack(itemStack)));
		}

		//TODO this causes an error waaay down the call chain.  replaceItems also saves the inventory trigger manager
		//but while trying to write the new data, NPE is thrown.  Starting a new edit shows that the new data at least
		//gets written to the trigger, but reloading will throw away the edits.
		//None of the items I want to save are null, so that isn't the source of the NPE.
		//I know this because item.createSnapshot() in the sponge InventoryTriggerManager.writeItemsList() succeeds.
		//please investigate this wysohn, because I have no idea at all why it can't save.
		replaceItems(trigger, iitems);
		stopEdit(player);
		player.sendMessage("Saved edits");
	}

	@Listener
	public void onClose(InteractInventoryEvent.Close e) {
		Player p = e.getCause().first(Player.class).orElse(null);
		if (p == null) {
			return;
		}
		UUID u = p.getUniqueId();
		if (!sessions.containsKey(u)) {
			return;
		}
		if (suspended.containsKey(u)) {
			return;
		}

		Inventory inv = e.getTargetInventory();
		GridInventory gridInv = inv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class)).first();
		InventoryTrigger trigger = sessions.get(u);

		int size = gridInv.capacity();
		Inventory newInv = createInventory(size, trigger.getTriggerName());
		GridInventory newGrid = newInv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));

		for (int i = 0; i < size; i++) {
			SlotIndex index = new SlotIndex(i);
			gridInv.getSlot(index).ifPresent(slot -> newGrid.set(index, slot.peek().orElse(ItemStack.of(ItemTypes.AIR))));
		}

		suspended.put(u, new SpongeInventory(newGrid, null));
		p.sendMessage(savePrompt);
	}

	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect e) {
		Player player = e.getTargetEntity();
		stopEdit(new SpongePlayer(player));
	}

	@Override
	public void reload() {
	}

	@Override
	public void saveAll() {
	}
}
