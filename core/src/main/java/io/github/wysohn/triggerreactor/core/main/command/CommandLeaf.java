package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.Queue;
import java.util.function.BiFunction;

public class CommandLeaf implements TriggerCommand{
    final String command;
    final Usage usage;
    final BiFunction<ICommandSender, Queue<String>, Boolean> argsFn;

    public CommandLeaf(String command, Usage usage, BiFunction<ICommandSender, Queue<String>, Boolean> argsFn) {
        this.command = command;
        this.usage = usage;
        this.argsFn = argsFn;
    }

    @Override
    public boolean onCommand(ICommandSender sender, Queue<String> args) {
        if(args.isEmpty() || !command.equals(args.peek()))
            return false;
        args.poll();

        return argsFn.apply(sender, args);
    }

    @Override
    public void printUsage(ICommandSender sender, int depth) {
        usage.printUsage(sender);
    }
}
