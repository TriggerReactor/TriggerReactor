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
import io.github.wysohn.triggerreactor.core.manager.javascript.CompiledEvaluable;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationResult;
import io.github.wysohn.triggerreactor.core.script.validation.Validator;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public abstract class AbstractJavascriptBasedManager extends Manager {
    @Inject
    Set<IScriptEngineInitializer> initializerSet;
    @Inject
    ScriptEngineManager sem;
    @Inject
    IGameController gameController;
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    TaskSupervisor taskSupervisor;

    @Override
    public void onEnable() throws Exception {
        for (IScriptEngineInitializer init : initializerSet) {
            init.initScriptEngine(sem);
        }
    }

    protected abstract class Evaluable<R> {
        private final String identifier;
        private final String timingsGroup;
        private final String functionName;
        private final String sourceCode;
        private final CompiledEvaluable<R> compiled;

        //        private CompiledScript compiled = null;
        private boolean firstRun = true;
        private Validator validator = null;

        public Evaluable(String identifier, String timingsGroup, String functionName, String sourceCode) {
            this.identifier = identifier;
            this.timingsGroup = timingsGroup;
            this.functionName = functionName;
            this.sourceCode = sourceCode;
            this.compiled = new CompiledEvaluable(functionName);
        }

        public R evaluate(Timings.Timing timing,
                          InterpreterLocalContext localContext,
                          Map<String, Object> variables,
                          Object... args) throws Exception {
            ScriptEngine engine = (ScriptEngine) localContext.getExtra(Interpreter.SCRIPT_ENGINE_KEY);
            ValidationUtil.notNull(engine);

//            if(compiled == null || compiled.getEngine() != engine){
//                Compilable compiler = (Compilable) engine;
//                compiled = compiler.compile(sourceCode);
//            }
            compiled.compile(engine, sourceCode);

            Timings.Timing time = timing.getTiming(timingsGroup).getTiming(functionName);
            time.setDisplayName(identifier + functionName);

            final ScriptContext scriptContext = new SimpleScriptContext();
            final Bindings bindings = engine.createBindings();

            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            scriptContext.setBindings(engine.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);

            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                bindings.put(key, value);
            }

            try (Timings.Timing t = time.getTiming("JS <eval>").begin()) {
                compiled.evaluate(engine, scriptContext);
//                compiled.eval(scriptContext);
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

//            Object jsObject = bindings.get(functionName);
//            if (jsObject == null)
//                throw new Exception(functionName + ".js does not have 'function " + functionName + "()'.");

            Callable<R> call = () -> {
                Object argObj = args;
                Object result = null;

//                try (Timings.Timing t = time.begin(true)) {
//                    engine.setContext(scriptContext);
//                    result = ((Invocable) engine).invokeFunction(functionName, argObj);
//                }
                result = compiled.invokeFunction(engine, scriptContext, time, argObj);

                return (R) result;
            };

            if (gameController.isServerThread()) {
                R result = null;

                try {
                    result = call.call();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new Exception(identifier + functionName + " encountered error.", e1);
                }
                return result;
            } else {
                Future<R> future = taskSupervisor.submitSync(call);
                if (future == null) {
                    //probably server is shutting down
                    if (!pluginLifecycleController.isEnabled()) {
                        return call.call();
                    } else {
                        throw new Exception(
                                identifier + functionName + " couldn't be finished. The server returned null Future.");
                    }
                } else {
                    R result = null;
                    try {
                        result = future.get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e1) {
                        throw new Exception(identifier + functionName + " encountered error.", e1);
                    } catch (TimeoutException e1) {
                        throw new Exception(
                                identifier + functionName + " was stopped. It took longer than 5 seconds to process. "
                                        + "Is the server lagging?", e1);
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