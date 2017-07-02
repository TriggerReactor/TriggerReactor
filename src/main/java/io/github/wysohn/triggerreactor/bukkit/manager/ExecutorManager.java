/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

@SuppressWarnings("serial")
public class ExecutorManager extends AbstractExecutorManager{
    private static final ScriptEngineManager sem = new ScriptEngineManager();

    public static final String Invocable = null;

    private TriggerReactor plugin;
    private File executorFolder;
    private Map<String, Executor> jsExecutors = new HashMap<>();

    public ExecutorManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        this.plugin = plugin;
        this.executorFolder = new File(plugin.getDataFolder(), "Executor");
        JarUtil.copyFolderFromJar("Executor", plugin.getDataFolder(), CopyOption.COPY_IF_NOT_EXIST);

        initScriptEngine();

        reload();

    }

    private void initScriptEngine() throws ScriptException {
        registerClass(Executor.class);
        registerClass(Bukkit.class);
        registerClass(Location.class);
        registerClass(ChatColor.class);

        sem.put("plugin", this.plugin);

        sem.put("get", new Function<String, Object>(){
            @Override
            public Object apply(String t) {
                return plugin.getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>(){
            @Override
            public Void apply(String a, Object b) {
                if(!VariableManager.isValidName(a))
                    throw new RuntimeException("["+a+"] cannot be used as key");

                if(a != null && b != null){
                    if(!(b instanceof String) && !(b instanceof Number) && !(b instanceof Boolean)
                            && !(b instanceof ConfigurationSerializable))
                        throw new RuntimeException("["+b.getClass().getSimpleName()+"] is not a valid type to be saved.");

                    plugin.getVariableManager().put(a, b);
                }else if(a != null && b == null){
                    plugin.getVariableManager().remove(a);
                }

                return null;
            }
        });

        sem.put("has", new Function<String, Boolean>(){
            @Override
            public Boolean apply(String t) {
                return plugin.getVariableManager().has(t);
            }
        });

        sem.put("Char", new Function<String, Character>(){
            @Override
            public Character apply(String t) {
                return t.charAt(0);
            }
        });
    }

    private ScriptEngine getNashornEngine() {
        return sem.getEngineByName("nashorn");
    }

    private void registerClass(Class<?> clazz) throws ScriptException{
        registerClass(clazz.getSimpleName(), clazz);
    }

    private void registerClass(String name, Class<?> clazz) throws ScriptException{
        sem.put(name, getNashornEngine().eval("Java.type('"+clazz.getName()+"');"));
    }

    @Override
    public void reload(){
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().endsWith(".js");
            }
        };

        jsExecutors.clear();
        for(File file : executorFolder.listFiles(filter)){
            try {
                reloadExecutors(new Stack<String>(), file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor "+file.getName());
                continue;
            }
        }
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

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    @Override
    public Executor get(Object key) {
        return jsExecutors.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return jsExecutors.containsKey(key);
    }

    @Override
    public Set<Entry<String, Executor>> entrySet() {
        Set<Entry<String, Executor>> set = new HashSet<>();
        for(Entry<String, Executor> entry : jsExecutors.entrySet()){
            set.add(new AbstractMap.SimpleEntry<String, Executor>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    @Override
    public Map<String, Executor> getExecutorMap() {
        return this.jsExecutors;
    }

    class JSExecutor extends Executor{
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

            extractCustomVariables(variables, e);
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

                    if(plugin.isDebugging()){
                        Integer result = null;
                        long start = System.currentTimeMillis();
                        result = (Integer) invocable.invokeFunction(executorName, argObj);
                        long end = System.currentTimeMillis();
                        plugin.getLogger().info(executorName+" execution -- "+(end - start)+"ms");
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

        protected void extractCustomVariables(Map<String, Object> variables, Object e) {
            if(e instanceof InventoryInteractEvent){
                if(((InventoryInteractEvent) e).getWhoClicked() instanceof Player)
                    variables.put("player", ((InventoryInteractEvent) e).getWhoClicked());
            } else if(e instanceof InventoryCloseEvent){
                if(((InventoryCloseEvent) e).getPlayer() instanceof Player)
                    variables.put("player", ((InventoryCloseEvent) e).getPlayer());
            } else if(e instanceof InventoryOpenEvent){
                if(((InventoryOpenEvent) e).getPlayer() instanceof Player)
                    variables.put("player", ((InventoryOpenEvent) e).getPlayer());
            }
        }
    }

    public static void main(String[] ar){
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for(int i = 0; i < stack.size(); i++)
            System.out.println(i+". "+stack.get(i));
    }
}
