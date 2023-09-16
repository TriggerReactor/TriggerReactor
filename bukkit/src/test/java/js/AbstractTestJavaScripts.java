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
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.mockito.Mockito;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

public abstract class AbstractTestJavaScripts {
    protected ScriptEngineManager sem;
    protected ScriptEngine engine;
    protected Server server;
    protected TriggerReactorCore mockMain;
    protected PluginManager mockPluginManager;

    @Before
    public void init() throws Exception {
        sem = new ScriptEngineManager();
        Bindings bindings = sem.getBindings();
        ScriptEngineInitializer.DEFAULT.initialize(bindings);
        sem.setBindings(bindings);
        engine = sem.getEngineByExtension("js");

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

    protected void register(ScriptEngineManager sem, ScriptEngine engine, Class<?> clazz)
            throws ScriptException {
        engine.put("Temp", clazz);
        engine.eval("var " + clazz.getSimpleName() + " = Temp.static;");
    }
}
