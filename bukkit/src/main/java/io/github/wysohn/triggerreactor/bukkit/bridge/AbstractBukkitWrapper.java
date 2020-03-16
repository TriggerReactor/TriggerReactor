package io.github.wysohn.triggerreactor.bukkit.bridge;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitEntity;
import io.github.wysohn.triggerreactor.bukkit.bridge.event.BukkitPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWrapper;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;

public abstract class AbstractBukkitWrapper implements IWrapper{
	public IEntity wrap(Entity entity) {
		return new BukkitEntity(entity);
	}
	
	public abstract IPlayer wrap(Player player);
	
	public IPlayerBlockLocationEvent wrap(PlayerBlockLocationEvent pble) {
		return new BukkitPlayerBlockLocationEvent(pble);
	}
	
	public ICommandSender wrap(CommandSender commandSender) {
		return new BukkitCommandSender(commandSender);
	}
	
	public IInventory wrap(Inventory inventory) {
		return new BukkitInventory(inventory);
	}
	
	public IItemStack wrap(ItemStack itemStack) {
		return new BukkitItemStack(itemStack);
	}
	
	public ILocation wrap(Location location) {
		return new BukkitLocation(location);
	}
}
