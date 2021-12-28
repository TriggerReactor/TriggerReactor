package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import io.github.wysohn.triggerreactor.tools.script.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.script.ScriptEditorUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.inject.Inject;
import java.util.stream.Collectors;

public final class ScriptEditListener extends AbstractBukkitListener {
    @Inject
    ScriptEditManager scriptEditManager;
    @Inject
    IWrapper wrapper;

    @Inject
    ScriptEditListener() {

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        IPlayer player = wrapper.wrap(e.getPlayer());
        ScriptEditorUser editorUser = new ScriptEditorUser(player);

        ScriptEditor editor = scriptEditManager.getEditor(editorUser);
        if (editor != null) {
            e.setCancelled(true);

            scriptEditManager.onChat(editorUser, editor, e.getMessage());
        } else {
            // send message to only who is not editing
            scriptEditManager.getEditingUsers()
                    .stream()
                    .map(IScriptObject::get)
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .collect(Collectors.toList())
                    .forEach(e.getRecipients()::remove);
        }
    }
}
