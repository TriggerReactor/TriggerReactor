package io.github.wysohn.triggerreactor.bukkit.tools.migration;

import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.tools.migration.AbstractInvTriggerMingrationHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InvTriggerMigrationHelper extends AbstractInvTriggerMingrationHelper<ItemStack> {
    protected final FileConfiguration oldConfig;

    public InvTriggerMigrationHelper(File oldFile, FileConfiguration oldConfig) {
        super(oldFile);
        this.oldConfig = oldConfig;
    }

    @Override
    protected Map<Integer, ItemStack> getItems() {
        Map<Integer, ItemStack> out = new LinkedHashMap<>();
        Optional.of(oldConfig)
                .map(config -> config.getConfigurationSection(InventoryTriggerManager.ITEMS))
                .ifPresent(section -> {
                    for (String key : section.getKeys(false)) {
                        try {
                            int index = Integer.parseInt(key);
                            ItemStack itemStack = section.getItemStack(key);
                            out.put(index, itemStack);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            // continue if something went wrong
                        }
                    }
                });
        return out;
    }

    @Override
    protected Optional<Integer> getSize() {
        return Optional.of(oldConfig)
                .map(config -> config.getInt(InventoryTriggerManager.SIZE, -1))
                .filter(val -> val > 0);
    }
}
