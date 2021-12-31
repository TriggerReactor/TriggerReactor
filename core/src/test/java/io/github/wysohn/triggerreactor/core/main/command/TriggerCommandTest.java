package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.components.TriggerCommandTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TriggerCommandTest {
    TriggerCommandTestComponent component;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void printHierarchy(){
        TriggerCommand triggerCommand = new TriggerCommand();
        ITriggerCommand command = triggerCommand.createCommand();
        ICommandSender sender = mock(ICommandSender.class);
        triggerCommand.pluginLifecycleController = mock(IPluginLifecycleController.class);

        doAnswer(invocation -> {
            System.out.println((String) invocation.getArgument(0));
            return null;
        }).when(sender).sendMessage(anyString());

        command.printUsage(sender, 999);
    }

    @Test
    public void createCommand() {
    }
}