package js;

import io.github.wysohn.triggerreactor.bukkit.manager.BukkitScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCoreTest;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.mockito.Mockito;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

public abstract class AbstractTestJavaScripts {
    private static final IScriptEngineInitializer INITIALIZER = new BukkitScriptEngineInitializer() {
    };

    protected ScriptEngineManager sem;
    protected ScriptEngine engine;
    protected Server server;
    protected TriggerReactorCore mockMain;

    @Before
    public void init() throws Exception {
        sem = new ScriptEngineManager();
        INITIALIZER.initScriptEngine(sem);
        engine = IScriptEngineInitializer.getEngine(sem);

        mockMain = mock(TriggerReactorCore.class);
        Mockito.when(mockMain.isServerThread()).thenReturn(true);
        TriggerReactorCoreTest.setInstance(mockMain);

        PluginManager mockPluginManager = mock(PluginManager.class);
        Mockito.when(mockPluginManager.isPluginEnabled(Mockito.anyString())).thenAnswer(
                invocation -> {
                    String pluginName = invocation.getArgument(0);

                    switch (pluginName) {
                        case "PlaceholderAPI":
                            return false;
                    }

                    return false;
                }
        );

        before();

        server = mock(Server.class);
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);

        Mockito.when(server.getPluginManager()).thenReturn(mockPluginManager);
        Mockito.when(server.dispatchCommand(Mockito.any(CommandSender.class), Mockito.anyString()))
                .then(invocation -> {
                    CommandSender sender = invocation.getArgument(0);
                    String command = invocation.getArgument(1);

                    // send the command as message for test purpose
                    sender.sendMessage(command);

                    return null;
                });
    }

    protected abstract void before() throws Exception;

    protected void register(ScriptEngineManager sem, ScriptEngine engine, Class<?> clazz)
            throws ScriptException {
        engine.put("Temp", clazz);
        engine.eval("var " + clazz.getSimpleName() + " = Temp.static;");
    }
}
