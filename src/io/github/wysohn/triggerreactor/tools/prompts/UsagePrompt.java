package io.github.wysohn.triggerreactor.tools.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class UsagePrompt implements Prompt{
	private final Prompt next;

	public UsagePrompt(Prompt next) {
		this.next = next;
	}

	@Override
	public Prompt acceptInput(ConversationContext arg0, String arg1) {
		return next;
	}

	@Override
	public boolean blocksForInput(ConversationContext arg0) {
		return true;
	}

	@Override
	public String getPromptText(ConversationContext arg0) {
		return ChatColor.LIGHT_PURPLE
				+ "In edit mode, you cannot receieve any message from the other users. You can type " + ChatColor.GOLD
				+ "save " + ChatColor.LIGHT_PURPLE + "or " + ChatColor.GOLD + "exit " + ChatColor.LIGHT_PURPLE
				+ "any time" + "to escape from the edit mode. If the code is too long, you can go up or down by typing "
				+ ChatColor.GOLD + "u " + ChatColor.LIGHT_PURPLE + "or " + ChatColor.GOLD + "d" + ChatColor.LIGHT_PURPLE
				+ ". When you are" + "doing so, you can provide number of "
				+ "lines to skip. (for example, d 10 will move down 10 lines) If you don't provide the number, the default value 1 will"
				+ "be used instead. " + ChatColor.GOLD + "il" + ChatColor.LIGHT_PURPLE + " to insert a new line and "
				+ ChatColor.GOLD + "dl" + ChatColor.LIGHT_PURPLE + " to delete line. You can also " + ChatColor.GOLD
				+ "type anything and press Tab key" + ChatColor.LIGHT_PURPLE
				+ " to copy the code where the cursor is pointing." + ChatColor.GREEN + " Type anything to continue...";
	}

}
