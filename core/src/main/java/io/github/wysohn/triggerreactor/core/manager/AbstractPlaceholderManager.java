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

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public abstract class AbstractPlaceholderManager extends AbstractJavascriptBasedManager implements KeyValueManager<Placeholder> {
    protected Map<String, Placeholder> jsPlaceholders = new HashMap<>();

    public AbstractPlaceholderManager(TriggerReactor plugin) throws ScriptException {
        super(plugin);
    }

    protected void reloadPlaceholders(File file, FileFilter filter) throws ScriptException, IOException {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.indexOf("."));

        if (jsPlaceholders.containsKey(fileName)) {
            plugin.getLogger().warning(fileName + " already registered! Duplicating placerholders?");
        } else {
            JSPlaceholder placeholder = new JSPlaceholder(fileName, IScriptEngineInitializer.getNashornEngine(sem), file);
            jsPlaceholders.put(fileName, placeholder);
        }
    }

    @Override
    public Placeholder get(Object key) {
        return jsPlaceholders.get(key);
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
    public Map<String, Placeholder> getBackedMap() {
        return jsPlaceholders;
    }

    public static class JSPlaceholder extends Placeholder {
        private final String placeholderName;
        private final String sourceCode;

        private ScriptEngine engine = null;
        private CompiledScript compiled = null;

        public JSPlaceholder(String placeholderName, ScriptEngine engine, File file) throws ScriptException, IOException {
            this.placeholderName = placeholderName;
            this.engine = engine;

            StringBuilder builder = new StringBuilder();
            FileReader reader = new FileReader(file);
            int read = -1;
            while ((read = reader.read()) != -1)
                builder.append((char) read);
            reader.close();
            sourceCode = builder.toString();

            Compilable compiler = (Compilable) engine;
            compiled = compiler.compile(sourceCode);
        }

        @Override
        public Object parse(Object context, Map<String, Object> variables, Object... args) throws Exception {
            final Bindings bindings = engine.createBindings();

            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                bindings.put(key, value);
            }

            ScriptContext scriptContext = new SimpleScriptContext();
            try {
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                compiled.eval(scriptContext);
            } catch (ScriptException e2) {
                e2.printStackTrace();
            }

            JSObject jsObject = (JSObject) scriptContext.getAttribute(placeholderName);
            Callable<Object> call = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Object argObj = args;

                    if (TriggerReactor.getInstance().isDebugging()) {
                        Object result = null;
                        long start = System.currentTimeMillis();
                        result = jsObject.call(null, argObj);
                        long end = System.currentTimeMillis();
                        TriggerReactor.getInstance().getLogger().info(placeholderName + " placeholder -- " + (end - start) + "ms");
                        return result;
                    } else {
                        return jsObject.call(null, argObj);
                    }
                }
            };

            if (TriggerReactor.getInstance().isServerThread()) {
                Object result = null;
                try {
                    result = call.call();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new Exception("$" + placeholderName + " encountered error.", e1);
                }
                return result;
            } else {
                Future<Object> future = runSyncTaskForFuture(call);

                if (future == null) {
                    //probably server is shutting down
                    if (!TriggerReactor.getInstance().isEnabled()) {
                        return call.call();
                    } else {
                        throw new Exception("$" + placeholderName + " couldn't be finished. The server returned null Future.");
                    }
                } else {
                    Object result = null;
                    try {
                        result = future.get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e1) {
                        throw new Exception("$" + placeholderName + " encountered error.", e1);
                    } catch (TimeoutException e1) {
                        throw new Exception("$" + placeholderName + " was stopped. It took longer than 5 seconds to process. Is the server lagging?", e1);
                    }
                    return result;
                }
            }
        }
    }
}
