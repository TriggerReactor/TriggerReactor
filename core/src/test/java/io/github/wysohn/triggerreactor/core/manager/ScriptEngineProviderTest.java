package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.components.DaggerScriptEngineProviderTestComponent;
import org.junit.Before;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScriptEngineProviderTest {

    Logger logger;
    ScriptEngineManager scriptEngineManager;

    @Before
    public void init(){
        logger = mock(Logger.class);
        scriptEngineManager = mock(ScriptEngineManager.class);
    }

    @Test(expected = RuntimeException.class)
    public void onEnable() throws Exception {
        ScriptEngineProvider provider = DaggerScriptEngineProviderTestComponent.builder()
                .logger(logger)
                .engineManager(scriptEngineManager)
                .build()
                .provider();

        provider.onEnable();
    }

    @Test
    public void onEnable2() throws Exception {
        ScriptEngineProvider provider = DaggerScriptEngineProviderTestComponent.builder()
                .logger(logger)
                .engineManager(scriptEngineManager)
                .build()
                .provider();

        ScriptEngine scriptEngine = mock(ScriptEngine.class);
        Bindings bindings = mock(Bindings.class);

        when(scriptEngine.getBindings(anyInt())).thenReturn(bindings);
        when(scriptEngineManager.getEngineByName(anyString())).thenReturn(scriptEngine);

        provider.onEnable();
    }


    @Test
    public void getEngine() {
    }
}