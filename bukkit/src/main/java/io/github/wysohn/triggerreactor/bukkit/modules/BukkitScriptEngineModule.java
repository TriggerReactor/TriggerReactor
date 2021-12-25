/*
 *     Copyright (C) 2021 wysohn and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import org.bukkit.Server;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Module
public abstract class BukkitScriptEngineModule {
    @Provides
    @Singleton
    static ScriptEngineManager bindScriptEngineManager(Server server) {
        ScriptEngineManager scriptEngineManager = server.getServicesManager().load(ScriptEngineManager.class);

        if (scriptEngineManager == null) scriptEngineManager = new ScriptEngineManager();

        return scriptEngineManager;
    }

    @Provides
    @IntoSet
    static IScriptEngineInitializer providePluginInitializer(@Named("PluginInstance") Lazy<Object> pluginInstance) {
        return (sem) -> sem.put("plugin", pluginInstance.get());
    }

    @Provides
    @IntoSet
    static IScriptEngineInitializer provideSharedVarsInitializer(Lazy<TriggerReactorMain> main) {
        return (sem) -> {
            for (Map.Entry<String, AbstractAPISupport> entry : main.get().getSharedVars().entrySet()) {
                sem.put(entry.getKey(), entry.getValue());
            }
        };
    }

    @Provides
    @IntoSet
    static IScriptEngineInitializer provideInitializer(Lazy<GlobalVariableManager> lazy) {
        return (sem) -> {
            GlobalVariableManager variableManager = lazy.get();

            sem.put("get", (Function<String, Object>) variableManager::get);

            sem.put("put", (BiFunction<String, Object, Void>) (a, b) -> {
                if (!GlobalVariableManager.isValidName(a))
                    throw new RuntimeException("[" + a + "] cannot be used as key");

                if (a != null && b == null) {
                    variableManager.remove(a);
                } else {
                    try {
                        variableManager.put(a, b);
                    } catch (Exception e) {
                        throw new RuntimeException("Executor -- put(" + a + "," + b + ")", e);
                    }
                }

                return null;
            });

            sem.put("has", (Function<String, Boolean>) t -> variableManager.has(t));
        };
    }
}
