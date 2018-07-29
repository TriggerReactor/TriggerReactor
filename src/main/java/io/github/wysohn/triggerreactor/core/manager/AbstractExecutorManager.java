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

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

@SuppressWarnings("serial")
public abstract class AbstractExecutorManager extends AbstractJavascriptBasedManager implements KeyValueManager<Executor> {
    protected Map<String, Executor> jsExecutors = new HashMap<>();

    public AbstractExecutorManager(TriggerReactor plugin) throws ScriptException {
        super(plugin);
    }

    /**
     * Loads all the Executor files and files under the folders. If Executors are inside the folder, the folder
     * name will be added infront of them. For example, an Executor named test is under folder named hi, then
     * its name will be hi:test; therefore, you should #hi:test to call this executor.
     * @param file the target file/folder
     * @param filter the filter for Executors. Usually you check if the file ends withd .js or is a folder.
     * @throws ScriptException
     * @throws IOException
     */
    protected void reloadExecutors(File file, FileFilter filter) throws ScriptException, IOException{
        reloadExecutors(new Stack<String>(), file, filter);
    }

    private void reloadExecutors(Stack<String> name, File file, FileFilter filter) throws ScriptException, IOException{
        if(file.isDirectory()){
            name.push(file.getName());
            for(File f : file.listFiles(filter)){
                reloadExecutors(name, f, filter);
            }
            name.pop();
        }else{
            StringBuilder builder = new StringBuilder();
            for(int i = name.size() - 1; i >= 0; i--){
                builder.append(name.get(i)+":");
            }
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if(jsExecutors.containsKey(builder.toString())){
                plugin.getLogger().warning(builder.toString()+" already registered! Duplicating executors?");
            }else{
                JSExecutor exec = new JSExecutor(fileName, IScriptEngineInitializer.getNashornEngine(sem), file);
                jsExecutors.put(builder.toString(), exec);
            }
        }
    }

    /* (non-Javadoc)
     * @see io.github.wysohn.triggerreactor.core.manager.KeyValueManager#get(java.lang.Object)
     */
    @Override
    public Executor get(Object key) {
        return jsExecutors.get(key);
    }

    /* (non-Javadoc)
     * @see io.github.wysohn.triggerreactor.core.manager.KeyValueManager#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return jsExecutors.containsKey(key);
    }

    /* (non-Javadoc)
     * @see io.github.wysohn.triggerreactor.core.manager.KeyValueManager#entrySet()
     */
    @Override
    public Set<Entry<String, Executor>> entrySet() {
        Set<Entry<String, Executor>> set = new HashSet<>();
        for(Entry<String, Executor> entry : jsExecutors.entrySet()){
            set.add(new AbstractMap.SimpleEntry<String, Executor>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    /* (non-Javadoc)
     * @see io.github.wysohn.triggerreactor.core.manager.KeyValueManager#getExecutorMap()
     */
    @Override
    public Map<String, Executor> getBackedMap() {
        return this.jsExecutors;
    }

    public static class JSExecutor extends Executor{
        private final String executorName;
        private final String sourceCode;

        private ScriptEngine engine = null;
        private CompiledScript compiled = null;

        public JSExecutor(String executorName, ScriptEngine engine, File file) throws ScriptException, IOException {
            this.executorName = executorName;
            this.engine = engine;

            StringBuilder builder = new StringBuilder();
            FileReader reader = new FileReader(file);
            int read = -1;
            while((read = reader.read()) != -1)
                builder.append((char) read);
            reader.close();
            sourceCode = builder.toString();

            Compilable compiler = (Compilable) engine;
            compiled = compiler.compile(sourceCode);
        }

        @Override
        public Integer execute(boolean sync, Object e, Object... args) throws Exception {
            ///////////////////////////////
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> vars = ReflectionUtil.extractVariables(e);
            variables.putAll(vars);

            instance.extractCustomVariables(variables, e);
            ///////////////////////////////

            ScriptContext scriptContext = engine.getContext();
            final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            for(Map.Entry<String, Object> entry : variables.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                bindings.put(key, value);
            }

            try {
                compiled.eval(scriptContext);
            } catch (ScriptException e2) {
                e2.printStackTrace();
            }

            Invocable invocable = (Invocable) compiled.getEngine();
            Callable<Integer> call = new Callable<Integer>(){
                @Override
                public Integer call() throws Exception {
                    Object argObj = args;

                    if(TriggerReactor.getInstance().isDebugging()){
                        Integer result = null;
                        long start = System.currentTimeMillis();
                        result = (Integer) invocable.invokeFunction(executorName, argObj);
                        long end = System.currentTimeMillis();
                        TriggerReactor.getInstance().getLogger().info(executorName+" execution -- "+(end - start)+"ms");
                        return result;
                    }else{
                        return (Integer) invocable.invokeFunction(executorName, argObj);
                    }
                }
            };

            if(TriggerReactor.getInstance().isServerThread()){
                Integer result = null;
                try {
                    result = call.call();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new Exception("#"+executorName+" encountered error.", e1);
                }
                return result;
            }else{
                Future<Integer> future = runSyncTaskForFuture(call);

                Integer result = null;
                try {
                    result = future.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e1) {
                    throw new Exception("#"+executorName+" encountered error.", e1);
                } catch (TimeoutException e1) {
                    throw new Exception("#"+executorName+" was stopped. It took longer than 5 seconds to process. Is the server lagging?", e1);
                }
                return result;
            }
        }
    }

}