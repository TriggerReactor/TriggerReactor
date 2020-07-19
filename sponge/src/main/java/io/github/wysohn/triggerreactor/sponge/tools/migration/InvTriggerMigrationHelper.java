package io.github.wysohn.triggerreactor.sponge.tools.migration;

import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.tools.migration.AbstractInvTriggerMingrationHelper;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InvTriggerMigrationHelper extends AbstractInvTriggerMingrationHelper<ItemStack> {
    protected final ConfigurationNode oldConfig;

    public InvTriggerMigrationHelper(File oldFile, ConfigurationNode oldConfig) {
        super(oldFile);
        this.oldConfig = oldConfig;
    }

    @Override
    protected Optional<Integer> getSize() {
        return Optional.of(oldConfig)
                .map(config -> config.getNode(AbstractInventoryTriggerManager.SIZE))
                .map(ConfigurationNode::getInt);
    }

    @Override
    protected Map<Integer, ItemStack> getItems() {
        final DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;

        return Optional.of(oldConfig)
                .map(config -> config.getNode(AbstractInventoryTriggerManager.ITEMS))
                .map(ConfigurationNode::getChildrenMap)
                .map(map -> {
                    Map<Integer, ItemStack> out = new LinkedHashMap<>();
                    for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
                        int index = Integer.parseInt((String) entry.getKey());
                        try {
                            DataContainer container = translator.translate(entry.getValue());
                            Sponge.getDataManager().deserialize(ItemStack.class, container).ifPresent(itemStack ->
                                    out.put(index, itemStack));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            //show exception and continue
                        }
                    }
                    return out;
                }).orElseGet(LinkedHashMap::new);
    }
}
