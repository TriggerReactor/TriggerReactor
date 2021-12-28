package io.github.wysohn.triggerreactor.tools.migration;

import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractInvTriggerMingrationHelper<ItemStack> implements IMigrationHelper {
    protected final File oldFile;

    public AbstractInvTriggerMingrationHelper(File oldFile) {
        this.oldFile = oldFile;
    }

    @Override
    public void migrate(IConfigSource current) {
        getSize().ifPresent(size -> current.put(InventoryTriggerManager.SIZE, size));
        getItems().forEach((index, item) -> current.put(InventoryTriggerManager.ITEMS + "." + index, item));

        if (oldFile.exists())
            oldFile.renameTo(new File(oldFile.getParentFile(), oldFile.getName() + ".bak"));

        current.saveAll();
    }

    protected abstract Optional<Integer> getSize();

    protected abstract Map<Integer, ItemStack> getItems();
}
