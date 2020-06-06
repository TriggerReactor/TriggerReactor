package io.github.wysohn.triggerreactor.sponge.tools;

import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.IMigrationHelper;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpongeMigrationHelper implements IMigrationHelper {
    private final ConfigurationNode oldConfig;
    private final File oldFile;

    public SpongeMigrationHelper(ConfigurationNode oldConfig, File oldFile) {
        this.oldConfig = oldConfig;
        this.oldFile = oldFile;
    }

    private void traversal(Map<Object, ? extends ConfigurationNode> map, BiConsumer<String, ConfigurationNode> consumer) {
        map.forEach(((s, o) -> {
            if (o.hasMapChildren()) {
                traversal(o.getChildrenMap(), consumer);
            } else {
                consumer.accept(ConfigurationUtil.asDottedPath(o), o);
            }
        }));
    }

    @Override
    public void migrate(IConfigSource current) {
        traversal(oldConfig.getChildrenMap(), (key, node) -> current.put(key, node.getValue()));

        if (oldFile.exists())
            oldFile.renameTo(new File(oldFile.getParentFile(), oldFile.getName() + ".bak"));

        current.saveAll();
    }
}
