package io.github.wysohn.triggerreactor.tools.prompts;

import java.io.IOException;

import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.plugin.Plugin;

import io.github.wysohn.triggerreactor.tools.ScriptEditor;

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

			if(postWork != null)
				postWork.run();

			HandlerList.unregisterAll(this);
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
		} else {
			editor.intput(arg1);
		}

		//editor.printScript(arg0.getForWhom());
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
				editor.printScript(arg0.getForWhom());
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
}
