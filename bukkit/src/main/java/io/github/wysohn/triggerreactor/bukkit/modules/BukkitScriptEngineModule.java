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

package io.github.wysohn.triggerreactor.bukkit.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.ProvidesIntoSet;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.SharedVariableManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BukkitScriptEngineModule extends AbstractModule {
    @ProvidesIntoSet
    public ScriptEngineInitializer provideJavaScriptInitializer(
            SharedVariableManager sharedVariableManager,
            GlobalVariableManager globalVariableManager) {
        return (sem) -> {
            sem.put("plugin", this);

            for (Map.Entry<String, AbstractAPISupport> entry : sharedVariableManager.getSharedVars().entrySet()) {
                sem.put(entry.getKey(), entry.getValue());
            }

            sem.put("get", new Function<String, Object>() {
                @Override
                public Object apply(String t) {
                    return globalVariableManager.get(t);
                }
            });

            sem.put("put", new BiFunction<String, Object, Void>() {
                @Override
                public Void apply(String a, Object b) {
                    if (!GlobalVariableManager.isValidName(a))
                        throw new RuntimeException("[" + a + "] cannot be used as key");

                    if (a != null && b == null) {
                        globalVariableManager.remove(a);
                    } else {
                        try {
                            globalVariableManager.put(a, b);
                        } catch (Exception e) {
                            throw new RuntimeException("Executor -- put(" + a + "," + b + ")", e);
                        }
                    }

                    return null;
                }
            });

            sem.put("has", new Function<String, Boolean>() {
                @Override
                public Boolean apply(String t) {
                    return globalVariableManager.has(t);
                }
            });
        };
    }
}
