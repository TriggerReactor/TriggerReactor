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

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

public abstract class AbstractJavascriptBasedManager extends Manager {

    protected static final ScriptEngineManager sem = new ScriptEngineManager(null);
    public static AbstractJavascriptBasedManager instance;

    protected static ScriptEngine getNashornEngine() {
        return sem.getEngineByName("nashorn");
    }

    /**
     * Initializes pre-defined functions and variables for Executors.
     * @throws ScriptException
     */
    protected void initScriptEngine() throws ScriptException{
        sem.put("plugin", this.plugin);

        for(Entry<String, AbstractAPISupport> entry : this.plugin.getSharedVars().entrySet()) {
            sem.put(entry.getKey(), entry.getValue());
        }

        sem.put("get", new Function<String, Object>(){
            @Override
            public Object apply(String t) {
                return plugin.getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>(){
            @Override
            public Void apply(String a, Object b) {
                if(!AbstractVariableManager.isValidName(a))
                    throw new RuntimeException("["+a+"] cannot be used as key");

                if(a != null && b == null){
                    plugin.getVariableManager().remove(a);
                } else{
                    try {
                        plugin.getVariableManager().put(a, b);
                    } catch (Exception e) {
                        throw new RuntimeException("Executor -- put("+a+","+b+")", e);
                    }
                }

                return null;
            }
        });

        sem.put("has", new Function<String, Boolean>(){
            @Override
            public Boolean apply(String t) {
                return plugin.getVariableManager().has(t);
            }
        });

        sem.put("Char", new Function<String, Character>(){
            @Override
            public Character apply(String t) {
                return t.charAt(0);
            }
        });
    }

    protected void registerClass(Class<?> clazz) throws ScriptException {
        registerClass(clazz.getSimpleName(), clazz);
    }

    protected void registerClass(String name, Class<?> clazz) throws ScriptException {
        ScriptEngine engine = getNashornEngine();
        Object value = engine.eval("Java.type('"+clazz.getName()+"');");
        sem.put(name, value);
    }

    /**
     * Extract and put necessary variables needed for the Executors to work properly. For Bukkit API for example,
     * you will have to extract 'player' variable manually for inventory events as Player instance is not saved in
     * the field of Inventory evnet classes.
     * @param variables the local variable map.
     * @param e the context.
     */
    protected abstract void extractCustomVariables(Map<String, Object> variables, Object e);

    public AbstractJavascriptBasedManager(TriggerReactor plugin) throws ScriptException {
        super(plugin);

        instance = this;
        initScriptEngine();
    }

}