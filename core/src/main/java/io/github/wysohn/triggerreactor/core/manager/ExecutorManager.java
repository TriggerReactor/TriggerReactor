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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class ExecutorManager extends AbstractJavascriptBasedManager<Executor> {
    public ExecutorManager(TriggerReactorCore plugin,
                           ScriptEngineManager sem) throws IOException {
        this(plugin, sem, new HashMap<>());
    }

    public ExecutorManager(TriggerReactorCore plugin,
                           ScriptEngineManager sem,
                           Map<String, Executor> overrides) throws IOException {
        super(plugin, sem, overrides, new File(plugin.getDataFolder(), "Executor"));

        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, plugin.getDataFolder(), JarUtil.CopyOption.REPLACE_IF_EXIST);

        //reload();
    }

    @Override
    public void reload() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        evaluables.clear();
        for (File file : Objects.requireNonNull(folder.listFiles(filter))) {
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor " + file.getName());
            }
        }

        evaluables.putAll(overrides);
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    /**
     * Loads all the Executor files and files under the folders. If Executors are inside the folder, the folder
     * name will be added infront of them. For example, an Executor named test is under folder named hi, then
     * its name will be hi:test; therefore, you should #hi:test to call this executor.
     *
     * @param file   the target file/folder
     * @param filter the filter for Executors. Usually you check if the file ends withd .js or is a folder.
     * @throws ScriptException
     * @throws IOException
     */
    private void reloadExecutors(File file, FileFilter filter) throws ScriptException, IOException {
        reloadExecutors(new Stack<>(), file, filter);
    }

    private void reloadExecutors(Stack<String> name, File file, FileFilter filter) throws ScriptException, IOException {
        if (file.isDirectory()) {
            name.push(file.getName());
            for (File f : Objects.requireNonNull(file.listFiles(filter))) {
                reloadExecutors(name, f, filter);
            }
            name.pop();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = name.size() - 1; i >= 0; i--) {
                builder.append(name.get(i)).append(":");
            }
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if (evaluables.containsKey(builder.toString())) {
                plugin.getLogger().warning(builder.toString() + " already registered! Duplicating executors?");
            } else {
                JSExecutor exec = new JSExecutor(fileName, getEngine(sem), file);
                evaluables.put(builder.toString(), exec);
            }
        }
    }

    private static final String JAR_FOLDER_LOCATION = "Executor";
    private static final Set<String> DEPRECATED_EXECUTORS = new HashSet<>();

    static {
        DEPRECATED_EXECUTORS.add("MODIFYPLAYER");
    }
}