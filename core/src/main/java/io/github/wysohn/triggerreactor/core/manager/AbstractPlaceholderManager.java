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
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationResult;
import io.github.wysohn.triggerreactor.core.script.validation.Validator;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public abstract class AbstractPlaceholderManager extends AbstractJavascriptBasedManager implements KeyValueManager<Placeholder> {
    protected Map<String, Placeholder> jsPlaceholders = new HashMap<>();

    public AbstractPlaceholderManager(TriggerReactorCore plugin) throws ScriptException {
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
        private boolean firstRun = true;
        private Validator validator = null;

        public JSPlaceholder(String placeholderName, ScriptEngine engine, File file) throws ScriptException, IOException {
            this(placeholderName, engine, new FileInputStream(file));
        }

        private void registerValidationInfo(ScriptContext context) {
            Map<String, Object> validation = (Map<String, Object>) context.getAttribute("validation");
            if (validation == null) {
                return;
            }
            this.validator = Validator.from(validation);
        }

        public JSPlaceholder(String placeholderName, ScriptEngine engine, InputStream file) throws ScriptException, IOException {
            this.placeholderName = placeholderName;
            this.engine = engine;

            StringBuilder builder = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(file);
            int read = -1;
            while ((read = reader.read()) != -1)
                builder.append((char) read);
            reader.close();
            sourceCode = builder.toString();

            Compilable compiler = (Compilable) engine;
            compiled = compiler.compile(sourceCode);
        }

        public ValidationResult validate(Object... args) {
            if (firstRun) {
                throw new RuntimeException("the executor must be run at least once before using validate");
            }
            return validator.validate(args);
        }

        @Override
        public Object parse(Timings.Timing timing, Object context, Map<String, Object> variables,
                            Object... args) throws Exception {
            Timings.Timing time = timing.getTiming("Executors").getTiming(placeholderName);
            time.setDisplayName("$" + placeholderName);

            final Bindings bindings = engine.createBindings();

            bindings.put("event", context);
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

            if (firstRun) {
                registerValidationInfo(scriptContext);
                firstRun = false;
            }

            if (validator != null) {
                ValidationResult result = validator.validate(args);
                int overload = result.getOverload();
                if (overload == -1) {
                    throw new ValidationException(result.getError());
                }
                scriptContext.setAttribute("overload", overload, ScriptContext.ENGINE_SCOPE);
            }

            Invocable jsObject = (Invocable) scriptContext.getAttribute(placeholderName);
            if (jsObject == null)
                throw new Exception(placeholderName + ".js does not have 'function " + placeholderName + "()'.");

            Callable<Object> call = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Object argObj = args;
                    Object result = null;

                    try (Timings.Timing t = time.begin(true)) {
                        result = jsObject.invokeMethod(jsObject, "call", argObj);
                    }

                    return result;
                }
            };

            if (TriggerReactorCore.getInstance().isServerThread()) {
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
                    if (!TriggerReactorCore.getInstance().isEnabled()) {
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
