package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.*;
import java.util.stream.Collectors;

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

    void addChild(ITriggerCommand command) {
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
            if (child.onCommand(sender, args))
                return true;
        }

        // no children consumed the queue, so probably it's an invalid command
        printUsage(sender, 2);
        return false;
    }

    @Override
    public List<String> onTab(ListIterator<String> args) {
        if (root) {
            List<String> tabs = new LinkedList<>();
            for (ITriggerCommand child : children) {
                List<String> childTabs = child.onTab(args);
                if (childTabs == null)
                    continue;
                tabs.addAll(childTabs);
            }
            return tabs;
        }

        if (args.hasNext()) {
            String current = args.next();

            // it's not for this composite
            if (Arrays.binarySearch(commands, current) < -1) {
                args.previous();
                return null;
            }

            List<String> tabs;
            if (args.hasNext()) {
                tabs = new LinkedList<>();
                for (ITriggerCommand child : children) {
                    List<String> childTabs = child.onTab(args);
                    if (childTabs == null)
                        continue;
                    tabs.addAll(childTabs);
                }
            } else {
                // treat this composite as leaf node
                tabs = Arrays.stream(commands)
                        .filter(c -> c.startsWith(current))
                        .collect(Collectors.toCollection(LinkedList::new));
            }

            args.previous();
            return tabs;
        } else {
            return new LinkedList<>();
        }
    }

    @Override
    public void printUsage(ICommandSender sender, int depth) {
        if (depth <= 0)
            return;

        usage.printUsage(sender, INDENT * Math.max(0, 2 - depth));
        for (ITriggerCommand child : children) {
            child.printUsage(sender, depth - 1);
        }
    }
}
