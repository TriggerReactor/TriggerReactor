package io.github.wysohn.triggerreactor.core.config;

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;

public interface IMigrationHelper {
    void migrate(IConfigSource current);
}
