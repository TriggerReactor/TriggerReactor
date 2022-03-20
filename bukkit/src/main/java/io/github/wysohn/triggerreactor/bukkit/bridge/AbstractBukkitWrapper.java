package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitEntity;
import io.github.wysohn.triggerreactor.bukkit.bridge.event.BukkitPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.bridge.*;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractBukkitWrapper implements IWrapper {
    @Override
    public <T extends IMinecraftObject> T wrap(Object object) {
        if (object instanceof Player) {
            return (T) wrap((Player) object);
        } else if (object instanceof Entity) {
            return (T) wrap((Entity) object);
        } else if (object instanceof PlayerBlockLocationEvent) {
            return (T) wrap((PlayerBlockLocationEvent) object);
        } else if (object instanceof CommandSender) {
            return (T) wrap((CommandSender) object);
        } else if (object instanceof Inventory) {
            return (T) wrap((Inventory) object);
        } else if (object instanceof ItemStack) {
            return (T) wrap((ItemStack) object);
        } else if (object instanceof Location) {
            return (T) wrap((Location) object);
        } else {
            throw new RuntimeException("Unsupported object " + object);
        }
    }

    public abstract IPlayer wrap(Player player);

    public IEntity wrap(Entity entity) {
        return new BukkitEntity(entity);
    }

    public IPlayerBlockLocationEvent wrap(PlayerBlockLocationEvent pble) {
        return new BukkitPlayerBlockLocationEvent(this, pble);
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
