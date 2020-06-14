package io.github.wysohn.triggerreactor.sponge.tools;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;

public class SpongeMigrationHelperVar extends SpongeMigrationHelper {
    public SpongeMigrationHelperVar(ConfigurationNode oldConfig, File oldFile) {
        super(oldConfig, oldFile);
    }

    @Override
    public void migrate(IConfigSource current) {
        traversal(oldConfig.getChildrenMap(), (key, node) -> {
            ConfigurationNode keyNode = node.getNode("type");
            ConfigurationNode valueNode = node.getNode("value");
            if (keyNode.isVirtual() || valueNode.isVirtual())
                return;

            current.put(key, valueNode.getValue());
        });

        if (oldFile.exists())
            oldFile.renameTo(new File(oldFile.getParentFile(), oldFile.getName() + ".bak"));

        current.saveAll();
    }
}
