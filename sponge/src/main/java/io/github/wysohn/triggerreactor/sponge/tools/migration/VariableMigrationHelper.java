package io.github.wysohn.triggerreactor.sponge.tools.migration;

import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.sponge.tools.ConfigurationUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;

import java.io.File;
import java.util.Map;
import java.util.function.BiConsumer;

public class VariableMigrationHelper implements IMigrationHelper {
    protected final ConfigurationNode oldConfig;
    protected final File oldFile;

    public VariableMigrationHelper(ConfigurationNode oldConfig, File oldFile) {
        this.oldConfig = oldConfig;
        this.oldFile = oldFile;
    }

    /**
     * Variable Manager of Sponge stores data different than how it works in Bukkit. Reading from oldConfig require
     * some modification.
     *
     * @param map
     * @param consumer
     */
    protected void traversal(Map<Object, ? extends ConfigurationNode> map, BiConsumer<String, Object> consumer) {
        DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            ConfigurationNode o = entry.getValue();

            ConfigurationNode keyNode = o.getNode("type");
            ConfigurationNode valueNode = o.getNode("value");

            try {
                if (!keyNode.isVirtual() && !valueNode.isVirtual()) {
                    Class<? extends DataSerializable> clazz =
                            (Class<? extends DataSerializable>) Class.forName(keyNode.getString());
                    Sponge.getDataManager().getBuilder(clazz).ifPresent(dataBuilder -> {
                        DataContainer container = translator.translate(valueNode);
                        consumer.accept(ConfigurationUtil.asDottedPath(o), dataBuilder.build(container).orElse(null));
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (o.hasMapChildren()) {
                    traversal(o.getChildrenMap(), consumer);
                }
            }
        }
    }

    @Override
    public void migrate(IConfigSource current) {
        traversal(oldConfig.getChildrenMap(), current::put);

        if (oldFile.exists())
            oldFile.renameTo(new File(oldFile.getParentFile(), oldFile.getName() + ".bak"));

        current.saveAll();
    }
}
