package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.components.CommandHandlerTestComponent;
import io.github.wysohn.triggerreactor.components.DaggerCommandHandlerTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CommandHandlerTest {

    CommandHandlerTestComponent component;
    private ITriggerCommand command;
    private IPluginLifecycleController lifecycleController;

    @Before
    public void setUp() throws Exception {
        command = mock(ITriggerCommand.class);
        lifecycleController = mock(IPluginLifecycleController.class);

        component = DaggerCommandHandlerTestComponent.builder()
                .commandName("test")
                .permission("test.permission")
                .triggerCommand(command)
                .pluginLifecycle(lifecycleController)
                .build();
    }

    @Test
    public void onCommandNoPermission() {
        ICommandSender sender = mock(ICommandSender.class);

        CommandHandler commandHandler = component.getCommandHandler();

        commandHandler.onCommand(sender, "test", new String[]{"test"});

        verify(command, never()).onCommand(any(), any());
    }

    @Test
    public void onCommandWithPermission() {
        ICommandSender sender = mock(ICommandSender.class);
        when(sender.hasPermission("test.permission")).thenReturn(true);
        when(lifecycleController.isEnabled()).thenReturn(true);

        CommandHandler commandHandler = component.getCommandHandler();

        commandHandler.onCommand(sender, "test", new String[]{"test"});

        verify(command).onCommand(any(), any());
    }

    @Test
    public void onTabCompleteNoPermission() {
        ICommandSender sender = mock(ICommandSender.class);

        CommandHandler commandHandler = component.getCommandHandler();

        List<String> result = commandHandler.onTabComplete(sender, new String[]{"test"});

        assertEquals(1, result.size());
        assertEquals("permission denied.", result.get(0));
    }

    @Test
    public void onTabCompleteWithPermission() {
        ICommandSender sender = mock(ICommandSender.class);
        when(sender.hasPermission("test.permission")).thenReturn(true);
        when(lifecycleController.isEnabled()).thenReturn(true);

        CommandHandler commandHandler = component.getCommandHandler();

        List<String> result = commandHandler.onTabComplete(sender, new String[]{"test"});

        verify(command).onTab(any());
    }
}