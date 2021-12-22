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
package io.github.wysohn.triggerreactor.tools;

import java.util.function.Predicate;

public class ValidationUtil {
    public static <T> void notNull(T obj) {
        if (obj == null)
            throw new RuntimeException("Value cannot be null.");
    }

    public static <T> void allNotNull(T... objs) {
        for (T obj : objs) {
            if (obj == null)
                throw new RuntimeException("At least one of the element in array is null.");
        }
    }

    public static <T> void assertTrue(T obj, Predicate<T> predicate, String message){
        if(!predicate.test(obj))
            throw new RuntimeException(message);
    }

    public static <T> void assertTrue(T obj, Predicate<T> predicate){
        assertTrue(obj, predicate, "Test fail.");
    }
}
