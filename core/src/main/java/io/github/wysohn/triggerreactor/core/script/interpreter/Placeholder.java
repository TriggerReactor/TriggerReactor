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
package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.manager.IEvaluable;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;

public interface Placeholder extends IEvaluable, SynchronizableTask {
    /**
     * Replace this placeholder with appropriate value.
     *
     * @param timing  the parent timing instance. Extend timing using this instance, not creating new one. Can be null.
     * @param vars    variables that will be used in the Placeholder. Some may can be overridden.
     * @param context the context where placeholder was used. It's Event for Bukkit API.
     * @param args    arguments to be used
     * @return replaced value. Should be always primitive type. Can be null if something went wrong
     */
    Object evaluate(Timings.Timing timing,
                    Map<String, Object> vars,
                    Object context,
                    Object... args) throws Exception;
}
