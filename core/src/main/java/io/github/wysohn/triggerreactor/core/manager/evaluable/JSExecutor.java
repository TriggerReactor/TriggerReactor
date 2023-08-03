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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;

public class JSExecutor extends Evaluable<Integer> implements Executor {
//    @Inject
//    private JSExecutor(@Assisted String executorName,
//                       @Assisted ScriptEngine engine,
//                       @Assisted File file) throws ScriptException, IOException {
//        this(executorName, engine, new FileInputStream(file));
//    }

    @Inject
    private JSExecutor(@Assisted String executorName,
                       @Assisted ScriptEngine engine,
                       @Assisted InputStream file) throws ScriptException, IOException {
        super("#", "Executors", executorName, FileUtil.readFromStream(file), engine);
    }
}
