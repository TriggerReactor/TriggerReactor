package io.github.wysohn.triggerreactor.core.manager.config;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.Manager;

import java.io.File;

public class ConfigManager extends Manager implements IMigratable {
    private final File file;
    private final IConfigSource configSource;

    public ConfigManager(TriggerReactorCore plugin, File file) {
        super(plugin);
        this.file = file;
        this.configSource = new GsonConfigSource(file);
    }

    @Override
    public void reload() {
        configSource.reload();
    }

    @Override
    public void saveAll() {
        configSource.saveAll();
    }

    @Override
    public void disable() {
        configSource.disable();
    }

    @Override
    public boolean isMigrationNeeded() {
        // .json need migration if it didn't exist or its length is 0 (empty).
        return !file.exists() || file.length() == 0L;
    }

    @Override
    public void migrate(IMigrationHelper migrationHelper) {
        migrationHelper.migrate(configSource);
    }
}
