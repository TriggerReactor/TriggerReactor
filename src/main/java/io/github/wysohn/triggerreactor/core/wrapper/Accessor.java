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
package io.github.wysohn.triggerreactor.core.wrapper;

import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class Accessor {
    public final Object targetParent;
    public final String targetName;
    public Accessor(Object targetParent, String targetName) {
        this.targetParent = targetParent;
        this.targetName = targetName;
    }

    public Object getTargetParent(){
        return targetParent;
    }

    public Object evaluateTarget() throws NoSuchFieldException, IllegalArgumentException{
        return ReflectionUtil.getField(targetParent, targetName);
    }

    public void setTargetValue(Object value) throws NoSuchFieldException, IllegalArgumentException{
        ReflectionUtil.setField(targetParent, targetName, value);
    }

    @Override
    public String toString() {
        return targetParent+"."+targetName;
    }


}
