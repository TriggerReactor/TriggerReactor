package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CommandComposite implements ITriggerCommand {
    final String[] commands;
    final Usage usage;
    final boolean root;
    protected List<ITriggerCommand> children = new LinkedList<>();

    public CommandComposite(String command, Usage usage) {
        this(command, usage, false);
    }

    public CommandComposite(String[] commands, Usage usage) {
        this(commands, usage, false);
    }

    CommandComposite(String command, Usage usage, boolean root) {
        this(new String[]{command}, usage, root);
    }

    CommandComposite(String[] commands, Usage usage, boolean root) {
        this.commands = commands;
        this.usage = usage;
        this.root = root;

        Arrays.sort(commands);
    }

    void addChild(ITriggerCommand command){
        children.add(command);
    }


    @Override
    public boolean onCommand(ICommandSender sender, Queue<String> args) {
        // always delegate to children if root
        if (!root) {
            // or check if this composite is appropriate for the command
            if (Arrays.binarySearch(commands, args.peek()) < 0) {
                return false;
            }
            // consume the command
            args.poll();
        }

        // delegate to the children to handle the command
        for (ITriggerCommand child : children) {
            if(child.onCommand(sender, args))
                return true;
        }

        // no children consumed the queue, so probably it's an invalid command
        printUsage(sender, 2);
        return false;
    }

    @Override
    public void printUsage(ICommandSender sender, int depth) {
        if(depth <= 0)
            return;

        usage.printUsage(sender, INDENT * Math.max(0, 2 - depth));
        for (ITriggerCommand child : children) {
            child.printUsage(sender, depth - 1);
        }
    }
}
