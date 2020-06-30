package io.github.wysohn.triggerreactor.core.manager.config;

public interface IMigratable {
    boolean isMigrationNeeded();

    void migrate(IMigrationHelper migrationHelper);
}
