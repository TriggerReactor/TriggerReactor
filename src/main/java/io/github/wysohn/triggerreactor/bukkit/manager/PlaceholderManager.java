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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;

public class PlaceholderManager extends AbstractPlaceholderManager implements BukkitScriptEngineInitializer{
    private static final String JAR_FOLDER_LOCATION = "assets"+JarUtil.JAR_SEPARATOR+
            "triggerreactor"+JarUtil.JAR_SEPARATOR+"Placeholder"+JarUtil.JAR_SEPARATOR+"Bukkit";

    private File placeholderFolder;

    public PlaceholderManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        this.placeholderFolder = new File(plugin.getDataFolder(), JAR_FOLDER_LOCATION);
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, plugin.getDataFolder(), CopyOption.REPLACE_IF_EXIST);

        reload();
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".js");
            }
        };

        jsPlaceholders.clear();
        for(File file : placeholderFolder.listFiles(filter)){
            try {
                reloadPlaceholders(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load placeholder "+file.getName());
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        super.initScriptEngine(sem);
        BukkitScriptEngineInitializer.super.initScriptEngine(sem);
    }

}
