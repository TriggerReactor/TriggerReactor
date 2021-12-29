package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import org.junit.Test;

import java.util.Queue;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommandTest {
    @Test
    public void test() {
        TriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .leaf("cmd", "/trg cmd <name> [script]", (sender, args) -> {
                    String name = args.poll();
                    if (name == null)
                        return false;

                    StringBuilder script = new StringBuilder();
                    while (!args.isEmpty()){
                        script.append(args.poll());
                        script.append(' ');
                    }

                    sender.sendMessage(script.toString());
                    return true;
                })
                .build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = TriggerCommand.toQueue("cmd myCommand #MESSAGE 1 + 3 + 4;");

        assertTrue(command.onCommand(sender, args));
        verify(sender).sendMessage("#MESSAGE 1 + 3 + 4; ");
    }
}