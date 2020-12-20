package io.github.wysohn.triggerreactor.core.config;

public interface IMigratable {
    boolean isMigrationNeeded();

    void migrate(IMigrationHelper migrationHelper);
}
