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
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class PlaceholderManager
        extends AbstractJavascriptBasedManager<Placeholder> {

    public PlaceholderManager(TriggerReactorCore plugin, ScriptEngineManager sem) throws IOException {
        this(plugin, sem, new HashMap<>());
    }

    public PlaceholderManager(TriggerReactorCore plugin,
                              ScriptEngineManager sem,
                              Map<String, Placeholder> overrides) throws IOException {
        super(plugin, sem, overrides, new File(plugin.getDataFolder(), "Placeholder"));

        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, plugin.getDataFolder(), JarUtil.CopyOption.REPLACE_IF_EXIST);

        //reload();
    }

    @Override
    public void reload() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        evaluables.clear();
        for (File file : Objects.requireNonNull(folder.listFiles(filter))) {
            try {
                reloadPlaceholders(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load placeholder " + file.getName());
            }
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

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
                plugin.getLogger().warning(builder.toString() + " already registered! Duplicating placeholders?");
            } else {
                JSPlaceholder placeholder = new JSPlaceholder(fileName, getEngine(sem), file);
                evaluables.put(builder.toString(), placeholder);
            }
        }
    }

    private static final String JAR_FOLDER_LOCATION = "Placeholder";

    public static class JSPlaceholder extends Evaluable<Object> implements Placeholder {
        public JSPlaceholder(String placeholderName, ScriptEngine engine, File file) throws ScriptException,
                IOException {
            this(placeholderName, engine, new FileInputStream(file));
        }

        public JSPlaceholder(String placeholderName, ScriptEngine engine, InputStream file) throws ScriptException,
                IOException {
            super("$", "Placeholders", placeholderName, FileUtil.readFromStream(file), engine);
        }
    }
}
