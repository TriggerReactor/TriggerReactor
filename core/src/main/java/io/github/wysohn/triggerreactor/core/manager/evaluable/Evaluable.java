/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.evaluable;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.interpreter.SynchronizableTask;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;
import io.github.wysohn.triggerreactor.core.script.validation.ValidationResult;
import io.github.wysohn.triggerreactor.core.script.validation.Validator;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.*;
import java.util.Map;
import java.util.concurrent.*;

abstract class Evaluable<R> implements IEvaluable {
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

        synchronized (this.engine) {
            Compilable compiler = (Compilable) this.engine;
            compiled = compiler.compile(sourceCode);
        }
    }

    @Override
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
                    throw new Exception(
                            indentifier + functionName + " couldn't be finished. The server returned null Future.");
                }
            } else {
                R result = null;
                try {
                    result = future.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e1) {
                    throw new Exception(indentifier + functionName + " encountered error.", e1);
                } catch (TimeoutException e1) {
                    throw new Exception(indentifier + functionName
                                                + " was stopped. It took longer than 5 seconds to process. Is the "
                                                + "server lagging?",
                                        e1);
                }
                return result;
            }
        }
    }

    @Override
    public void registerValidationInfo(Bindings bindings) {
        Map<String, Object> validation = (Map<String, Object>) bindings.get("validation");
        if (validation == null) {
            return;
        }
        this.validator = Validator.from(validation);
    }

    @Override
    public ValidationResult validate(Object... args) {
        return validator.validate(args);
    }
}
