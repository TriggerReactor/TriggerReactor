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

import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationResult;
import io.github.wysohn.triggerreactor.core.script.validation.Validator;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public abstract class AbstractJavascriptBasedManager extends Manager {
    @Inject
    protected IGameController gameController;
    @Inject
    protected IPluginLifecycleController pluginLifecycleController;
    @Inject
    protected TaskSupervisor taskSupervisor;
    @Inject
    protected ScriptEngineManager sem;
    @Inject
    protected Logger logger;
    @Inject
    @Named("DataFolder")
    protected File dataFolder;

    public static ScriptEngine getEngine(ScriptEngineManager sem) {
        ScriptEngine engine = sem.getEngineByName("graal.js");
        if (engine != null) {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowAllAccess", true);
            return engine;
        }

        engine = sem.getEngineByName("JavaScript");
        if (engine != null) {
            return engine;
        }

        throw new RuntimeException("No java script engine was available. If you are using Java version above 11, " + "the stock Java does not contain the java script engine as it used to be. Install GraalVM instead of " + "the stock Java, or you have to download third-party plugin, such as JShader.");
    }

    protected static String readSourceCode(InputStream file) throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(file);
        int read = -1;
        while ((read = reader.read()) != -1) builder.append((char) read);
        reader.close();
        return builder.toString();
    }

    protected abstract class Evaluable<R> {
        private final String indentifier;
        private final String timingsGroup;
        private final String functionName;
        private final String sourceCode;

        private final ScriptEngine engine;

        private CompiledScript compiled = null;
        private boolean firstRun = true;
        private Validator validator = null;

        public Evaluable(String indentifier,
                         String timingsGroup,
                         String functionName,
                         String sourceCode,
                         ScriptEngine engine) throws ScriptException {
            this.indentifier = indentifier;
            this.timingsGroup = timingsGroup;
            this.functionName = functionName;
            this.sourceCode = sourceCode;

            this.engine = engine;

            synchronized (this.engine) {
                Compilable compiler = (Compilable) this.engine;
                compiled = compiler.compile(sourceCode);
            }
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
            scriptContext.setBindings(engine.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);

            bindings.put("event", event);
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                bindings.put(key, value);
            }

            try (Timings.Timing t = time.getTiming("JS <eval>").begin()) {
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

            if (gameController.isServerThread()) {
                R result = null;

                try {
                    result = call.call();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new Exception(indentifier + functionName + " encountered error.", e1);
                }
                return result;
            } else {
                Future<R> future = taskSupervisor.submitSync(call);
                if (future == null) {
                    //probably server is shutting down
                    if (!pluginLifecycleController.isEnabled()) {
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
                        throw new Exception(indentifier + functionName + " was stopped. It took longer than 5 seconds to process. Is the server lagging?",
                                            e1);
                    }
                    return result;
                }
            }
        }

        private void registerValidationInfo(Bindings bindings) {
            Map<String, Object> validation = (Map<String, Object>) bindings.get("validation");
            if (validation == null) {
                return;
            }
            this.validator = Validator.from(validation);
        }

        public ValidationResult validate(Object... args) {
            return validator.validate(args);
        }
    }
}