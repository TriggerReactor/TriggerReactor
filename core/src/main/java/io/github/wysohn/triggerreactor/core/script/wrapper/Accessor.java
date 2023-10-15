/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.core.script.wrapper;

import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

import java.lang.reflect.Array;

public class Accessor {
    public final Object targetParent;
    public final Object target;

    public Accessor(Object targetParent, String targetName) {
        this.targetParent = targetParent;
        this.target = targetName;
    }

    public Accessor(Object array, Integer index) {
        this.targetParent = array;
        this.target = index;
    }

    public Object getTargetParent() {
        return targetParent;
    }

    public Object evaluateTarget() throws ArrayIndexOutOfBoundsException, NoSuchFieldException, IllegalArgumentException {
        if (targetParent.getClass().isArray()) {
            if (target instanceof Integer) {
                return Array.get(targetParent, (Integer) target);
            } else if (target instanceof String && ((String) target).equals("length")) {
                return Array.getLength(targetParent);
            }

            throw new IllegalArgumentException(target.getClass() + " is not a valid type for array operation.");
        } else if (targetParent instanceof Class) {
            return ReflectionUtil.getField((Class<?>) targetParent, (Object) null, (String) target);
        } else {
            return ReflectionUtil.getField(targetParent, (String) target);
        }
    }

    public void setTargetValue(Object value) throws NoSuchFieldException, IllegalArgumentException {
        if (targetParent.getClass().isArray()) {
            Array.set(targetParent, (Integer) target, value);
        } else {
            ReflectionUtil.setField(targetParent, (String) target, value);
        }
    }

    @Override
    public String toString() {
        if (targetParent.getClass().isArray()) {
            return targetParent + "[" + target + "]";
        } else {
            return targetParent + "." + target;
        }
    }

    public static ArrayIndexOutOfBoundsException outOfBoundsException(final Accessor accessor) {
        return new ArrayIndexOutOfBoundsException(accessor.target + " is out of bound for array! Size: " + Array.getLength(accessor.targetParent));
    }

    public static ArrayIndexOutOfBoundsException outOfBoundsException(final Accessor accessor, final Throwable cause) {
        final ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(accessor.target + " is out of bound for array! Size: " + Array.getLength(accessor.targetParent));
        e.initCause(cause);

        return e;
    }

}