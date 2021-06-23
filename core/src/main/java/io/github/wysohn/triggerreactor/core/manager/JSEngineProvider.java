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

package io.github.wysohn.triggerreactor.core.manager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSEngineProvider {
    public static ScriptEngine getScriptEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("graal.js");
        if(engine != null)
            return engine;

        engine = scriptEngineManager.getEngineByName("nashorn");
        if(engine != null)
            return engine;

        throw new RuntimeException("You are using the Java version > 11, yet you are not using" +
                " the graalVM. For Java version > 11, you are required to install and run your" +
                " server with GraalVM as the stock JVM no longer support Nashorn javascript engine." +
                " Or, if you really want to use stock JVM, you have to install the plugin that will" +
                " load the Nashorn engine manually. This is one example but not necessarily has to be:" +
                " https://www.spigotmc.org/resources/nashornjs-provider-and-cli.91204/");
    }
}
