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
    private static int getVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    public static ScriptEngine getScriptEngine() {
        int version = getVersion();
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        if(version < 9){
            return scriptEngineManager.getEngineByName("nashorn");
        } else if(System.getProperty("org.graalvm.home") != null){
            return scriptEngineManager.getEngineByName("graal.js");
        } else {
            // check if nashorn is installed externally
            ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");
            if(engine != null)
                return engine;

            throw new RuntimeException("You are using the Java version > 8, yet you are not using" +
                    " the graalVM. For Java version > 8, you are required to install and run your" +
                    " server with GraalVM as the stock JVM no longer support Nashorn javascript engine." +
                    " Or, if you really want to use stock JVM, you have to install the plugin that will" +
                    " load the Nashorn engine manually. This is one example but not necessarily has to be:" +
                    " https://www.spigotmc.org/resources/nashornjs-provider-and-cli.91204/");
        }
    }
}
