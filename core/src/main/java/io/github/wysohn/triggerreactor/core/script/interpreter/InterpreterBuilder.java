/*
 * Copyright (C) 2023. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;

public class InterpreterBuilder {
    private final Interpreter interpreter;

    private InterpreterBuilder(InterpreterGlobalContext globalContext,
                               Interpreter interpreter,
                               Timings.Timing timing) {
        this.interpreter = interpreter;
        this.interpreter.context = new InterpreterLocalContext(timing);
        this.interpreter.globalContext = globalContext;
    }

    /**
     * Override the local context with the given instance.
     * Useful for testing by mocking the context.
     *
     * @param localContext the local context to use
     * @return this
     */
    public InterpreterBuilder overrideContext(InterpreterLocalContext localContext) {
        interpreter.context = localContext;
        return this;
    }

    public Interpreter build() {
        interpreter.verifyPreCondition();
        return interpreter;
    }

    public InterpreterBuilder addLocalVariables(Map<String, Object> scriptVars) {
        ValidationUtil.notNull(scriptVars);
        interpreter.context.putAllVars(scriptVars);
        return this;
    }

    public InterpreterBuilder withInterrupter(ProcessInterrupter interrupter) {
        ValidationUtil.notNull(interrupter);
        interpreter.context.setInterrupter(interrupter);
        return this;
    }

    public static InterpreterBuilder start(InterpreterGlobalContext globalContext,
                                           Node root) {
        return start(globalContext, root, Timings.LIMBO);
    }

    public static InterpreterBuilder start(InterpreterGlobalContext globalContext,
                                           Node root,
                                           Timings.Timing timing) {
        return new InterpreterBuilder(globalContext, new Interpreter(root), timing);
    }
}
