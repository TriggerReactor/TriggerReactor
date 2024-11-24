/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Singleton
public class ScriptEditManager extends Manager {
    @Inject
    private IExceptionHandle pluginManagement;

    private final Map<ScriptEditor.User, ScriptEditor> editings = new HashMap<>();
    private final Set<ScriptEditor.User> viewingUsage = new HashSet<>();
    private final Set<ScriptEditor.User> exitDoublecheck = new HashSet<>();

    @Inject
    private ScriptEditManager() {
        super();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    public boolean isEditing(IPlayer player) {
        return editings.containsKey(new UserImpl(player));
    }

    /**
     * Start the script editor.
     *
     * @param sender      the sender to start editor
     * @param title       title to be shown on the editor
     * @param script      the actual script. It can be empty string
     * @param saveHandler the callback interface that allows you to save the script written by editor.
     */
    public void startEdit(ICommandSender sender, String title, String script, SaveHandler saveHandler) {
        UserImpl editorUser = new UserImpl((IPlayer) sender);

        if (editings.containsKey(editorUser))
            return;

        ScriptEditor editor = new ScriptEditor(title, script, saveHandler);
        editings.put(editorUser, editor);

        editorUser.sendMessage(ScriptEditor.USAGE);
        viewingUsage.add(editorUser);
    }

    /**
     * @param player
     * @param message
     * @return true if editing so that the message should be visible only to the player
     * ;false otherwise, so in this case, all other users' message should not be visible to the player
     * @deprecated event handler. Should be called from the listener or tests only.
     */
    @Deprecated
    public boolean onChat(IPlayer player, String message) {
        UserImpl editorUser = new UserImpl(player);

        if (editings.containsKey(editorUser)) {
            ScriptEditor editor = editings.get(editorUser);

            if (viewingUsage.remove(editorUser)) {
                editor.printScript(editorUser);
                return true;
            }

            if (message.equals("save")) {
                try {
                    editor.save();
                } catch (IOException | ScriptException ex) {
                    pluginManagement.handleException(null, ex);
                } finally {
                    editings.remove(editorUser);
                    editorUser.sendMessage("&aSaved!");
                }
            } else if (message.equals("exit")) {
                if (exitDoublecheck.remove(editorUser)) {
                    editings.remove(editorUser);
                    editorUser.sendMessage("&7Done");
                } else {
                    exitDoublecheck.add(editorUser);
                    editorUser.sendMessage("&6Are you sure to exit? &cUnsaved data will be all discared! "
                        + "&dType &6exit &done more time to confirm.");
                }
                return true;
            } else if (message.equals("il")) {
                editor.insertNewLine();
            } else if (message.equals("dl")) {
                editor.deleteLine();
            } else if (message.length() > 0 && message.charAt(0) == 'u' && message.charAt(1) == ' ') {
                String[] split = message.split(" ");

                    int lines = 1;
                    try {
                        lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                    } catch (NumberFormatException ex) {
                        pluginManagement.handleException(null, ex);
                    }

                    editor.up(lines);

            } else if (message.length() > 0 && message.charAt(0) == 'd' && message.charAt(1) == ' ' ) {
                String[] split = message.split(" ");

                    int lines = 1;
                    try {
                        lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                    } catch (NumberFormatException ex) {
                        pluginManagement.handleException(null, ex);
                    }
    
                    editor.down(lines);
            } else {
                if (!exitDoublecheck.remove(editorUser)) {
                    editor.intput(message.replaceAll("\\^", " "));
                }
            }

            editor.printScript(editorUser);
            return true;
        } else {
//            MessageChannel channel = e.getOriginalChannel();
//            Collection<MessageReceiver> copy = new LinkedList<>();
//
//            for (Iterator<MessageReceiver> iter = channel.getMembers().iterator(); iter.hasNext(); ) {
//                MessageReceiver receiver = iter.next();
//                if (receiver instanceof Player) {
//                    SpongeScriptEditorUser receivingUser = new SpongeScriptEditorUser((Player) receiver);
//                    if (!editings.containsKey(receivingUser))
//                        copy.add(receiver);
//                }
//            }
//
//            e.setChannel(MessageChannel.fixed(copy));
            return false;
        }
    }

    /**
     * @param player       player who pressed tab
     * @param lineConsumer callback to consume the current line of editor
     * @deprecated event handler. Should be called from the listener or tests only.
     */
    @Deprecated
    public void onTab(IPlayer player, Consumer<String> lineConsumer) {
        UserImpl editorUser = new UserImpl(player);

        if (!editings.containsKey(editorUser))
            return;
        ScriptEditor editor = editings.get(editorUser);

        lineConsumer.accept(parseSpaceToMarker(editor.getLine()));
    }

    public static String parseSpaceToMarker(String str) {
        if (str == null || "".equals(str))
            return null;

        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (str.charAt(index++) == ' ') {
            builder.append('^');
        }
        builder.append(str.substring(index - 1));

        return builder.toString();
    }

    /**
     * @param player player who left the server
     * @deprecated event handler. Should be called from the listener or tests only.
     */
    @Deprecated
    public void onQuit(IPlayer player) {
        UserImpl editorUser = new UserImpl(player);

        editings.remove(editorUser);
        viewingUsage.remove(editorUser);
        exitDoublecheck.remove(editorUser);
    }

    private static class UserImpl implements ScriptEditor.User {
        private final IPlayer receiver;

        public UserImpl(IPlayer receiver) {
            this.receiver = receiver;
        }

        @Override
        public void sendMessage(String rawMessage) {
            receiver.sendMessage(rawMessage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(receiver.getUniqueId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            UserImpl that = (UserImpl) o;
            return receiver.getUniqueId().equals(that.receiver.getUniqueId());
        }
    }
}
