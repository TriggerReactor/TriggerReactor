package js;

import io.github.wysohn.triggerreactor.bukkit.manager.BukkitScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@PowerMockIgnore("javax.script.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({TriggerReactorCore.class, Bukkit.class})
public abstract class AbstractTestJavaScripts {
    private static final IScriptEngineInitializer INITIALIZER = new BukkitScriptEngineInitializer() {
    };

    protected ScriptEngineManager sem;
    protected ScriptEngine engine;

    @Before
    public void init() throws Exception {
        sem = new ScriptEngineManager(null);
        INITIALIZER.initScriptEngine(sem);
        engine = sem.getEngineByName("nashorn");

        TriggerReactorCore mockMain = Mockito.mock(TriggerReactorCore.class);
        Mockito.when(mockMain.isServerThread()).thenReturn(true);

        PluginManager mockPluginManager = Mockito.mock(PluginManager.class);
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

        PowerMockito.mockStatic(TriggerReactorCore.class);
        Mockito.when(TriggerReactorCore.getInstance()).thenReturn(mockMain);

        PowerMockito.mockStatic(Bukkit.class);
        Mockito.when(Bukkit.getPluginManager()).thenReturn(mockPluginManager);
        Mockito.when(Bukkit.dispatchCommand(Mockito.any(CommandSender.class), Mockito.anyString()))
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
