package io.github.wysohn.triggerreactor.core.manager.config;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.Manager;

import java.io.File;

public class ConfigManager extends Manager {
    private final File file;
    private final IConfigSource configSource;
    private IMigrationHelper migrationHelper;

    public ConfigManager(TriggerReactor plugin, File file) {
        super(plugin);
        this.file = file;
        this.configSource = new GsonConfigSource(file);
    }

    @Override
    public void reload() {
        // do not migrate if target file already exists.
        // we don't want the old data to be written on the existing json file.
        if (migrationHelper != null && !file.exists()) {
            migrationHelper.migrate(configSource);
        }

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

    public void setMigrationHelper(IMigrationHelper migrationHelper) {
        this.migrationHelper = migrationHelper;
    }
}
