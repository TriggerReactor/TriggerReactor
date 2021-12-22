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
package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.tools.prompts.EditingPrompt;
import io.github.wysohn.triggerreactor.bukkit.tools.prompts.UsagePrompt;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class ScriptEditManager extends AbstractScriptEditManager implements ConversationAbandonedListener {
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;

    @Inject
    public ScriptEditManager() {

    }

    @Override
    public void startEdit(ICommandSender sender, String title, String script, Consumer<String> consumer) {
        ConversationFactory factory = new ConversationFactory((Plugin) pluginInstance);

        EditingPrompt prompt = new EditingPrompt((Plugin) pluginInstance, sender.get(), new ScriptEditor(title, script, consumer));
        Conversation conv = factory.thatExcludesNonPlayersWithMessage("Sorry, this is in-game only feature!")
                .withFirstPrompt(new UsagePrompt(prompt))
                .addConversationAbandonedListener(this)
                .buildConversation(sender.get());
        conv.getContext().setSessionData("edit", prompt);

        conv.begin();
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent arg0) {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void saveAll() {

    }
}