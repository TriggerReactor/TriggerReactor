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

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;

public class ScriptEngineFactoryAdapter implements ScriptEngineFactory {
    private final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();

    @Override
    public String getEngineName() {
        return factory.getEngineName();
    }

    @Override
    public String getEngineVersion() {
        return factory.getEngineVersion();
    }

    @Override
    public List<String> getExtensions() {
        return factory.getExtensions();
    }

    @Override
    public String getLanguageName() {
        return factory.getLanguageName();
    }

    @Override
    public String getLanguageVersion() {
        return factory.getLanguageVersion();
    }

    @Override
    public String getMethodCallSyntax(String obj, String method, String... args) {
        return factory.getMethodCallSyntax(obj, method, args);
    }

    @Override
    public List<String> getMimeTypes() {
        return factory.getMimeTypes();
    }

    @Override
    public List<String> getNames() {
        return factory.getNames();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return factory.getOutputStatement(toDisplay);
    }

    @Override
    public Object getParameter(String key) {
        return factory.getParameter(key);
    }

    @Override
    public String getProgram(String... statements) {
        return factory.getProgram(statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        // this method is used by ScriptEngineManager
        return factory.getScriptEngine("--no-deprecation-warning");
    }

    public ScriptEngine getScriptEngine(ClassLoader appLoader) {
        return factory.getScriptEngine(appLoader);
    }

    public ScriptEngine getScriptEngine(ClassFilter classFilter) {
        return factory.getScriptEngine(classFilter);
    }

    public ScriptEngine getScriptEngine(String... args) {
        return factory.getScriptEngine(args);
    }

    public ScriptEngine getScriptEngine(String[] args, ClassLoader appLoader) {
        return factory.getScriptEngine(args, appLoader);
    }

    public ScriptEngine getScriptEngine(String[] args,
                                        ClassLoader appLoader,
                                        ClassFilter classFilter) {
        return factory.getScriptEngine(args, appLoader, classFilter);
    }
}
