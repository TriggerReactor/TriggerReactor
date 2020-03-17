package io.github.wysohn.triggerreactor.core.manager.config;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.Manager;

import java.io.File;

public class ConfigManager extends Manager implements IMigratable {
    private final File file;
    private final IConfigSource configSource;

    public ConfigManager(TriggerReactor plugin, File file) {
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
        // if .json file already exist, it's already up to date.
        return !file.exists();
    }

    @Override
    public void migrate(IMigrationHelper migrationHelper) {
        migrationHelper.migrate(configSource);
    }
}
