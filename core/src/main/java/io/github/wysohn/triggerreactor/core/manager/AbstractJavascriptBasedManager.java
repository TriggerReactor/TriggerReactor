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
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.SynchronizableTask;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationResult;
import io.github.wysohn.triggerreactor.core.script.validation.Validator;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractJavascriptBasedManager extends Manager implements IScriptEngineInitializer {
    protected static final ScriptEngineManager sem = new ScriptEngineManager();
    public static AbstractJavascriptBasedManager instance;

    @Override
    public void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);

        sem.put("plugin", this.plugin);

        for (Entry<String, AbstractAPISupport> entry : this.plugin.getSharedVars().entrySet()) {
            sem.put(entry.getKey(), entry.getValue());
        }

        sem.put("get", new Function<String, Object>() {
            @Override
            public Object apply(String t) {
                return plugin.getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>() {
            @Override
            public Void apply(String a, Object b) {
                if (!GlobalVariableManager.isValidName(a))
                    throw new RuntimeException("[" + a + "] cannot be used as key");

                if (a != null && b == null) {
                    plugin.getVariableManager().remove(a);
                } else {
                    try {
                        plugin.getVariableManager().put(a, b);
                    } catch (Exception e) {
                        throw new RuntimeException("Executor -- put(" + a + "," + b + ")", e);
                    }
                }

                return null;
            }
        });

        sem.put("has", new Function<String, Boolean>() {
            @Override
            public Boolean apply(String t) {
                return plugin.getVariableManager().has(t);
            }
        });
    }

    public AbstractJavascriptBasedManager(TriggerReactorCore plugin) throws ScriptException {
        super(plugin);

        instance = this;
        initScriptEngine(sem);
    }

    protected static abstract class Evaluable<R>{
        private final String indentifier;
        private final String timingsGroup;
        private final String functionName;
        private final String sourceCode;

        private final ScriptEngine engine;

        private CompiledScript compiled = null;
        private boolean firstRun = true;
        private Validator validator = null;

        public Evaluable(String indentifier, String timingsGroup, String functionName, String sourceCode,
                         ScriptEngine engine) throws ScriptException {
            this.indentifier = indentifier;
            this.timingsGroup = timingsGroup;
            this.functionName = functionName;
            this.sourceCode = sourceCode;

            this.engine = engine;

            synchronized (this.engine){
                Compilable compiler = (Compilable) this.engine;
                compiled = compiler.compile(sourceCode);
            }
        }

        private void registerValidationInfo(Bindings bindings) {
            Map<String, Object> validation = (Map<String, Object>) bindings.get("validation");
            if (validation == null) {
                return;
            }
            this.validator = Validator.from(validation);
        }

        public ValidationResult validate(Object... args){
            return validator.validate(args);
        }

        public R evaluate(Timings.Timing timing,
                          Map<String, Object> variables,
                          Object event,
                          Object... args) throws Exception {
            Timings.Timing time = timing.getTiming(timingsGroup).getTiming(functionName);
            time.setDisplayName(indentifier + functionName);

            final ScriptContext scriptContext = new SimpleScriptContext();
            final Bindings bindings = engine.createBindings();

            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            scriptContext.setBindings(engine.getBindings(ScriptContext.GLOBAL_SCOPE),
                    ScriptContext.GLOBAL_SCOPE);

            bindings.put("event", event);
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                bindings.put(key, value);
            }

            try (Timings.Timing t = time.getTiming("JS <eval>").begin()){
                compiled.eval(scriptContext);
            } catch (ScriptException e2) {
                e2.printStackTrace();
            }

            if (firstRun) {
                registerValidationInfo(bindings);
                firstRun = false;
            }

            if (validator != null) {
                ValidationResult result = validator.validate(args);
                int overload = result.getOverload();
                if (overload == -1) {
                    throw new ValidationException(result.getError());
                }
                bindings.put("overload", overload);
            }

            Object jsObject = bindings.get(functionName);
            if (jsObject == null)
                throw new Exception(functionName + ".js does not have 'function " + functionName + "()'.");

            Callable<R> call = () -> {
                Object argObj = args;
                Object result = null;

                try (Timings.Timing t = time.begin(true)) {
                    engine.setContext(scriptContext);
                    result = ((Invocable) engine).invokeFunction(functionName, argObj);
                }

                return (R) result;
            };

            if (TriggerReactorCore.getInstance().isServerThread()) {
                R result = null;

                try {
                    result = call.call();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new Exception(indentifier + functionName + " encountered error.", e1);
                }
                return result;
            } else {
                Future<R> future = SynchronizableTask.runSyncTaskForFuture(call);
                if (future == null) {
                    //probably server is shutting down
                    if (!TriggerReactorCore.getInstance().isEnabled()) {
                        return call.call();
                    } else {
                        throw new Exception(indentifier + functionName + " couldn't be finished. The server returned null Future.");
                    }
                } else {
                    R result = null;
                    try {
                        result = future.get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e1) {
                        throw new Exception(indentifier + functionName + " encountered error.", e1);
                    } catch (TimeoutException e1) {
                        throw new Exception(indentifier + functionName + " was stopped. It took longer than 5 seconds to process. Is the server lagging?", e1);
                    }
                    return result;
                }
            }
        }

        protected static String readSourceCode(InputStream file) throws IOException {
            StringBuilder builder = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(file);
            int read = -1;
            while ((read = reader.read()) != -1)
                builder.append((char) read);
            reader.close();
            return builder.toString();
        }
    }
}