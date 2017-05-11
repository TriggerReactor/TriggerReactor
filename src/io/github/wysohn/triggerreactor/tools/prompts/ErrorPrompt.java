package io.github.wysohn.triggerreactor.tools.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class ErrorPrompt implements Prompt {
	private final Prompt returnPrompt;
	private final String errorMessage;

	public ErrorPrompt(Prompt returnPrompt, String errorMessage) {
		this.returnPrompt = returnPrompt;
		this.errorMessage = errorMessage;
	}

	@Override
	public Prompt acceptInput(ConversationContext arg0, String arg1) {
		// TODO Auto-generated method stub
		return returnPrompt;
	}

	@Override
	public boolean blocksForInput(ConversationContext arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getPromptText(ConversationContext arg0) {
		return errorMessage+ChatColor.GREEN+"   "+"Type anything to continue...";
	}

}
