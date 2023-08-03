package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ScriptEngineProviderTest {

    private ScriptEngineProvider scriptEngineProvider;

    private ScriptEngine engine1;
    private ScriptEngine engine2;

    @Before
    public void setUp() throws Exception {
        engine1 = mock(ScriptEngine.class);
        engine2 = mock(ScriptEngine.class);
        scriptEngineProvider = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Set<IScriptEngineGateway> gateways = new HashSet<>();
                        gateways.add(new Gateway(engine1, "engine1", 0));
                        gateways.add(new Gateway(engine2, "engine2", 100));

                        bind(new TypeLiteral<Set<IScriptEngineGateway>>() {
                        })
                                .toInstance(gateways);
                    }
                }
        ).getInstance(ScriptEngineProvider.class);
    }

    @Test
    public void getEngine() {
        // arrange

        // act
        ScriptEngine engine = scriptEngineProvider.getEngine();

        // assert
        assertNotNull(engine);
        assertEquals(engine2, engine);
    }

    private class Gateway implements IScriptEngineGateway {
        private final ScriptEngine engine;
        private final String name;
        private final int priority;

        public Gateway(ScriptEngine engine, String name, int priority) {
            this.engine = engine;
            this.name = name;
            this.priority = priority;
        }

        @Override
        public ScriptEngine getEngine() {
            return engine;
        }

        @Override
        public String getEngineName() {
            return name;
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}