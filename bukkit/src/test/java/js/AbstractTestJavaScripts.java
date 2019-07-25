package js;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.function.Function;

@PowerMockIgnore("javax.script.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({TriggerReactor.class, Bukkit.class})
public class AbstractTestJavaScripts {
    protected ScriptEngineManager sem;
    protected ScriptEngine engine;

    @Before
    public void init() throws Exception{
        sem = new ScriptEngineManager(null);
        engine = sem.getEngineByName("nashorn");

        sem.put("Char", new Function<String, Character>() {
            @Override
            public Character apply(String t) {
                return t.charAt(0);
            }
        });
        register(sem, engine, ReflectionUtil.class);
        register(sem, engine, Bukkit.class);
        register(sem, engine, ChatColor.class);

        TriggerReactor mockMain = Mockito.mock(TriggerReactor.class);
        Mockito.when(mockMain.isServerThread()).thenReturn(true);

        PluginManager mockPluginManager = Mockito.mock(PluginManager.class);
        Mockito.when(mockPluginManager.isPluginEnabled(Mockito.anyString())).thenAnswer(
                invocation -> {
                    String pluginName = invocation.getArgument(0);

                    switch (pluginName){
                        case "PlaceholderAPI":
                            return false;
                    }

                    return false;
                }
        );

        PowerMockito.mockStatic(TriggerReactor.class);
        Mockito.when(TriggerReactor.getInstance()).thenReturn(mockMain);
        PowerMockito.mockStatic(Bukkit.class);
        Mockito.when(Bukkit.getPluginManager()).thenReturn(mockPluginManager);
    }

    private void register(ScriptEngineManager sem, ScriptEngine engine, Class<?> clazz)
            throws ScriptException {
        engine.put("Temp", clazz);
        engine.eval("var "+clazz.getSimpleName()+" = Temp.static;");
    }
}
