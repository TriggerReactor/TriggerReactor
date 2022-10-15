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

package js;

import io.github.wysohn.triggerreactor.core.manager.JSExecutor;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ExecutorTest extends JsTest {
    public static final Map<String, Boolean> coverage = new TreeMap<>();

    private final JSExecutor executor;

    public ExecutorTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
        super(engine, name, "Executor", directories);
        executor = new JSExecutor(name, engine, stream);
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);
        executor.evaluate(Timings.LIMBO, varMap, null, args);
        return null;
    }

    @Override
    public int getOverload(Object... args) {
        return executor.validate(args).getOverload();
    }
}