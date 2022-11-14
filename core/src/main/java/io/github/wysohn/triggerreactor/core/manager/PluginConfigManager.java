/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public class PluginConfigManager extends Manager implements IMigratable {
    @Inject
    @Named("PluginConfig")
    private IConfigSource configSource;
    @Inject
    @Named("DataFolder")
    private File dataFolder;

    @Inject
    private PluginConfigManager(){

    }

    @Override
    public void initialize() {

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
    public void shutdown() {
        configSource.disable();
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
        configSource.reload();
    }
}
