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

import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class UsagePrompt implements Prompt {
    private final Prompt next;

    public UsagePrompt(Prompt next) {
        this.next = next;
    }

    @Override
    public String getPromptText(ConversationContext arg0) {
        return ChatColor.translateAlternateColorCodes('&', ScriptEditor.USAGE);
    }

    @Override
    public boolean blocksForInput(ConversationContext arg0) {
        return true;
    }

    @Override
    public Prompt acceptInput(ConversationContext arg0, String arg1) {
        return next;
    }

}
