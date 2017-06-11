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
package io.github.wysohn.triggerreactor.manager;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import io.github.wysohn.triggerreactor.tools.prompts.EditingPrompt;
import io.github.wysohn.triggerreactor.tools.prompts.UsagePrompt;

public class ScriptEditManager extends Manager implements ConversationAbandonedListener{
	public ScriptEditManager(TriggerReactor plugin) {
	    super(plugin);
	}

	public void startEdit(Conversable sender, String title, String script, SaveHandler saveHandler){
		ConversationFactory factory = new ConversationFactory(plugin);

		EditingPrompt prompt = new EditingPrompt(plugin, sender, new ScriptEditor(title, script, saveHandler));
		Conversation conv = factory.thatExcludesNonPlayersWithMessage("Sorry, this is in-game only feature!")
				.withFirstPrompt(new UsagePrompt(prompt))
				.addConversationAbandonedListener(this)
				.buildConversation(sender);
		conv.getContext().setSessionData("edit", prompt);

		conv.begin();
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent arg0) {

	}

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll(){

    }
}