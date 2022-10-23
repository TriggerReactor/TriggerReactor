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

import io.github.wysohn.triggerreactor.core.script.validation.ValidationResult;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.Bindings;
import java.util.Map;

public interface IEvaluable {
    /**
     * Evaluate the Evaluable with given arguments.
     *
     * @param timing    Timings.Timing object to record the timings
     * @param variables variables to be used in the execution context
     * @param event     event object to be used in the execution context
     * @param args      arguments to be used in the execution context
     * @return result of the evaluation
     * @throws Exception
     */
    Object evaluate(Timings.Timing timing,
                    Map<String, Object> variables,
                    Object event,
                    Object... args) throws Exception;

    /**
     * Register validation specific to this Evaluable.
     * "validation" key in the Bindings will be used to retrieve the validation information.
     *
     * @param bindings current bindings for the execution context
     */
    default void registerValidationInfo(Bindings bindings) {
        //do nothing
    }

    /**
     * Validate whether the arguments are valid for this Evaluable.
     *
     * @param args arguments to be validated
     * @return ValidationResult; null if no validation is registered.
     */
    default ValidationResult validate(Object... args) {
        return null;
    }
}
