/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Objects;

@Singleton
public class PlaceholderManager extends AbstractPlaceholderManager {
    private File placeholderFolder;

    @Inject
    public PlaceholderManager() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, dataFolder, CopyOption.REPLACE_IF_EXIST);

        this.placeholderFolder = new File(dataFolder, "Placeholder");

        onReload();
    }

    @Override
    public void onReload() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        jsPlaceholders.clear();
        File[] folder = placeholderFolder.listFiles(filter);
        ValidationUtil.assertTrue(folder, Objects::nonNull, placeholderFolder + " is not a folder.");

        for (File file : Objects.requireNonNull(folder)) {
            try {
                reloadPlaceholders(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                logger.warning("Could not load placeholder " + file.getName());
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }
    private static final String JAR_FOLDER_LOCATION = "Placeholder";
}
