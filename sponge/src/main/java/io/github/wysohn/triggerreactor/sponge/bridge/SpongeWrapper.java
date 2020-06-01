package io.github.wysohn.triggerreactor.sponge.bridge;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongeEntity;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.bridge.event.SpongePlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.sponge.manager.event.PlayerBlockLocationEvent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SpongeWrapper {
	public IEntity wrap(Entity entity) {
		return new SpongeEntity(entity);
	}

	public IPlayer wrap(Player player) {
		return new SpongePlayer(player);
	}

	public IPlayerBlockLocationEvent wrap(PlayerBlockLocationEvent event) {
		return new SpongePlayerBlockLocationEvent(event);
	}

	public ICommandSender wrap(CommandSource commandSender) {
		return new SpongeCommandSender(commandSender);
	}

	public IInventory wrap(Inventory inventory) {
		return new SpongeInventory(inventory, null);
	}

	public IInventory wrap(Inventory inventory, Carrier carrier) {
		return new SpongeInventory(inventory, carrier);
	}

	public IItemStack wrap(ItemStack itemStack) {
		return new SpongeItemStack(itemStack);
	}

	public ILocation wrap(Location<World> location) {
		return new SpongeLocation(location);
	}
}
