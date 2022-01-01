package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.*;
import java.util.stream.Collectors;

public interface ITriggerCommand {
    /**
     * Handle the command. Each child command will consume the command queue
     * as much as they need and delegate the rest to its own child.
     * @param sender sender
     * @param args command queue split using the space as delimiter. NOTE that it also must contain
     *             the actual command in the top of the queue, so the root command can search for the
     *             actual command candidate.
     * @return true if consumed the queue; false otherwise
     */
    boolean onCommand(ICommandSender sender, Queue<String> args);

    /**
     * Handle tab completion. Each child command will consume the command queue
     * and delegate to child, and the child does the same, and so on and so forth,
     * until the queue is empty.
     *
     * @param args the arguments. Note that because this is tab completion, the argument
     *             at the bottom of the queue is partially filled in command.
     *
     *             Note: cursor must be moved to next position only by the children when entering the children,
     *             and it is moved back to the original position by the children before returning to the parent.
     * @return list of possible commands; it can be possibly null to show that the top of the queue
     * isn't match with the command node we are looking at.
     */
    List<String> onTab(ListIterator<String> args);

    /**
     * Print the usage to the sender.
     *
     * @param sender sender
     * @param depth depth of tree search to print the usage. Going under its children consumes
     *              one depth, and the delegation ends when the value reaches 0. For example, if it's 2,
     *              it will print the usage of itself, and its direct children, but not for the grandchildren
     *              and so on and so forth. If it were 1, then it would print usage of itself only. Nothing
     *              shows if depth is 0 or less.
     */
    void printUsage(ICommandSender sender, int depth);

    static Queue<String> toQueue(String args) {
        return toQueue(args, " ");
    }

    static Queue<String> toQueue(String args, String delimiter) {
        return toQueue(args.split(delimiter));
    }

    static Queue<String> toQueue(String[] args){
        return Arrays.stream(args).collect(Collectors.toCollection(LinkedList::new));
    }

    static List<String> toList(String... args){
        return Arrays.stream(args).collect(Collectors.toCollection(LinkedList::new));
    }

    static String consumeAllArguments(Queue<String> args){
        StringBuilder script = new StringBuilder();
        if(!args.isEmpty())
            script.append(args.poll());
        while (!args.isEmpty()){
            script.append(' ');
            script.append(args.poll());
        }

        return script.toString();
    }

    int INDENT = 2;
}
