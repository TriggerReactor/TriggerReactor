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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public final class ExecutorManager extends AbstractJavascriptBasedManager implements KeyValueManager<Executor> {
    private static final String JAR_FOLDER_LOCATION = "Executor";

    protected Map<String, Executor> jsExecutors = new HashMap<>();

    private final Map<String, Executor> overrides;
    private final File executorFolder;

    public ExecutorManager(TriggerReactorCore plugin,
                           ScriptEngineManager sem) throws IOException {
        this(plugin, sem, new HashMap<>());
    }

    public ExecutorManager(TriggerReactorCore plugin,
                           ScriptEngineManager sem,
                           Map<String, Executor> overrides) throws IOException {
        super(plugin, sem);
        this.executorFolder = new File(plugin.getDataFolder(), "Executor");
        this.overrides = overrides;

        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, plugin.getDataFolder(), JarUtil.CopyOption.REPLACE_IF_EXIST);

        //reload();
    }

    @Override
    public void reload() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        jsExecutors.clear();
        for (File file : executorFolder.listFiles(filter)) {
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor " + file.getName());
            }
        }

        jsExecutors.putAll(overrides);
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
    protected void reloadExecutors(File file, FileFilter filter) throws ScriptException, IOException {
        reloadExecutors(new Stack<String>(), file, filter);
    }

    private void reloadExecutors(Stack<String> name, File file, FileFilter filter) throws ScriptException, IOException {
        if (file.isDirectory()) {
            name.push(file.getName());
            for (File f : file.listFiles(filter)) {
                reloadExecutors(name, f, filter);
            }
            name.pop();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = name.size() - 1; i >= 0; i--) {
                builder.append(name.get(i) + ":");
            }
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if (jsExecutors.containsKey(builder.toString())) {
                plugin.getLogger().warning(builder.toString() + " already registered! Duplicating executors?");
            } else {
                JSExecutor exec = new JSExecutor(fileName, getEngine(sem), file);
                jsExecutors.put(builder.toString(), exec);
            }
        }
    }

    /* (non-Javadoc)
     * @see KeyValueManager#get(java.lang.Object)
     */
    @Override
    public Executor get(Object key) {
        return jsExecutors.get(key);
    }

    /* (non-Javadoc)
     * @see KeyValueManager#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return jsExecutors.containsKey(key);
    }

    /* (non-Javadoc)
     * @see KeyValueManager#entrySet()
     */
    @Override
    public Set<Entry<String, Executor>> entrySet() {
        Set<Entry<String, Executor>> set = new HashSet<>();
        for (Entry<String, Executor> entry : jsExecutors.entrySet()) {
            set.add(new AbstractMap.SimpleEntry<String, Executor>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    /* (non-Javadoc)
     * @see KeyValueManager#getExecutorMap()
     */
    @Override
    public Map<String, Executor> getBackedMap() {
        return this.jsExecutors;
    }

    public static class JSExecutor extends Evaluable<Integer> implements Executor {
        public JSExecutor(String executorName, ScriptEngine engine, File file) throws ScriptException, IOException {
            this(executorName, engine, new FileInputStream(file));
        }

        public JSExecutor(String executorName, ScriptEngine engine, InputStream file) throws ScriptException, IOException {
            super("#", "Executors", executorName, readSourceCode(file), engine);
        }

        @Override
        public Integer execute(Timings.Timing timing,
                               Map<String, Object> variables,
                               Object event,
                               Object... args) throws Exception {
            return evaluate(timing, variables, event, args);
        }
    }

    private static final Set<String> DEPRECATED_EXECUTORS = new HashSet<>();

    static {
        DEPRECATED_EXECUTORS.add("MODIFYPLAYER");
    }
}