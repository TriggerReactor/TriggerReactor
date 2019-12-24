package io.github.wysohn.triggerreactor.core.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;

public abstract class AbstractInventoryEditManager extends Manager {
	protected static final char X = '\u2718';
	protected static final char CHECK = '\u2713';
	protected static final char PENCIL = '\u270E';

	//map of player uuids to inventories representing the inventories currently being edited
	protected Map<UUID, InventoryTrigger> sessions = new HashMap<>();
	
	//inventories currently awaiting a save/discard/continue command
	protected Map<UUID, IInventory> suspended = new HashMap<>();
	
	public AbstractInventoryEditManager(TriggerReactor plugin) {
		super(plugin);
	}
	
	public abstract void startEdit(IPlayer player, InventoryTrigger trigger);
	
	//called by the "continue edit" button
	public abstract void continueEdit(IPlayer player);
	
	//called by the "don't save" button
	public abstract void discardEdit(IPlayer player);
	
	//called by the "save" button
	public abstract void saveEdit(IPlayer player);
	
	//helper method to remove the player from both maps
	protected void stopEdit(IPlayer player) {
		UUID u = player.getUniqueId();
		sessions.remove(u);
		suspended.remove(u);
	}
	
	//helper method to replace all the items in an inventory trigger
	protected void replaceItems(InventoryTrigger trigger, IItemStack[] items) {
		IItemStack[] triggerItems = trigger.getItems();
		for (int i = 0; i < triggerItems.length; i++) {
			triggerItems[i] = items[i];
		}
		TriggerReactor.getInstance().saveAsynchronously(TriggerReactor.getInstance().getInvManager());
	}
}
