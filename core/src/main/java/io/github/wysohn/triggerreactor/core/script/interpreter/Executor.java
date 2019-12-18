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

public abstract class Executor extends SynchronizableTask {
    public static final int STOP = 0;
    public static final int WAIT = 1;
    public static final int BREAK = 2;
    public static final int CONTINUE = 3;

    /**
     * @param timing  the parent timing instance. Extend timing using this instance, not creating new one. Can be null.
     * @param context the context where the Executor was started
     * @param vars    variables to be used in the Executor script
     * @param args    arguments passed from the interpreted code
     * @return usually null; return code to intercept execution
     * @throws Exception
     */
    protected abstract Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                       Object... args) throws Exception;
}
