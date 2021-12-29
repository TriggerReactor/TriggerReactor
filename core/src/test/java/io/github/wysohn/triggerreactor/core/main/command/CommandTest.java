package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import org.junit.Test;

import java.util.Queue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommandTest {
    @Test
    public void test() {
        TriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("cmd", "/trg cmd ddd <name> [script]",
                        ddd -> ddd.leaf("ddd", "/trg cmd ddd <name> [script]", (sender, args) -> {
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
                        }))
                .build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = TriggerCommand.toQueue("cmd ddd myCommand #MESSAGE 1 + 3 + 4;");

        assertTrue(command.onCommand(sender, args));
        verify(sender).sendMessage("#MESSAGE 1 + 3 + 4; ");
    }

    @Test
    public void testInvalidCommand1() {
        TriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---")
                .composite("cmd", "/trg cmd",
                        ddd -> ddd.leaf("ddd", "/trg cmd ddd <name> [script]", (sender, args) -> {
                            String name = args.poll();
                            if (name == null)
                                return false;

                            StringBuilder script = new StringBuilder();
                            while (!args.isEmpty()) {
                                script.append(args.poll());
                                script.append(' ');
                            }

                            sender.sendMessage(script.toString());
                            return true;
                        }))
                .leaf("test", "/trg test", (sender, strings) -> true)
                .build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = TriggerCommand.toQueue("cmdd myCommand #MESSAGE 1 + 3 + 4;");

        assertFalse(command.onCommand(sender, args));
        verify(sender).sendMessage("--- TriggerReactor ---");
        verify(sender).sendMessage("  /trg cmd");
        verify(sender).sendMessage("  /trg test");
    }

    @Test
    public void testInvalidCommand2() {
        TriggerCommand command = CommandBuilder.begin("--- TriggerReactor ---").composite("cmd", "/trg cmd", cmd -> {
            cmd.leaf("ddd", "/trg cmd ddd <name> [script]", (sender, args) -> {
                String name = args.poll();
                if (name == null)
                    return false;

                StringBuilder script = new StringBuilder();
                while (!args.isEmpty()) {
                    script.append(args.poll());
                    script.append(' ');
                }

                sender.sendMessage(script.toString());
                return true;
            }).leaf("eee", "/trg cmd eee hi usage", (sender, strings) -> true);
        }).build();

        ICommandSender sender = mock(ICommandSender.class);
        Queue<String> args = TriggerCommand.toQueue("cmd fff myCommand #MESSAGE 1 + 3 + 4;");

        assertFalse(command.onCommand(sender, args));
        verify(sender).sendMessage("/trg cmd");
        verify(sender).sendMessage("  /trg cmd ddd <name> [script]");
        verify(sender).sendMessage("  /trg cmd eee hi usage");
    }
}