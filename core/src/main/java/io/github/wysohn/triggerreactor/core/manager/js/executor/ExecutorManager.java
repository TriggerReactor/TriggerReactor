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
package io.github.wysohn.triggerreactor.core.manager.js.executor;

import io.github.wysohn.triggerreactor.core.manager.IJavascriptFileLoader;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.evaluable.JSExecutor;
import io.github.wysohn.triggerreactor.core.manager.js.AbstractJavascriptBasedManager;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Singleton
public class ExecutorManager extends AbstractJavascriptBasedManager<Executor> {
    @Inject
    private Logger logger;
    @Inject
    private IJSFolderContentCopyHelper copyHelper;
    @Inject
    private IJavascriptFileLoader fileLoader;
    @Inject
    private IJSExecutorFactory factory;
    @Inject
    private ScriptEngineProvider engineProvider;

    @Inject
    private ExecutorManager(@Named("DataFolder") File dataFolder,
                            Map<String, Executor> overrides) throws IOException {
        super(overrides, new File(dataFolder, "Executor"));
    }

    @Override
    public void initialize() {
        try {
            copyHelper.copyFolderFromJar(JAR_FOLDER_LOCATION, folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        evaluables.clear();
        for (File file : fileLoader.listFiles(folder, filter)) {
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                logger.warning("Could not load executor " + file.getName());
            }
        }

        evaluables.putAll(overrides);
    }

    @Override
    public void shutdown() {

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
                logger.warning(builder.toString() + " already registered! Duplicating executors?");
            } else {
                JSExecutor exec = factory.create(fileName, engineProvider.getEngine(), new FileInputStream(file));
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