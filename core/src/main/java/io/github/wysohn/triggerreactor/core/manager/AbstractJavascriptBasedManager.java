/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractJavascriptBasedManager extends Manager implements IScriptEngineInitializer {

    protected static final ScriptEngineManager sem = new ScriptEngineManager(null);
    public static AbstractJavascriptBasedManager instance;

    @Override
    public void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        IScriptEngineInitializer.super.initScriptEngine(sem);

        sem.put("plugin", this.plugin);

        for (Entry<String, AbstractAPISupport> entry : this.plugin.getSharedVars().entrySet()) {
            sem.put(entry.getKey(), entry.getValue());
        }

        sem.put("get", new Function<String, Object>() {
            @Override
            public Object apply(String t) {
                return plugin.getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>() {
            @Override
            public Void apply(String a, Object b) {
                if (!GlobalVariableManager.isValidName(a))
                    throw new RuntimeException("[" + a + "] cannot be used as key");

                if (a != null && b == null) {
                    plugin.getVariableManager().remove(a);
                } else {
                    try {
                        plugin.getVariableManager().put(a, b);
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
                return plugin.getVariableManager().has(t);
            }
        });
    }

    public AbstractJavascriptBasedManager(TriggerReactorCore plugin) throws ScriptException {
        super(plugin);

        instance = this;
        initScriptEngine(sem);
    }

}