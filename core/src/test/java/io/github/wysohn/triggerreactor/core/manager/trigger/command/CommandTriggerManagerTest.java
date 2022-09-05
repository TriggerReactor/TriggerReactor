package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CommandTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    ICommandHandler commandHandler;
    CommandTriggerLoader loader;
    CommandTriggerManager manager;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());
        when(core.getDataFolder()).thenReturn(folder.getRoot());

        commandHandler = mock(ICommandHandler.class);
        loader = mock(CommandTriggerLoader.class);
        manager = new CommandTriggerManager(core, commandHandler, loader);
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload();

        verify(commandHandler).register(eq("test"), any());
    }

    @Test
    public void reload2() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        ICommand command = mock(ICommand.class);
        when(commandHandler.register(eq("test"), any())).thenReturn(command);
        manager.reload();

        verify(commandHandler).register(eq("test"), any());
        verify(mockTrigger).setCommand(command);
    }

    @Test
    public void testReload() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload("test");
    }

    @Test
    public void remove() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CommandTrigger mockTrigger = mock(CommandTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload();
        manager.remove("test");

        verify(commandHandler).unregister("test");
        assertNull(manager.get("test"));
    }

    @Test
    public void addCommandTrigger() {
        when(commandHandler.register(eq("test"), any())).thenReturn(mock(ICommand.class));

        ICommandSender sender = mock(ICommandSender.class);
        manager.addCommandTrigger(sender, "test", "#MESSAGE \"test\"");

        verify(commandHandler).register(eq("test"), any());
        assertNotNull(manager.get("test"));
        verify(commandHandler).sync();
    }

    @Test
    public void createTempCommandTrigger() throws AbstractTriggerManager.TriggerInitFailedException {
        manager.createTempCommandTrigger("#MESSAGE \"test\"");
    }

    @Test
    public void reregisterCommand() {
        when(commandHandler.register(eq("test"), any())).thenReturn(mock(ICommand.class));

        ICommandSender sender = mock(ICommandSender.class);
        manager.addCommandTrigger(sender, "test", "#MESSAGE \"test\"");
        manager.reregisterCommand("test");

        verify(commandHandler).unregister("test");
        verify(commandHandler, times(2)).register(eq("test"), any());
        verify(commandHandler, times(2)).sync();
    }
}