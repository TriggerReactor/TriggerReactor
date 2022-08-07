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
package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;

public interface Executor extends SynchronizableTask {
    int STOP = 0;
    int WAIT = 1;
    int BREAK = 2;
    int CONTINUE = 3;

    /**
     * @param timing  the parent timing instance. Extend timing using this instance, not creating new one. Can be null.
     * @param vars    variables to be used in the Executor script
     * @param context the context where the Executor was started
     * @param args    arguments passed from the interpreted code
     * @return usually null; return code to intercept execution
     * @throws Exception
     */
    Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                    Object... args) throws Exception;
}
