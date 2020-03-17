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
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public abstract class AbstractScriptEditManager extends Manager {

    public AbstractScriptEditManager(TriggerReactorCore plugin) {
        super(plugin);
    }

    /**
     * Start the script editor.
     *
     * @param sender      the sender to start editor
     * @param title       title to be shown on the editor
     * @param script      the actual script. It can be empty string
     * @param saveHandler the callback interface that allows you to save the script written by editor.
     */
    public abstract void startEdit(ICommandSender sender, String title, String script, SaveHandler saveHandler);

    public static String parseSpaceToMarker(String str) {
        if (str == null || str == "")
            return null;

        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (str.charAt(index++) == ' ') {
            builder.append('^');
        }
        builder.append(str.substring(index - 1));

        return builder.toString();
    }

//    public static void main(String[] ar) {
//        System.out.println(parseSpaceToMarker("        #MESSAGE pewpew"));
//    }
}