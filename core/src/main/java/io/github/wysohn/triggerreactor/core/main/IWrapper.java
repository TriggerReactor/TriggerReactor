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

package io.github.wysohn.triggerreactor.core.main;


import io.github.wysohn.triggerreactor.core.bridge.IMinecraftObject;

public interface IWrapper {
    /**
     * Wrap an arbitrary object that is specific to the sub-projects
     *
     * @param object the object only exist in the sub-project
     * @param <T>    type to be converted
     * @return the wrapped object
     */
    <T extends IMinecraftObject> T wrap(Object object);
}
