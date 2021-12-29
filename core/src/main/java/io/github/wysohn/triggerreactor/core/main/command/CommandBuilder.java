package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CommandBuilder {
    CommandComposite composite;

    CommandBuilder(Usage usage){
        composite = new RootCommand(usage);
    }

    CommandBuilder(String command, String usage){
        this(new String[]{command}, new SimpleUsage(usage));
    }

    CommandBuilder(String[] commands, String usage){
        this(commands, new SimpleUsage(usage));
    }

    CommandBuilder(String command, Usage usage){
        this(new String[]{command}, usage);
    }

    CommandBuilder(String[] commands, Usage usage){
        composite = new CommandComposite(commands, usage);
    }

    /**
     * overloaded {@link #begin(Usage)}
     */
    public static CommandBuilder begin(String usage){
        return begin(new SimpleUsage(usage));
    }

    /**
     * Start building the command tree
     * @param usage the title to be shown when invalid command is provided.
     *              Usually this is where we can show the plugin description
     */
    public static CommandBuilder begin(Usage usage){
        return new CommandBuilder(usage);
    }

    /**
     * overloaded {@link #leaf(String[], Usage, BiFunction)}
     */
    public CommandBuilder leaf(String command, String usage, BiFunction<ICommandSender, Queue<String>, Boolean> argsFn){
        leaf(command, new SimpleUsage(usage), argsFn);
        return this;
    }

    /**
     * overloaded {@link #leaf(String[], Usage, BiFunction)}
     */
    public CommandBuilder leaf(String[] commands, String usage, BiFunction<ICommandSender, Queue<String>, Boolean> argsFn){
        leaf(commands, new SimpleUsage(usage), argsFn);
        return this;
    }

    /**
     * overloaded {@link #leaf(String[], Usage, BiFunction)}
     */
    public CommandBuilder leaf(String command, Usage usage, BiFunction<ICommandSender, Queue<String>, Boolean> argsFn){
        leaf(new String[]{command}, usage, argsFn);
        return this;
    }

    /**
     * Add a leaf command. This is where the command logic is handled as it's the end of the
     * command tree.
     * @param commands command
     * @param usage usage
     * @param argsFn the command execution logic. Returns true if successfully handled the command;
     *               false if something went wrong (will print usage)
     */
    public CommandBuilder leaf(String[] commands, Usage usage, BiFunction<ICommandSender, Queue<String>, Boolean> argsFn){
        this.composite.addChild(new CommandLeaf(commands, usage, argsFn));
        return this;
    }

    public CommandBuilder leaf(CommandLeaf leaf){
        this.composite.addChild(leaf);
        return this;
    }

    /**
     * overloaded {@link #composite(String[], Usage, Consumer)}
     */
    public CommandBuilder composite(String command, String usage, Consumer<CommandBuilder> fn){
        return composite(command, new SimpleUsage(usage), fn);
    }

    /**
     * overloaded {@link #composite(String[], Usage, Consumer)}
     */
    public CommandBuilder composite(String[] commands, String usage, Consumer<CommandBuilder> fn){
        return composite(commands, new SimpleUsage(usage), fn);
    }

    /**
     * overloaded {@link #composite(String[], Usage, Consumer)}
     */
    public CommandBuilder composite(String command, Usage usage, Consumer<CommandBuilder> fn) {
        CommandBuilder builder = new CommandBuilder(command, usage);
        fn.accept(builder);
        this.composite.addChild(builder.composite);
        return this;
    }

    /**
     * Create a composite, which you can add commands under a group. For example, if there are commands
     * /trg abc def and /trg abc iei, def and iei commands will belong to abc group. You will add a composite
     * 'abc', then add the leaves, 'def' and 'iei'.
     * @param commands the command. 'abc' in the analogy above
     * @param usage message to be shown before showing the usage of the children. It's like a title before showing
     *              the usage for all the leaves below this composite.
     * @param fn the provided builder. Can be nested further to make a deep chain of commands.
     */
    public CommandBuilder composite(String[] commands, Usage usage, Consumer<CommandBuilder> fn) {
        CommandBuilder builder = new CommandBuilder(commands, usage);
        fn.accept(builder);
        this.composite.addChild(builder.composite);
        return this;
    }

    public CommandBuilder composite(CommandComposite composite){
        this.composite.addChild(composite);
        return this;
    }

    public ITriggerCommand build(){
        return composite;
    }
}
