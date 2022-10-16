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

package io.github.wysohn.triggerreactor.core.manager.evaluable;

import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JSPlaceholder extends Evaluable<Object> implements Placeholder {
    public JSPlaceholder(String placeholderName, ScriptEngine engine, File file) throws ScriptException,
            IOException {
        this(placeholderName, engine, new FileInputStream(file));
    }

    public JSPlaceholder(String placeholderName, ScriptEngine engine, InputStream file) throws ScriptException,
            IOException {
        super("$", "Placeholders", placeholderName, FileUtil.readFromStream(file), engine);
    }
}
