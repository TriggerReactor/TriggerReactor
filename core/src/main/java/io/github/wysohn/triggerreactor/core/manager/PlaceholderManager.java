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
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class PlaceholderManager extends AbstractJavascriptBasedManager implements KeyValueManager<Placeholder> {
    @Inject
    IResourceProvider resourceProvider;
    @Inject
    Logger logger;
    @Inject
    @Named("DataFolder")
    File dataFolder;

    protected Map<String, Placeholder> jsPlaceholders = new HashMap<>();
    private File placeholderFolder;

    @Inject
    PlaceholderManager() {
    }

    @Override
    public void onEnable() throws Exception {
        resourceProvider.copyFolderFromJar(JAR_FOLDER_LOCATION, dataFolder, JarUtil.CopyOption.REPLACE_IF_EXIST);
        this.placeholderFolder = new File(dataFolder, "Placeholder");

        onReload();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() throws RuntimeException {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        jsPlaceholders.clear();

        if(!placeholderFolder.exists())
            placeholderFolder.mkdirs();

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
    public boolean containsKey(Object key) {
        return jsPlaceholders.containsKey(key);
    }

    @Override
    public Set<Entry<String, Placeholder>> entrySet() {
        Set<Entry<String, Placeholder>> set = new HashSet<>();
        for (Entry<String, Placeholder> entry : jsPlaceholders.entrySet()) {
            set.add(new AbstractMap.SimpleEntry<String, Placeholder>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    @Override
    public Placeholder get(Object key) {
        return jsPlaceholders.get(key);
    }

    @Override
    public Map<String, Placeholder> getBackedMap() {
        return jsPlaceholders;
    }

    protected void reloadPlaceholders(File file, FileFilter filter) throws ScriptException, IOException {
        reloadPlaceholders(new Stack<>(), file, filter);
    }

    private void reloadPlaceholders(Stack<String> name, File file, FileFilter filter) throws ScriptException,
            IOException {
        if (file.isDirectory()) {
            name.push(file.getName());
            for (File f : file.listFiles(filter)) {
                reloadPlaceholders(name, f, filter);
            }
            name.pop();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = name.size() - 1; i >= 0; i--) {
                builder.append(name.get(i) + "@");
            }

            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if (jsPlaceholders.containsKey(builder.toString())) {
                logger.warning(builder.toString() + " already registered! Duplicating placeholders?");
            } else {
                JSPlaceholder placeholder = new JSPlaceholder(fileName, file);
                jsPlaceholders.put(builder.toString(), placeholder);
            }
        }
    }

    public class JSPlaceholder extends Evaluable<Object> implements Placeholder {
        public JSPlaceholder(String placeholderName, File file) throws ScriptException, IOException {
            this(placeholderName, new FileInputStream(file));
        }

        public JSPlaceholder(String placeholderName, InputStream file) throws ScriptException, IOException {
            super("$", "Placeholders", placeholderName, readSourceCode(file));
        }

        @Override
        public Object parse(Timings.Timing timing,
                            InterpreterLocalContext localContext,
                            Map<String, Object> variables,
                            Object... args) throws Exception {
            return evaluate(timing, localContext, variables, args);
        }
    }

    private static final String JAR_FOLDER_LOCATION = "Placeholder";
}
