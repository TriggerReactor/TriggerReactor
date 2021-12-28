package js;

import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import js.components.DaggerScriptEngineComponent;
import js.components.ScriptEngineComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.script.*;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public abstract class AbstractTestJavaScripts {
    protected Server server;
    protected IGameController mockMain;
    protected PluginManager mockPluginManager;
    protected InterpreterLocalContext localContext;

    @Before
    public void init() throws Exception {
        mockMain = mock(IGameController.class);
        Mockito.when(mockMain.isServerThread()).thenReturn(true);

        mockPluginManager = mock(PluginManager.class);
        Mockito.when(mockPluginManager.isPluginEnabled(Mockito.anyString())).thenAnswer(invocation -> {
            String pluginName = invocation.getArgument(0);

            switch (pluginName) {
                case "PlaceholderAPI":
                    return false;
            }

            return false;
        });

        localContext = new InterpreterLocalContext();
        localContext.setExtra(Interpreter.SCRIPT_ENGINE_KEY, component.engine());

        before();

        server = mock(Server.class);
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);

        Mockito.when(server.getPluginManager()).thenReturn(mockPluginManager);
        Mockito.when(server.dispatchCommand(Mockito.any(CommandSender.class), Mockito.anyString())).then(invocation -> {
            CommandSender sender = invocation.getArgument(0);
            String command = invocation.getArgument(1);

            // send the command as message for test purpose
            sender.sendMessage(command);

            return null;
        });
    }

    protected void before() throws Exception {

    }

    @Test
    public void testWithCompilation() throws Exception {
        ScriptEngineManager sem = component.manager();
        ScriptEngine engine = sem.getEngineByName("nashorn");
        CompiledScript compiled = ((Compilable) engine).compile("a + b");

        for (int i = 0; i < 10000; i++) {
            Bindings bindings = engine.createBindings();
            bindings.put("a", i);
            bindings.put("b", i);

            Object result = compiled.eval(bindings);
            assertEquals(result, (double) i + i);
        }
    }

    @Test
    public void testWithoutCompilation() throws Exception {
        ScriptEngineManager sem = component.manager();
        ScriptEngine engine = sem.getEngineByName("nashorn");

        for (int i = 0; i < 10000; i++) {
            Bindings bindings = engine.createBindings();
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            bindings.put("a", i);
            bindings.put("b", i);

            Object result = engine.eval("a + b");
            assertEquals(result, (double) i + i);
        }
    }

    protected void register(ScriptEngineManager sem, ScriptEngine engine, Class<?> clazz) throws ScriptException {
        engine.put("Temp", clazz);
        engine.eval("var " + clazz.getSimpleName() + " = Temp.static;");
    }

    protected static final ScriptEngineComponent component = DaggerScriptEngineComponent.create();
}
