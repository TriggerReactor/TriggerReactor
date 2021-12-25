package io.github.wysohn.triggerreactor.sponge.tools.migration;

import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.sponge.tools.ConfigurationUtil;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.Map;
import java.util.function.BiConsumer;

public class NaiveMigrationHelper implements IMigrationHelper {
    protected final ConfigurationNode oldConfig;
    protected final File oldFile;

    public NaiveMigrationHelper(ConfigurationNode oldConfig, File oldFile) {
        this.oldConfig = oldConfig;
        this.oldFile = oldFile;
    }

    @Override
    public void migrate(IConfigSource current) {
        traversal(oldConfig.getChildrenMap(), current::put);

        if (oldFile.exists()) oldFile.renameTo(new File(oldFile.getParentFile(), oldFile.getName() + ".bak"));

        current.saveAll();
    }

    protected void traversal(Map<Object, ? extends ConfigurationNode> map, BiConsumer<String, Object> consumer) {
        map.forEach(((s, o) -> {
            if (o.hasMapChildren()) {
                traversal(o.getChildrenMap(), consumer);
            } else {
                consumer.accept(ConfigurationUtil.asDottedPath(o), o.getValue());
            }
        }));
    }
}
