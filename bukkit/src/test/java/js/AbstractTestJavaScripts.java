/*
 * Copyright (C) 2022. TriggerReactor Team
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

package js;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.js.engine.IJSEvaluator;
import io.github.wysohn.triggerreactor.core.manager.js.engine.PolyglotJSEngine;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.graalvm.polyglot.Engine;
import org.junit.Before;
import org.mockito.Mockito;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public abstract class AbstractTestJavaScripts {
    // protected ScriptEngineManager sem;
    protected IJSEvaluator engine;
    protected Server server;
    protected TriggerReactorCore mockMain;
    protected PluginManager mockPluginManager;

    @Before
    public void init() throws Exception {
        // sem = new ScriptEngineManager();
        // Bindings bindings = sem.getBindings();

        engine = new PolyglotJSEngine(Engine.create(), Thread.currentThread().getContextClassLoader());
        engine.clearBindings();
        ScriptEngineInitializer.DEFAULT.initialize(engine.getBindings());

        // sem.setBindings(bindings);
        // engine = new CompiledScriptEngine(sem.getEngineByExtension("js"));

        mockMain = mock(TriggerReactorCore.class);

        mockPluginManager = mock(PluginManager.class);
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

    protected void register(IJSEvaluator engine, Class<?> clazz)
            throws Exception {
        engine.compile("var " + clazz.getSimpleName() + " = Temp.static;");
        engine.putBindings("Temp", clazz);

        engine.eval();
    }
}
