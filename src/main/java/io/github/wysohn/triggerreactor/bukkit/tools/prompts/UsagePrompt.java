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
package io.github.wysohn.triggerreactor.bukkit.tools.prompts;

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
                + "any time to escape from the edit mode. If the code is too long, you can go up or down by typing "
                + ChatColor.GOLD + "u " + ChatColor.LIGHT_PURPLE + "or " + ChatColor.GOLD + "d" + ChatColor.LIGHT_PURPLE
                + ". When you are doing so, you can provide number of "
                + "lines to skip. (for example, d 10 will move down 10 lines) If you don't provide the number, the default value 1 will "
                + "be used instead. " + ChatColor.GOLD + "il" + ChatColor.LIGHT_PURPLE + " to insert a new line and "
                + ChatColor.GOLD + "dl" + ChatColor.LIGHT_PURPLE + " to delete line. You can also " + ChatColor.GOLD
                + "type anything and press Tab key" + ChatColor.LIGHT_PURPLE
                + " to copy the code where the cursor is pointing. Because of current design of Bukkit API,"
                + " you need to specify space with " + ChatColor.GOLD + "$ " + ChatColor.LIGHT_PURPLE
                + "to show the spaces." + " For example, you might can add four spaces before the code like this: "
                + ChatColor.GOLD + "$$$$#MESSAGE \"HI\"" + ChatColor.LIGHT_PURPLE + ChatColor.GREEN
                + " Type anything to continue...";
    }

}
