package io.github.wysohn.triggerreactor.tools.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class ExitConfirmPrompt implements Prompt {
	Prompt fallback;
	Runnable postWork;
	public ExitConfirmPrompt(Prompt fallback, Runnable postWork) {
		this.fallback = fallback;
		this.postWork = postWork;
	}

	@Override
	public Prompt acceptInput(ConversationContext arg0, String arg1) {
		if(arg1.equalsIgnoreCase("yes")){
			return Prompt.END_OF_CONVERSATION;
		}else{
			return fallback;
		}
	}

	@Override
	public boolean blocksForInput(ConversationContext arg0) {
		return true;
	}

	@Override
	public String getPromptText(ConversationContext arg0) {
		return ChatColor.GOLD + "Are you sure to exit? " + ChatColor.RED + "Unsaved data will all be deleted! "
				+ ChatColor.GOLD + "Type " + ChatColor.GREEN + "yes " + ChatColor.GOLD + "to exit or type "
				+ ChatColor.RED + "anything " + ChatColor.BLUE + "to cancel.";
	}

}
