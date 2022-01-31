package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.components.DaggerScriptEditManagerTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.tools.script.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.script.ScriptEditorUser;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ScriptEditManagerTest {

    IThrowableHandler throwableHandler;

    @Before
    public void init(){
        throwableHandler = mock(IThrowableHandler.class);
    }

    @Test
    public void onChatSave() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "save"));
        assertFalse(manager.isEditing(user));

        verify(editor).save();
    }

    @Test
    public void onChatExit() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "exit"));
        assertTrue(manager.isDoubleChecking(user));
        assertTrue(manager.isEditing(user));

        assertTrue(manager.onChat(user, editor, "exit"));
        assertFalse(manager.isDoubleChecking(user));
        assertFalse(manager.isEditing(user));

        verify(editor, never()).save();
    }

    @Test
    public void onChatInsertLine() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "il"));
        assertTrue(manager.isEditing(user));

        verify(editor).insertNewLine();
    }

    @Test
    public void onChatDeleteLine() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "dl"));
        assertTrue(manager.isEditing(user));

        verify(editor).deleteLine();
    }

    @Test
    public void onChatUp() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "u"));
        assertTrue(manager.isEditing(user));

        verify(editor).up(eq(1));
    }

    @Test
    public void onChatUpMultiline() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "u 23"));
        assertTrue(manager.isEditing(user));

        verify(editor).up(eq(23));
    }

    @Test
    public void onChatDown() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "d"));
        assertTrue(manager.isEditing(user));

        verify(editor).down(eq(1));
    }

    @Test
    public void onChatDownMultiline() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.onChat(user, editor, "d 23"));
        assertTrue(manager.isEditing(user));

        verify(editor).down(eq(23));
    }

    @Test
    public void onChatInput() throws ScriptException, IOException {
        ScriptEditManager manager = DaggerScriptEditManagerTestComponent.builder()
                .throwableHandler(throwableHandler)
                .build()
                .getScriptEditManager();

        Consumer<String> handler = mock(Consumer.class);
        ICommandSender sender = mock(ICommandSender.class);
        ScriptEditor editor = mock(ScriptEditor.class);
        ScriptEditorUser user = new ScriptEditorUser(sender);

        manager.startEdit(sender, "title", "content", handler, false);
        assertTrue(manager.isEditing(user));
        assertTrue(manager.onChat(user, editor, "some random input"));

        verify(editor).input(eq("some random input"));
    }
}