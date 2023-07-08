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

import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class InterpreterBuilder {
    private final Interpreter interpreter;

    private InterpreterBuilder(InterpreterGlobalContext globalContext,
                               Interpreter interpreter) {
        this.interpreter = interpreter;
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
        return interpreter;
    }

    public static InterpreterBuilder start(InterpreterGlobalContext globalContext,
                                           Node root) {
        return new InterpreterBuilder(globalContext, new Interpreter(root));
    }
}
