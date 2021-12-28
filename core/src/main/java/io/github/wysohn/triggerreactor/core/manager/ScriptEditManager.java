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
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.script.*;

import javax.inject.Inject;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScriptEditManager extends Manager implements IScriptCommandChain {
    @Inject
    IThrowableHandler throwableHandler;
    private final Map<ScriptEditorUser, ScriptEditor> editings = new HashMap<>();
    private final Set<ScriptEditorUser> viewingUsage = new HashSet<>();
    private final Set<ScriptEditorUser> exitDoublecheck = new HashSet<>();
    private final ScriptCommandChains commandChain = ScriptCommandChains.Builder.begin()
            .chain(new SimpleScriptCommandChain("save", (user, editor, command) -> {
                try {
                    editor.save();
                } catch (IOException | ScriptException ex) {
                    throwableHandler.handleException((ICommandSender) null, ex);
                } finally {
                    stopEditing(user);
                    user.sendMessage("&aSaved!");
                }
            }))
            .chain(new SimpleScriptCommandChain("exit", (user, editor, command) -> {
                if (isDoubleChecking(user)) {
                    endDoubleChecking(user);
                    user.sendMessage("&7Done");
                } else {
                    beginDoubleChecking(user);
                    user.sendMessage(
                            "&6Are you sure to exit? &cUnsaved data will be all discard! &dType &6exit &done more "
                                    + "time to confirm.");
                }
            }))
            .chain(new SimpleScriptCommandChain("il", (user, editor, command) -> editor.insertNewLine()))
            .chain(new SimpleScriptCommandChain("dl", (user, editor, command) -> editor.deleteLine()))
            .chain((user, editor, command) -> {
                if (command.length() <= 0 || command.charAt(0) != 'u')
                    return false;

                String[] split = command.split(" ");

                int lines = 1;
                try {
                    lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                } catch (NumberFormatException ex) {
                    throwableHandler.handleException((ICommandSender) null, ex);
                }

                editor.up(lines);
                return true;
            })
            .chain((user, editor, command) -> {
                if (command.length() <= 0 || command.charAt(0) != 'd')
                    return false;

                String[] split = command.split(" ");

                int lines = 1;
                try {
                    lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                } catch (NumberFormatException ex) {
                    throwableHandler.handleException((ICommandSender) null, ex);
                }

                editor.down(lines);
                return true;
            })
            .chain((user, editor, command) -> {
                if (!isDoubleChecking(user)) {
                    editor.intput(command.replaceAll("\\^", " "));
                }

                return true;
            })
            .build();

    @Override
    public boolean onChat(ScriptEditorUser user, ScriptEditor editor, String command) {
        ValidationUtil.notNull(user);
        ValidationUtil.notNull(editor);
        ValidationUtil.notNull(command);
        return commandChain.onChat(user, editor, command);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() throws RuntimeException {

    }

    @Override
    public void saveAll() {

    }

    /**
     * Start the script editor.
     *
     * @param sender      the sender to start editor
     * @param title       title to be shown on the editor
     * @param script      the actual script. It can be empty string
     * @param saveHandler the callback interface that allows you to save the script written by editor.
     */
    public void startEdit(ICommandSender sender, String title, String script, Consumer<String> saveHandler) {
        ScriptEditorUser editorUser = new ScriptEditorUser(sender);

        if (editings.containsKey(editorUser))
            return;

        ScriptEditor editor = new ScriptEditor(title, script, saveHandler);
        editings.put(editorUser, editor);

        editorUser.sendMessage(ScriptEditor.USAGE);
        viewingUsage.add(editorUser);
    }

    public boolean isEditing(ScriptEditorUser editorUser) {
        return editings.containsKey(editorUser);
    }

    public ScriptEditor getEditor(ScriptEditorUser editorUser) {
        return editings.get(editorUser);
    }

    public boolean isViewingUsage(ScriptEditorUser editorUser) {
        return viewingUsage.contains(editorUser);
    }

    public void stopViewingUsage(ScriptEditorUser editorUser) {
        viewingUsage.remove(editorUser);
    }

    public void stopEditing(ScriptEditorUser editorUser) {
        editings.remove(editorUser);
    }

    public boolean isDoubleChecking(ScriptEditorUser editorUser) {
        return exitDoublecheck.contains(editorUser);
    }

    public void endDoubleChecking(ScriptEditorUser editorUser) {
        exitDoublecheck.remove(editorUser);
    }

    public void beginDoubleChecking(ScriptEditorUser editorUser) {
        exitDoublecheck.add(editorUser);
    }

    public Collection<ICommandSender> getEditingUsers() {
        return editings.keySet()
                .stream()
                .map(ScriptEditorUser::getCommandSender)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public void beginEditing(ScriptEditorUser editorUser, ScriptEditor editor) {
        editings.put(editorUser, editor);
    }

    public void beginViewingUsage(ScriptEditorUser editorUser) {
        viewingUsage.add(editorUser);
    }

    public static String parseSpaceToMarker(String str) {
        if (str == null || str == "")
            return null;

        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (str.charAt(index++) == ' ') {
            builder.append('^');
        }
        builder.append(str.substring(index - 1));

        return builder.toString();
    }
}