package io.github.wysohn.triggerreactor.core.main;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExceptionHandleTest {

    private IPluginManagement pluginManagement;
    private TaskSupervisor taskSupervisor;

    private IExceptionHandle handle;

    @Before
    public void setUp() throws Exception {
        pluginManagement = mock(IPluginManagement.class);
        taskSupervisor = mock(TaskSupervisor.class);

        handle = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IPluginManagement.class).toInstance(pluginManagement);
                        bind(TaskSupervisor.class).toInstance(taskSupervisor);
                    }
                }
        ).getInstance(ExceptionHandle.class);
    }

    private void createException(int level) {
        if (level > 0)
            try {
                createException(level - 1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            throw new RuntimeException("root");
    }

    @Test
    public void handleException() {
        // Arrange
        ICommandSender sender = mock(ICommandSender.class);

        when(sender.hasPermissionToSeeExceptions()).thenReturn(true);
        when(pluginManagement.getConsoleSender()).thenReturn(sender);

        doAnswer(invocationOnMock -> {
            Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(taskSupervisor).runTask(any());

        // Act
        try {
            createException(5);
        } catch (Exception e) {
            handle.handleException(null, e);
        }

        // Assert
        verify(sender, times(2 + 2 * 6)).sendMessage(anyString());
    }

    @Test
    public void testHandleException() {
        // Arrange
        IPlayer sender = mock(IPlayer.class);

        when(sender.hasPermissionToSeeExceptions()).thenReturn(true);
        when(pluginManagement.extractPlayerFromContext(any())).thenReturn(sender);

        doAnswer(invocationOnMock -> {
            Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(taskSupervisor).runTask(any());

        // Act
        try {
            createException(5);
        } catch (Exception e) {
            handle.handleException((Object) null, e);
        }

        // Assert
        verify(sender, times(2 + 2 * 6)).sendMessage(anyString());
    }
}