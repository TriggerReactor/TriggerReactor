package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@PluginScope
public class PluginConfigManager extends Manager implements IMigratable, IConfigSource {
    @Inject
    ConfigSourceFactories configSourceFactories;
    @Inject
    Logger logger;
    @Inject
    @Named("DataFolder")
    File dataFolder;

    private IConfigSource configSource;

    @Inject
    public PluginConfigManager() {

    }

    @Override
    public void onEnable() throws Exception{
        configSource = configSourceFactories.create(dataFolder, "config");
        configSource.onEnable();
    }

    @Override
    public void onReload() {
        configSource.onReload();
    }

    @Override
    public void saveAll() {
        configSource.saveAll();
    }

    @Override
    public void delete() {
        throw new RuntimeException("Not supported.");
    }

    @Override
    public void onDisable() {
        configSource.onDisable();
    }

    @Override
    public boolean isMigrationNeeded() {
        File oldFile = new File(dataFolder, "config.yml");
        // after migration, file will be renamed to .yml.bak, and .json file will be created.
        // otherwise, do not migrate.
        return oldFile.exists() && !configSource.fileExists();
    }

    @Override
    public void migrate(IMigrationHelper migrationHelper) {
        migrationHelper.migrate(configSource);
        configSource.onReload();
    }

    @Override
    public boolean fileExists() {
        return configSource.fileExists();
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> asType) {
        return configSource.get(key, asType);
    }

    @Override
    public <T> Optional<T> get(String key) {
        return configSource.get(key);
    }

    @Override
    public void put(String key, Object value) {
        configSource.put(key, value);
    }

    @Override
    public boolean has(String key) {
        return configSource.has(key);
    }

    @Override
    public Set<String> keys() {
        return configSource.keys();
    }

    @Override
    public boolean isSection(String key) {
        return configSource.isSection(key);
    }
}
