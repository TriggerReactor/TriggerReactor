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

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IPluginProcedure;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@ManagerScope
public class ExecutorManager extends AbstractJavascriptBasedManager implements IPluginProcedure, KeyValueManager<Executor> {
    @Inject
    Logger logger;
    @Inject
    TaskSupervisor task;
    @Inject
    @Named("DataFolder")
    File dataFolder;
    @Inject
    IWrapper wrapper;
    protected Map<String, Executor> jsExecutors = new HashMap<>();
    private File executorFolder;

    @Inject
    ExecutorManager() {
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
     * @see KeyValueManager#get(java.lang.Object)
     */
    @Override
    public Executor get(Object key) {
        return jsExecutors.get(key);
    }

    /* (non-Javadoc)
     * @see KeyValueManager#getExecutorMap()
     */
    @Override
    public Map<String, Executor> getBackedMap() {
        return this.jsExecutors;
    }

    @Override
    public void onEnable() throws Exception {
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, dataFolder, JarUtil.CopyOption.REPLACE_IF_EXIST);
        this.executorFolder = new File(dataFolder, "Executor");

        onReload();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() throws RuntimeException {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        jsExecutors.clear();
        for (File file : executorFolder.listFiles(filter)) {
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                logger.warning("Could not load executor " + file.getName());
                continue;
            }
        }

        //manually add CMDOP after reload is done
        this.jsExecutors.put("CMDOP", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing,
                                   InterpreterLocalContext localContext,
                                   Map<String, Object> variables,
                                   Object... args) throws Exception {
                IPlayer player = Optional.ofNullable(variables.get("player"))
                        .map(wrapper::wrap)
                        .filter(IPlayer.class::isInstance)
                        .map(IPlayer.class::cast)
                        .orElse(null);
                if (player == null)
                    return null;

                DispatchCommandAsOP call = new DispatchCommandAsOP(player, String.valueOf(args[0]));
                if (task.isServerThread()) {
                    call.call();
                } else {
                    try {
                        task.submitSync(call).get();
                    } catch (Exception ex) {
                        //to double check
                        call.deOpIfWasNotOp();
                    }
                }

                return null;
            }

        });
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
                logger.warning(builder.toString() + " already registered! Duplicating executors?");
            } else {
                JSExecutor exec = new JSExecutor(fileName, file);
                jsExecutors.put(builder.toString(), exec);
            }
        }
    }

    public class JSExecutor extends Evaluable<Integer> implements Executor {
        public JSExecutor(String executorName, File file) throws ScriptException, IOException {
            this(executorName, new FileInputStream(file));
        }

        public JSExecutor(String executorName, InputStream file) throws ScriptException, IOException {
            super("#", "Executors", executorName, readSourceCode(file));
        }

        @Override
        public Integer execute(Timings.Timing timing,
                               InterpreterLocalContext localContext,
                               Map<String, Object> variables,
                               Object... args) throws Exception {
            return evaluate(timing, localContext, variables, args);
        }
    }

    private static final Set<String> DEPRECATED_EXECUTORS = new HashSet<String>() {{
        add("MODIFYPLAYER");
    }};

    private static final String JAR_FOLDER_LOCATION = "Executor";

    private static class DispatchCommandAsOP implements Callable<Void> {
        private final IPlayer player;
        private final String cmd;

        private boolean wasOp;

        public DispatchCommandAsOP(IPlayer player, String cmd) {
            super();
            this.player = player;
            this.cmd = cmd;
        }

        @Override
        public Void call() throws Exception {
            wasOp = player.isOp();

            try {
                player.setOp(true);
                player.dispatchCommand(cmd);
            } catch (Exception ignored) {

            } finally {
                deOpIfWasNotOp();
            }
            return null;
        }

        private void deOpIfWasNotOp() {
            if (!wasOp)
                player.setOp(false);
        }

    }
}