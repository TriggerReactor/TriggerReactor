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

import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Test {
    private final String script;

    private Charset charset = StandardCharsets.UTF_8;
    private InterpreterGlobalContext globalContext = new InterpreterGlobalContext();
    private Map<String, Object> scriptVars = new HashMap<>();

    private Test(String script) {
        this.script = script;
    }

    public Object getGlobalVar(String name) {
        return globalContext.gvars.get(name);
    }

    public <R> R test() throws InterpreterException, ParserException, IOException, LexerException {
        Lexer lexer = new Lexer(script, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Interpreter interpreter = InterpreterBuilder.start(globalContext, root)
                .addLocalVariables(scriptVars)
                .build();

        interpreter.start();

        return (R) interpreter.result();
    }

    public static class Builder {
        private final Test test;

        private Builder(String script) {
            this.test = new Test(script);
            this.test.globalContext = new InterpreterGlobalContext();
        }

        public Builder charset(Charset charset) {
            ValidationUtil.notNull(charset);
            test.charset = charset;
            return this;
        }

        public Builder putExecutor(String name, Executor executor) {
            ValidationUtil.notNull(name);
            ValidationUtil.notNull(executor);
            test.globalContext.executorMap.put(name, executor);
            return this;
        }

        public Builder putPlaceholder(String name, Placeholder placeholder) {
            ValidationUtil.notNull(name);
            ValidationUtil.notNull(placeholder);
            test.globalContext.placeholderMap.put(name, placeholder);
            return this;
        }

        public Builder addScriptVariable(String name, Object value) {
            ValidationUtil.notNull(name);
            ValidationUtil.notNull(value);
            test.scriptVars.put(name, value);
            return this;
        }

        public Test build() {
            return test;
        }

        public static Builder of(String script) {
            return new Builder(script);
        }
    }
}
