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

package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.ProvidesIntoSet;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;

import javax.inject.Named;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScriptEngineModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    public ScriptEngineManager provideScriptEngineManager(@Named("PluginClassLoader") ClassLoader classLoader) {
        return new ScriptEngineManager(classLoader);
    }

    @Provides
    public IScriptEngineGateway provideScriptEngineGateway(ScriptEngineManager sem) {
        return new IScriptEngineGateway() {
            @Override
            public ScriptEngine getEngine() {
                ScriptEngine engine = sem.getEngineByName("graal.js");
                if (engine != null) {
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("polyglot.js.allowAllAccess", true);
                    return engine;
                }

                engine = sem.getEngineByName("JavaScript");
                if (engine != null) {
                    return engine;
                }

                throw new RuntimeException("No java script engine was available. If you are using Java version above 11, " +
                        "the stock Java does not contain the java script engine as it used to be. "
                        + "Install GraalVM instead of "
                        +
                        "the stock Java, or you have to download third-party plugin, such as "
                        + "JShader.");
            }
        };
    }

    @ProvidesIntoSet
    public ScriptEngineInitializer provideJavaScriptInitializer() {
        return ScriptEngineInitializer.DEFAULT;
    }

    @ProvidesIntoSet
    public ScriptEngineInitializer provideInjectorToJavaScriptInitializer(Injector injector) {
        return (sem) -> sem.put("injector", injector);
    }
}
