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
package io.github.wysohn.triggerreactor.bukkit.tools.prompts;

import java.io.IOException;

import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.ScriptEditorUser;

public class EditingPrompt implements Prompt, Listener {
	private final ScriptEditor editor;
	private Runnable postWork;
	private final Conversable sender;

	public EditingPrompt(Plugin plugin, Conversable sender, ScriptEditor editor, Runnable postWork) {
		this.sender = sender;
		this.editor = editor;
		this.postWork = postWork;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public EditingPrompt(Plugin plugin, Conversable sender, ScriptEditor editor){
		this(plugin, sender, editor, null);
	}

	@Override
	public Prompt acceptInput(ConversationContext arg0, String arg1) {
		if (arg1.equals("save")) {
			try {
				editor.save();
			} catch (IOException | ScriptException e) {
				return new ErrorPrompt(this, e.getMessage());
			}

			HandlerList.unregisterAll(this);

			if(postWork != null)
				postWork.run();

			return Prompt.END_OF_CONVERSATION;
		} else if (arg1.equals("exit")) {
			return new ExitConfirmPrompt(this, new Runnable(){
				@Override
				public void run() {
					HandlerList.unregisterAll(EditingPrompt.this);
				}
			});
		}else if (arg1.equals("il")) {
			editor.insertNewLine();
		}else if (arg1.equals("dl")) {
			editor.deleteLine();
		}else if (arg1.length() > 0 && arg1.charAt(0) == 'u') {
			String[] split = arg1.split(" ");

			int lines = 1;
			try{
				lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
			}catch(NumberFormatException e){
				return new ErrorPrompt(this, e.getMessage());
			}

			editor.up(lines);
		} else if (arg1.length() > 0 && arg1.charAt(0) == 'd') {
			String[] split = arg1.split(" ");

			int lines = 1;
			try{
				lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
			}catch(NumberFormatException e){
				return new ErrorPrompt(this, e.getMessage());
			}

			editor.down(lines);
		} else{
			editor.intput(arg1.replaceAll("\\^", " "));
		}
		return this;
	}

	@Override
	public boolean blocksForInput(ConversationContext arg0) {
		return true;
	}

	@Override
	public String getPromptText(ConversationContext arg0) {
		new Thread(new Runnable(){
			@Override
			public void run() {

				editor.printScript(new BukkitScriptEditorUser(arg0.getForWhom()));
			}
		}).start();
		return "";
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTab(PlayerChatTabCompleteEvent e){
		if(!e.getPlayer().equals(sender))
			return;

		e.getTabCompletions().clear();
		e.getTabCompletions().add(editor.getLine());
	}

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!e.getPlayer().equals(sender))
            return;

        HandlerList.unregisterAll(this);
    }

    private static class BukkitScriptEditorUser implements ScriptEditorUser{
        private final Conversable conv;

        public BukkitScriptEditorUser(Conversable conv) {
            super();
            this.conv = conv;
        }

        @Override
        public void sendMessage(String rawMessage) {
            conv.sendRawMessage(ChatColor.translateAlternateColorCodes('&', rawMessage));
        }

        @Override
        public int hashCode() {
            return conv.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return conv.equals(obj);
        }


    }
}
