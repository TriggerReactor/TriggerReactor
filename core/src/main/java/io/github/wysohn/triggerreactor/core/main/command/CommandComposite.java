package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CommandComposite implements TriggerCommand{
    final String command;
    final Usage usage;
    final boolean root;
    protected List<TriggerCommand> children = new LinkedList<>();

    public CommandComposite(String command, Usage usage) {
        this(command, usage, false);
    }

    CommandComposite(String command, Usage usage, boolean root) {
        this.command = command;
        this.usage = usage;
        this.root = root;
    }

    void addChild(TriggerCommand command){
        children.add(command);
    }


    @Override
    public boolean onCommand(ICommandSender sender, Queue<String> args) {
        // always delegate to children if root
        if(!root){
            // or check if this composite is appropriate for the command
            if(!command.equals(args.peek())){
                return false;
            }
            // consume the command
            args.poll();
        }

        // delegate to the children to handle the command
        for (TriggerCommand child : children) {
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
        for (TriggerCommand child : children) {
            child.printUsage(sender, depth - 1);
        }
    }
}
