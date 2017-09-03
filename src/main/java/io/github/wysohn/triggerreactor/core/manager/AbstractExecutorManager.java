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
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

@SuppressWarnings("serial")
public abstract class AbstractExecutorManager extends Manager {
    protected static final ScriptEngineManager sem = new ScriptEngineManager();

    public static AbstractExecutorManager instance;

    protected Map<String, Executor> jsExecutors = new HashMap<>();

    public AbstractExecutorManager(TriggerReactor plugin) throws ScriptException {
        super(plugin);
        instance = this;

        initScriptEngine();
    }

    /**
     * Initializes pre-defined functions and variables for Executors.
     * @throws ScriptException
     */
    protected abstract void initScriptEngine() throws ScriptException;

    protected void registerClass(Class<?> clazz) throws ScriptException{
        registerClass(clazz.getSimpleName(), clazz);
    }

    protected void registerClass(String name, Class<?> clazz) throws ScriptException{
        sem.put(name, getNashornEngine().eval("Java.type('"+clazz.getName()+"');"));
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
                JSExecutor exec = new JSExecutor(fileName, file);
                jsExecutors.put(builder.toString(), exec);
            }
        }
    }

    public Executor get(Object key) {
        return jsExecutors.get(key);
    }

    public boolean containsKey(Object key) {
        return jsExecutors.containsKey(key);
    }

    public Set<Entry<String, Executor>> entrySet() {
        Set<Entry<String, Executor>> set = new HashSet<>();
        for(Entry<String, Executor> entry : jsExecutors.entrySet()){
            set.add(new AbstractMap.SimpleEntry<String, Executor>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    public Map<String, Executor> getExecutorMap() {
        return this.jsExecutors;
    }

    protected static ScriptEngine getNashornEngine() {
        return sem.getEngineByName("nashorn");
    }

    /**
     * Extract and put necessary variables needed for the Executors to work properly. For Bukkit API for example,
     * you will have to extract 'player' variable manually for inventory events as Player instance is not saved in
     * the field of Inventory evnet classes.
     * @param variables the local variable map.
     * @param e the context.
     */
    protected abstract void extractCustomVariables(Map<String, Object> variables, Object e);

    public static class JSExecutor extends Executor{
        private final String executorName;
        private final String sourceCode;

        private ScriptEngine engine = getNashornEngine();
        private CompiledScript compiled = null;

        public JSExecutor(String executorName, File file) throws ScriptException, IOException {
            this.executorName = executorName;

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
        public synchronized Integer execute(boolean sync, Object e, Object... args) throws Exception {
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

            if(sync){
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