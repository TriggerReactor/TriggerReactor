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
package io.github.wysohn.triggerreactor.sponge.manager;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.sponge.tools.TextUtil;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.ScriptEditorUser;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;

public class ScriptEditManager extends AbstractScriptEditManager {
    private final Map<ScriptEditorUser, ScriptEditor> editings = new HashMap<>();
    private final Set<ScriptEditorUser> viewingUsage = new HashSet<>();
    private final Set<ScriptEditorUser> exitDoublecheck = new HashSet<>();

    public ScriptEditManager(TriggerReactorCore plugin) {
        super(plugin);
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e, @First Player sender) {
        SpongeScriptEditorUser editorUser = new SpongeScriptEditorUser(sender);

        if (editings.containsKey(editorUser)) {
            e.setCancelled(true);
            ScriptEditor editor = editings.get(editorUser);

            if (viewingUsage.remove(editorUser)) {
                editor.printScript(editorUser);
                return;
            }

            Text message = e.getRawMessage();
            String arg1 = message.toPlainSingle();

            if (arg1.equals("save")) {
                try {
                    editor.save();
                } catch (IOException | ScriptException ex) {
                    plugin.handleException(e, ex);
                } finally {
                    editings.remove(editorUser);
                    editorUser.sendMessage("&aSaved!");
                }
            } else if (arg1.equals("exit")) {
                if (exitDoublecheck.remove(editorUser)) {
                    editings.remove(editorUser);
                    editorUser.sendMessage("&7Done");
                } else {
                    exitDoublecheck.add(editorUser);
                    editorUser.sendMessage("&6Are you sure to exit? &cUnsaved data will be all discared! "
                            + "&dType &6exit &done more time to confirm.");
                }
                return;
            } else if (arg1.equals("il")) {
                editor.insertNewLine();
            } else if (arg1.equals("dl")) {
                editor.deleteLine();
            } else if (arg1.length() > 0 && arg1.charAt(0) == 'u') {
                String[] split = arg1.split(" ");

                int lines = 1;
                try {
                    lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                } catch (NumberFormatException ex) {
                    plugin.handleException(e, ex);
                }

                editor.up(lines);
            } else if (arg1.length() > 0 && arg1.charAt(0) == 'd') {
                String[] split = arg1.split(" ");

                int lines = 1;
                try {
                    lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                } catch (NumberFormatException ex) {
                    plugin.handleException(e, ex);
                }

                editor.down(lines);
            } else {
                if (!exitDoublecheck.remove(editorUser)) {
                    editor.intput(arg1.replaceAll("\\^", " "));
                }
            }

            editor.printScript(editorUser);
        } else {
            MessageChannel channel = e.getOriginalChannel();
            Collection<MessageReceiver> copy = new LinkedList<>();

            for (Iterator<MessageReceiver> iter = channel.getMembers().iterator(); iter.hasNext(); ) {
                MessageReceiver receiver = iter.next();
                if (receiver instanceof Player) {
                    SpongeScriptEditorUser receivingUser = new SpongeScriptEditorUser((Player) receiver);
                    if (!editings.containsKey(receivingUser))
                        copy.add(receiver);
                }
            }

            e.setChannel(MessageChannel.fixed(copy));
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect e, @First Player receiver) {
        SpongeScriptEditorUser editorUser = new SpongeScriptEditorUser(receiver);

        editings.remove(editorUser);
        viewingUsage.remove(editorUser);
        exitDoublecheck.remove(editorUser);
    }

    @Listener
    public void onTab(TabCompleteEvent e, @First Player player) {
        SpongeScriptEditorUser editorUser = new SpongeScriptEditorUser(player);

        if (!editings.containsKey(editorUser))
            return;
        ScriptEditor editor = editings.get(editorUser);

        e.getTabCompletions().clear();
        e.getTabCompletions().add(parseSpaceToMarker(editor.getLine()));
    }

    @Override
    public void reload() {

    }

    @Override
    public void saveAll() {

    }

    @Override
    public void startEdit(ICommandSender sender, String title, String script, SaveHandler saveHandler) {
        SpongeScriptEditorUser editorUser = new SpongeScriptEditorUser(sender.get());

        if (editings.containsKey(editorUser))
            return;

        ScriptEditor editor = new ScriptEditor(title, script, saveHandler);
        editings.put(editorUser, editor);

        editorUser.sendMessage(ScriptEditor.USAGE);
        viewingUsage.add(editorUser);
    }

    private class SpongeScriptEditorUser implements ScriptEditorUser {
        private final Player receiver;

        public SpongeScriptEditorUser(Player receiver) {
            this.receiver = receiver;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (!(obj instanceof SpongeScriptEditorUser))
                return false;

            SpongeScriptEditorUser other = (SpongeScriptEditorUser) obj;
            return receiver.getUniqueId().equals(other.receiver.getUniqueId());
        }

        @Override
        public int hashCode() {
            return receiver.getUniqueId().hashCode();
        }

        @Override
        public void sendMessage(String rawMessage) {
            receiver.sendMessage(TextUtil.colorStringToText(rawMessage));
        }
    }
}