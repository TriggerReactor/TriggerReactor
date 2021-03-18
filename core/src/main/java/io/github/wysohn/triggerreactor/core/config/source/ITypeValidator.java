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

package io.github.wysohn.triggerreactor.core.config.source;

import java.util.Collection;

public interface ITypeValidator {

    ITypeValidator DEFAULT = obj -> {
        if (obj == null)
            return true;

        return obj instanceof String
                || obj instanceof Number
                || obj instanceof Boolean
                || obj instanceof Collection
                || obj.getClass().isArray();
    };

    /**
     * Validate if the given object is serializable. The parent method isSerializable must be
     * called after invoking itself to maintain the proper order. Chain will be disconnected
     * if parent method is not invoked.
     * <p>
     * ex)
     * <p>
     * if([something])
     * <p>
     * return true;
     * <p>
     * else
     * <p>
     * return super.{@link #isSerializable(Object)}
     *
     * @param obj obj to check
     * @return true if serializable type; false otherwise
     */
    boolean isSerializable(Object obj);
}
