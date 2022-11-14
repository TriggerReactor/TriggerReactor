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

package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;

public abstract class AbstractTaggedTriggerManager<T extends Trigger> extends AbstractTriggerManager<T> {
    public AbstractTaggedTriggerManager(File folder) {
        super(folder);
    }

    /**
     * Extract prefix part of the trigger name. '-' sign will work as deliminator.
     * If there is no deliminator found, the 0th value of returned array will be null.
     * If there are more than one deliminator in the name, only the first String right before the first deliminator
     * encountered will be extracted, and the rest will be treated as one name.
     * For example, "some-name" would yield ["some", "name"], "some-name-bah" would yield ["some", "name-bah"],
     * and "something" would yield [null, "something].
     *
     * @param rawTriggerName the raw trigger name which possibly contains the prefix. Should not be null.
     * @return the array containing split name. 0th value is prefix, and 1st value is the rest of the name. However,
     * 0th value will be null if no deliminator exist in the rawTriggerName.
     */
    protected static String[] extractPrefix(String rawTriggerName) {
        if (rawTriggerName.indexOf("@") < rawTriggerName.indexOf("-")) {
            return new String[]{null, rawTriggerName};
        }
        String[] split = rawTriggerName.split("-", 2);
        if (split.length < 2)
            return new String[]{null, rawTriggerName};

        return split;
    }

//    public static void main(String[] ar){
//        System.out.println(Arrays.toString(extractPrefix("some-name")));
//        System.out.println(Arrays.toString(extractPrefix("some-name-bah")));
//        System.out.println(Arrays.toString(extractPrefix("something")));
//    }
}
