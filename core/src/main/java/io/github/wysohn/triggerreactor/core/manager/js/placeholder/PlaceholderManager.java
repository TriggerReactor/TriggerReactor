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
package io.github.wysohn.triggerreactor.core.manager.js.placeholder;

import io.github.wysohn.triggerreactor.core.manager.IJavascriptFileLoader;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.evaluable.JSPlaceholder;
import io.github.wysohn.triggerreactor.core.manager.js.AbstractJavascriptBasedManager;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

@Singleton
public class PlaceholderManager
        extends AbstractJavascriptBasedManager<Placeholder> {

    @Inject
    private Logger logger;
    @Inject
    private IJSFolderContentCopyHelper copyHelper;
    @Inject
    private IJavascriptFileLoader fileLoader;
    @Inject
    private IJSPlaceholderFactory factory;
    @Inject
    private ScriptEngineProvider engineProvider;

    @Inject
    private PlaceholderManager(@Named("DataFolder") File dataFolder,
                               Map<String, Placeholder> overrides) throws IOException {
        super(overrides, new File(dataFolder, "Placeholder"));
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
                reloadPlaceholders(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                logger.warning("Could not load placeholder " + file.getName());
            }
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Placeholder get(Object key) {
        return evaluables.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return evaluables.containsKey(key);
    }

    @Override
    public Set<Entry<String, Placeholder>> entrySet() {
        Set<Entry<String, Placeholder>> set = new HashSet<>();
        for (Entry<String, Placeholder> entry : evaluables.entrySet()) {
            set.add(new AbstractMap.SimpleEntry<String, Placeholder>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    @Override
    public Map<String, Placeholder> getBackedMap() {
        return evaluables;
    }

    protected void reloadPlaceholders(File file, FileFilter filter) throws ScriptException, IOException {
        reloadPlaceholders(new Stack<>(), file, filter);
    }

    private void reloadPlaceholders(Stack<String> name, File file, FileFilter filter) throws ScriptException,
            IOException {
        if (file.isDirectory()) {
            name.push(file.getName());
            for (File f : Objects.requireNonNull(file.listFiles(filter))) {
                reloadPlaceholders(name, f, filter);
            }
            name.pop();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = name.size() - 1; i >= 0; i--) {
                builder.append(name.get(i)).append("@");
            }

            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if (evaluables.containsKey(builder.toString())) {
                logger.warning(builder.toString() + " already registered! Duplicating placeholders?");
            } else {
                JSPlaceholder placeholder = factory.create(fileName, engineProvider.getEngine(), new FileInputStream(file));
                evaluables.put(builder.toString(), placeholder);
            }
        }
    }

    private static final String JAR_FOLDER_LOCATION = "Placeholder";

}
