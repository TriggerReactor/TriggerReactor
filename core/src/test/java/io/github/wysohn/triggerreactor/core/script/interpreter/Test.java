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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.IGlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.js.IBackedMapProvider;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Test {

    private Charset charset = StandardCharsets.UTF_8;
    private Map<String, Object> scriptVars = new HashMap<>();
    private Interpreter interpreter;
    private InterpreterLocalContext interpreterLocalContext;

    private Test(Interpreter interpreter) {
        ValidationUtil.notNull(interpreter);

        this.interpreter = interpreter;
    }

    public Object getGlobalVar(String name) {
        return this.interpreter.globalContext.gvars.get(name);
    }

    public <R> R test() throws InterpreterException, ParserException, IOException, LexerException {
        interpreterLocalContext = new InterpreterLocalContext(Timings.LIMBO);
        interpreter.start(null, interpreterLocalContext
                .putAllVars(scriptVars));

        return (R) interpreter.result(interpreterLocalContext);
    }

    public Object getScriptVar(String key) {
        return interpreterLocalContext.getVar(key);
    }

    public static class Builder {
        private final Test test;

        private Builder(String script) throws Exception {
            this(InterpreterBuilder.start(
                    Guice.createInjector(
                            new AbstractModule() {
                                @Provides
                                public IBackedMapProvider<Executor> provideExecutorMapProvider() {
                                    IBackedMapProvider mock = mock(IBackedMapProvider.class);
                                    when(mock.getBackedMap()).thenReturn(new HashMap<>());
                                    return mock;
                                }

                                @Provides
                                public IBackedMapProvider<Placeholder> providePlaceholderMapProvider() {
                                    IBackedMapProvider mock = mock(IBackedMapProvider.class);
                                    when(mock.getBackedMap()).thenReturn(new HashMap<>());
                                    return mock;
                                }

                                @Provides
                                public IGlobalVariableManager provideGlobalVariableManager() {
                                    IGlobalVariableManager mock = mock(IGlobalVariableManager.class);
                                    when(mock.getGlobalVariableAdapter()).thenReturn(new HashMap<>());
                                    return mock;
                                }

                                @Provides
                                public IExceptionHandle provideExceptionHandle() {
                                    return mock(IExceptionHandle.class);
                                }

                                @Provides
                                public SelfReference provideSelfReference() {
                                    return mock(SelfReference.class);
                                }

                                @Provides
                                public TaskSupervisor provideTaskSupervisor() {
                                    return mock(TaskSupervisor.class);
                                }
                            }
                    ).getInstance(InterpreterGlobalContext.class),
                    new Parser(new Lexer(script, StandardCharsets.UTF_8)).parse()
            ).build());
        }

        private Builder(Interpreter interpreter) throws Exception {
            this.test = new Test(interpreter);
        }

        public Builder charset(Charset charset) {
            ValidationUtil.notNull(charset);
            test.charset = charset;
            return this;
        }

        public Builder putExecutor(String name, Executor executor) {
            ValidationUtil.notNull(name);
            ValidationUtil.notNull(executor);
            test.interpreter.globalContext.executorMap.put(name, executor);
            return this;
        }

        public Builder putPlaceholder(String name, Placeholder placeholder) {
            ValidationUtil.notNull(name);
            ValidationUtil.notNull(placeholder);
            test.interpreter.globalContext.placeholderMap.put(name, placeholder);
            return this;
        }

        public Builder addScriptVariable(String name, Object value) {
            ValidationUtil.notNull(name);
            ValidationUtil.notNull(value);
            test.scriptVars.put(name, value);
            return this;
        }

        public Builder overrideSelfReference(SelfReference selfReference) {
            ValidationUtil.notNull(selfReference);
            test.interpreter.globalContext.selfReference = selfReference;
            return this;
        }

        public Builder overrideTaskSupervisor(TaskSupervisor mockTaskSupervisor) {
            ValidationUtil.notNull(mockTaskSupervisor);
            test.interpreter.globalContext.task = mockTaskSupervisor;
            return this;
        }

        public Builder addGlobalVariable(String key, Object value) {
            ValidationUtil.notNull(key);
            ValidationUtil.notNull(value);
            test.interpreter.globalContext.gvars.put(key, value);
            return this;
        }

        public Builder addTemporaryGlobalVariable(String key, Object value) {
            ValidationUtil.notNull(key);
            ValidationUtil.notNull(value);
            test.interpreter.globalContext.gvars.put(new TemporaryGlobalVariableKey(key), value);
            return this;
        }

        public Test build() {
            return test;
        }

        public static Builder of(String script) throws Exception {
            return new Builder(script);
        }

        public static Builder of(Interpreter interpreter) throws Exception {
            return new Builder(interpreter);
        }
    }
}
