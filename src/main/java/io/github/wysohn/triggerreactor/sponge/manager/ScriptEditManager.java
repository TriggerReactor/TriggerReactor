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
package io.github.wysohn.triggerreactor.sponge.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public class ScriptEditManager extends AbstractScriptEditManager{
	private final Map<ICommandSender, ScriptEditor> editings = new HashMap<>();

    public ScriptEditManager(TriggerReactor plugin) {
	    super(plugin);
	}

	@Override
    public void startEdit(ICommandSender sender, String title, String script, SaveHandler saveHandler){
	    if(editings.containsKey(sender))
	        return;

	    ScriptEditor editor = new ScriptEditor(title, script, saveHandler);
	    editings.put(sender, editor);
	}

	@Listener
	public void onChat(MessageChannelEvent.Chat e, @First MessageReceiver receiver){
	    if(!editings.containsKey(receiver))
	        return;
	    e.setCancelled(true);
	    ScriptEditor editor = editings.get(receiver);

        Text message = e.getRawMessage();
        String arg1 = message.toPlainSingle();

        if (arg1.equals("save")) {
            try {
                editor.save();
            } catch (IOException | ScriptException ex) {
                plugin.handleException(e, ex);
            }

            editings.remove(receiver);
        } else if (arg1.equals("exit")) {
            editings.remove(receiver);
        } else if (arg1.equals("il")) {
            editor.insertNewLine();
        } else if (arg1.equals("dl")) {
            editor.deleteLine();
        } else if (arg1.length() > 0 && arg1.charAt(0) == 'u') {
            String[] split = arg1.split(" ");

            int lines = 1;
            try {
                lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
            } catch (NumberFormatException ex) {
                plugin.handleException(e, ex);
            }

            editor.up(lines);
        } else if (arg1.length() > 0 && arg1.charAt(0) == 'd') {
            String[] split = arg1.split(" ");

            int lines = 1;
            try {
                lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
            } catch (NumberFormatException ex) {
                plugin.handleException(e, ex);
            }

            editor.down(lines);
        } else {
            editor.intput(arg1.replaceAll("\\$", " "));
        }
    }

	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect e, @First MessageReceiver receiver){
	    editings.remove(receiver);
	}

    @Override
    public void reload() {

    }

    @Override
    public void saveAll(){

    }
}