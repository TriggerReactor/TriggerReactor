package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ScriptEditManagerTest {
    IExceptionHandle exceptionHandle;


    ScriptEditManager scriptEditManager;

    @Before
    public void setUp() throws Exception {
        exceptionHandle = mock(IExceptionHandle.class);

        scriptEditManager = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IExceptionHandle.class).toInstance(exceptionHandle);
                    }
                }
        ).getInstance(ScriptEditManager.class);
    }

    @Test
    public void initialize() {
        // arrange

        // act
        scriptEditManager.initialize();

        // assert
    }

    @Test
    public void reload() {
        // arrange

        // act
        scriptEditManager.reload();

        // assert
    }

    @Test
    public void shutdown() {
        // arrange

        // act
        scriptEditManager.shutdown();

        // assert
    }

    @Test
    public void startEdit() {
        // arrange
        UUID uuid = UUID.randomUUID();

        IPlayer sender = mock(IPlayer.class);
        String title = "title";
        String script = "script";
        ScriptEditor.SaveHandler saveHandler = mock(ScriptEditor.SaveHandler.class);

        when(sender.getUniqueId()).thenReturn(uuid);

        // act
        scriptEditManager.startEdit(sender, title, script, saveHandler);

        // assert
        assertTrue(scriptEditManager.isEditing(sender));
    }

    @Test
    public void onChat() {
        // arrange
        UUID uuid = UUID.randomUUID();

        IPlayer player = mock(IPlayer.class);
        ScriptEditor.SaveHandler saveHandler = mock(ScriptEditor.SaveHandler.class);

        when(player.getUniqueId()).thenReturn(uuid);

        // act
        boolean result1 = scriptEditManager.isEditing(player);
        scriptEditManager.startEdit(player, "title", "script", saveHandler);
        boolean result2 = scriptEditManager.isEditing(player);
        scriptEditManager.onChat(player, "any");
        scriptEditManager.onChat(player, "abcd");
        scriptEditManager.onChat(player, "il");
        scriptEditManager.onChat(player, "nextline");
        scriptEditManager.onChat(player, "u");
        scriptEditManager.onChat(player, "d");
        scriptEditManager.onChat(player, "dl");
        scriptEditManager.onChat(player, "bcde");
        scriptEditManager.onChat(player, "save");
        boolean result3 = scriptEditManager.isEditing(player);

        // assert
        assertFalse(result1);
        assertTrue(result2);
        assertFalse(result3);
        verify(saveHandler).onSave("bcde");
    }

    @Test
    public void onQuit() {
        // arrange
        UUID uuid = UUID.randomUUID();

        IPlayer player = mock(IPlayer.class);

        when(player.getUniqueId()).thenReturn(uuid);

        // act
        boolean result1 = scriptEditManager.isEditing(player);
        scriptEditManager.startEdit(player, "title", "script", mock(ScriptEditor.SaveHandler.class));
        boolean result2 = scriptEditManager.isEditing(player);
        scriptEditManager.onQuit(player);
        boolean result3 = scriptEditManager.isEditing(player);

        // assert
        assertFalse(result1);
        assertTrue(result2);
        assertFalse(result3);
    }
}