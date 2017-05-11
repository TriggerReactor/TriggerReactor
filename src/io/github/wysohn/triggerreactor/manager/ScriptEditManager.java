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
