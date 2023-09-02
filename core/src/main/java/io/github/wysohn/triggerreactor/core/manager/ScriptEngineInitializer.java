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

package io.github.wysohn.triggerreactor.core.manager;

import javax.script.ScriptException;
import java.util.Map;
import java.util.function.Function;

/**
 * This interface is used to initialize the ScriptEngineManager.
 * <p>
 * Before the end of the initialization, the ScriptEngineManager will be passed to
 * the class that implements this interface. This is a great place to add global variables or functions
 * that can be used in the script without manually importing them.
 */
public interface ScriptEngineInitializer {

    void initialize(Map<String, Object> bindings) throws ScriptException;

    ScriptEngineInitializer DEFAULT = sem -> sem.put("Char", (Function<String, Character>) t -> t.charAt(0));
}
