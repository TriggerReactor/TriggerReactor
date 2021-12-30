package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

public class CommandHandler {
    @Inject
    @Named("CommandName")
    String commandName;
    @Inject
    @Named("Permission")
    String permission;
    @Inject
    ITriggerCommand triggerCommand;
    @Inject
    IPluginLifecycleController pluginLifecycleController;

    @Inject
    CommandHandler() {

    }

    public boolean onCommand(ICommandSender sender, String command, String[] args) {
        if (command.equalsIgnoreCase(commandName)) {
            if (!sender.hasPermission(permission))
                return true;

            if (!pluginLifecycleController.isEnabled()) {
                sender.sendMessage("&cTriggerReactor is disabled. Check your latest.log to see why the plugin is not "
                        + "loaded properly. If there was an error while loading, please report it through github "
                        + "issue or our discord channel.");
                return true;
            }

            Queue<String> commandQueue = ITriggerCommand.toQueue(args);
            triggerCommand.onCommand(sender, commandQueue);
        }

        return true;
    }

    /**
     * @param args
     * @param indexFrom inclusive
     * @param indexTo   inclusive
     * @return
     */
    private String mergeArguments(String[] args, int indexFrom, int indexTo) {
        StringBuilder builder = new StringBuilder(args[indexFrom]);
        for (int i = indexFrom + 1; i <= indexTo; i++) {
            builder.append(" " + args[i]);
        }
        return builder.toString();
    }

    //only for /trg command
    public List<String> onTabComplete(ICommandSender sender, String[] args) {
//        if (!sender.hasPermission(permission))
//            return Collections.singletonList("permission denied.");
//
//        switch (args.length) {
//            case 1:
//                return filter(Arrays.asList("area", "click", "cmd", "command", "custom", "del", "delete", "help",
//                        "inventory", "item", "list", "reload", "repeat", "run", "call", "saveall", "search", "sudo",
//                        "synccustom", "timings", "variables", "version", "walk"), args[0]);
//            case 2:
//                switch (args[0].toLowerCase()) {
//                    case "area":
//                    case "a":
//                        List<String> names = triggerNames(areaManager);
//                        // /trg area toggle
//                        names.add("toggle");
//                        return filter(names, args[1]);
//                    case "cmd":
//                    case "command":
//                        return filter(triggerNames(cmdManager), args[1]);
//                    case "custom":
//                        //event list
//                        return filter(new ArrayList<String>(customManager.getAbbreviations()), args[1]);
//                    case "delete":
//                    case "del":
//                        return filter(Arrays.asList("cmd", "command", "custom", "vars", "variables"), args[1]);
//                    case "inventory":
//                    case "i":
//                        return filter(triggerNames(invManager), args[1]);
//                    case "item":
//                        return filter(Arrays.asList("lore", "title"), args[1]);
//                    case "repeat":
//                    case "r":
//                        return filter(triggerNames(repeatManager), args[1]);
//                    case "sudo":
//                        return null; //player selection
//                    case "synccustom":
//                        return filter(triggerNames(customManager), args[1]);
//                    case "timings":
//                        return filter(Arrays.asList("print", "toggle", "reset"), args[1]);
//                    case "call":
//                        return filter(triggerNames(namedTriggerManager), args[1]);
//                }
//            case 3:
//                switch (args[0].toLowerCase()) {
//                    case "area":
//                    case "a":
//                        if (!args[1].equalsIgnoreCase("toggle")) {
//                            return filter(Arrays.asList("create", "delete", "enter", "exit", "sync"), args[2]);
//                        }
//                        return EMPTY;
//                    case "command":
//                    case "cmd":
//                        return filter(Arrays.asList("aliases", "permission", "sync", "settab"), args[2]);
//                    case "custom":
//                        return filter(triggerNames(customManager), args[2]);
//                    case "delete":
//                    case "del":
//                        AbstractTriggerManager manager;
//                        switch (args[1]) {
//                            case "cmd":
//                            case "command":
//                                manager = cmdManager;
//                                break;
//                            case "custom":
//                                manager = customManager;
//                                break;
//                            //"vars" and "variables" also possible, but I won't be offering completions for these
//                            default:
//                                return EMPTY;
//                        }
//                        return filter(triggerNames(manager), args[2]);
//                    case "inventory":
//                    case "i":
//                        return filter(Arrays.asList("column", "create", "delete", "edit", "edititems", "item", "open"
//                                , "row", "settitle"), args[2]);
//                    case "item":
//                        if (args[1].equals("lore")) {
//                            return filter(Arrays.asList("add", "set", "remove"), args[2]);
//                        }
//                    case "repeat":
//                    case "r":
//                        return filter(Arrays.asList("autostart", "delete", "interval", "pause", "status", "toggle"),
//                                args[2]);
//                }
//            case 4:
//                switch (args[0].toLowerCase()) {
//                    case "inventory":
//                    case "i":
//                        if (args[2].equalsIgnoreCase("open")) {
//                            return null; //player selection
//                        }
//                        if (args[2].equalsIgnoreCase("create")) {
//                            return filter(Arrays.asList("9", "18", "27", "36", "45", "54"), args[3]);
//                        }
//                }
//        }
        return EMPTY;
    }

    //returns all strings in completions that start with prefix.
    private static List<String> filter(Collection<String> completions, String prefix) {
        prefix = prefix.trim().toUpperCase();
        List<String> filtered = new ArrayList<String>();
        for (String s : completions) {
            if (s.toUpperCase().startsWith(prefix)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    //get all trigger names for a manager
    private List<String> triggerNames(AbstractTriggerManager<? extends Trigger> manager) {
        List<String> names = new ArrayList<String>();
        for (Trigger trigger : manager.getAllTriggers()) {
            names.add(trigger.getInfo().getTriggerName());
        }
        return names;
    }

    @SuppressWarnings("serial")
    private static final List<Paragraph> HELP_PAGES = new ArrayList<Paragraph>() {{
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] walk[w] [...] &8- &7create a walk trigger.");
            sender.sendMessage("  &7/trg w #MESSAGE \"HEY YOU WALKED!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg w &7without extra parameters.");

            sender.sendMessage("&b/triggerreactor[trg] click[c] [...] &8- &7create a click trigger.");
            sender.sendMessage("  &7/trg c #MESSAGE \"HEY YOU CLICKED!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg c &7without extra parameters.");
        });
        add((sender) -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] command[cmd] <command name> [...] &8- &7create a command " + "trigger.");
            sender.sendMessage("  &7/trg cmd test #MESSAGE \"I'M test COMMAND!\"");
            sender.sendMessage("  &7To create lines of script, simply type &b/trg cmd <command name> &7without extra "
                    + "parameters.");
            sender.sendMessage("  &7To change sync/async mode, type &b/trg cmd <command name> sync&7.");
            sender.sendMessage("  &7- To set permissions for this command, type &b/trg cmd <command name> "
                    + "permission[p] x.y x.z interpreter.start(new InterpreterLocalContext(Timings.LIMBO), "
                    + "globalContext);y.y ...&7.");
            sender.sendMessage("  &7- To set aliases for this command, type &b/trg cmd <command name> aliases[a] some"
                    + " thing ..interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);.&7.");
            sender.sendMessage("    &6*&7Not providing any permission or aliases will remove them instead.");
            sender.sendMessage("  &7- To add tab-completer, type &b/trg cmd <command name> settab[tab] <a/b/c>:a,b,c "
                    + "<player>:$playerlist this,it,that");
            sender.sendMessage("    &6*&7The parameter has following format: hint:val1,val2,...");
            sender.sendMessage("    &6*&7Not providing any tab-completer will remove it instead.");
            sender.sendMessage("    &7Hint shows up as simple string when a user is about to type something, and "
                    + "values interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);will start"
                    + " to show up as a form of tab-completers as soon as their first characters interpreter.start"
                    + "(new InterpreterLocalContext(Timings.LIMBO), globalContext);matching with the characters typed"
                    + " by the user.");
            sender.sendMessage("    &7You may omit the hint, yet you cannot omit the values. To use only hint but no "
                    + "values, interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);edit the "
                    + "config file manually.");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] inventory[i] <inventory name> &8- &7Create an inventory "
                    + "trigger named interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);"
                    + "<inventory name>");
            sender.sendMessage("  &7/trg i to see more commands...");

            sender.sendMessage("&b/triggerreactor[trg] item &8- &7Item modification. Type it to see the list.");

            sender.sendMessage("&b/triggerreactor[trg] area[a] &8- &7Create an area trigger.");
            sender.sendMessage("  &7/trg a to see more commands...");

            sender.sendMessage("&b/triggerreactor[trg] repeat[r] &8- &7Create an repeating trigger.");
            sender.sendMessage("&b/triggerreactor[trg] version &8- &7Show the plugin version.");
            sender.sendMessage("  &7/trg r to see more commands...");
        });
        add((sender) -> {
            sender.sendMessage("&b/triggerreactor[trg] custom <event> <name> [...] &8- &7Create a custom trigger.");
            sender.sendMessage("  &7/trg custom onJoin Greet #BROADCAST \"Please welcome \"+player.getName()+\"!\"");
            sender.sendMessage("&b/triggerreactor[trg] synccustom[sync] <name> &8- &7Toggle Sync/Async mode of custom"
                    + " trigger <name>");
            sender.sendMessage("  &7/trg synccustom Greet");

            sender.sendMessage("&b/triggerreactor[trg] variables[vars] [...] &8- &7set global variables.");
            sender.sendMessage("  &7&cWarning - This command will delete the previous data associated with the key if"
                    + " exists.");
            sender.sendMessage("  &7/trg vars Location test &8- &7save current location into global variable 'test'");
            sender.sendMessage("  &7/trg vars Item gifts.item1 &8- &7save hand held item into global variable 'test'");
            sender.sendMessage("  &7/trg vars test 13.5 &8- &7save 13.5 into global variable 'test'");

            sender.sendMessage("&b/triggerreactor[trg] variables[vars] <variable name> &8- &7get the value saved in "
                    + "<variable interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);name>. "
                    + "null if nothing.");
        });
        add((sender) -> {
            sender.sendMessage(
                    "&b/triggerreactor[trg] run [...] &8- &7Run simple script now without making a trigger" + ".");
            sender.sendMessage("  &7/trg run #TP {\"MahPlace\"}");

            sender.sendMessage("&b/triggerreactor[trg] sudo <player> [...] &8- &7Run simple script now without making"
                    + " a trigger.");
            sender.sendMessage("  &7/trg sudo wysohn #TP {\"MahPlace\"}");

            sender.sendMessage(
                    "&b/triggerreactor[trg] call <named trigger> [codes ...] &8- &7Run Named Trigger " + "directly.");
            sender.sendMessage("  &7/trg call MyNamedTrigger abc = {\"MahPlace\"}");
            sender.sendMessage("  &7the last argument (codes ...) are just like any script, so you can imagine that a "
                    + "temporary trigger will be made, the codes will run, and then the Named Trigger will be"
                    + " called, just like how you do with #CALL. This can be useful if you have variables in "
                    + "the Named Trigger that has to be initialized.");
        });
        add((sender -> {
            sender.sendMessage("&b/triggerreactor[trg] delete[del] <type> <name> &8- &7Delete specific "
                    + "trigger/variable/etc.");
            sender.sendMessage("  &7/trg del vars test &8- &7delete the variable saved in 'test'");
            sender.sendMessage("  &7/trg del cmd test &8- &7delete the command trigger 'test'");
            sender.sendMessage("  &7/trg del custom Greet &8- &7delete the custom trigger 'Greet'");

            sender.sendMessage("&b/triggerreactor[trg] search &8- &7Show all trigger blocks in this chunk as glowing "
                    + "stones.");

            sender.sendMessage("&b/triggerreactor[trg] list [filter...] &8- &7List all triggers.");
            sender.sendMessage("  &7/trg list CommandTrigger some &8- &7Show results that contains 'CommandTrigger' "
                    + "and 'some'.");

            sender.sendMessage("&b/triggerreactor[trg] saveall &8- &7Save all scripts, variables, and settings.");

            sender.sendMessage("&b/triggerreactor[trg] reload &8- &7Reload all scripts, variables, and settings.");
        }));
        add((sender -> {
            sender.sendMessage("&b/triggerreactor[trg] timings toggle &8- &7turn on/off timings analysis. Also "
                    + "analysis will be interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);"
                    + "reset.");
            sender.sendMessage("&b/triggerreactor[trg] timings reset &8- &7turn on/off timings analysis. Also "
                    + "analysis will be interpreter.start(new InterpreterLocalContext(Timings.LIMBO), globalContext);"
                    + "reset.");
            sender.sendMessage("&b/triggerreactor[trg] timings print &8- &7Show analysis result.");
            sender.sendMessage(
                    "  &b/triggerreactor[trg] timings print xx &8- &7Save analysis to file named xx" + ".timings");
        }));
    }};
    private static final List<String> EMPTY = new ArrayList<String>();

    private interface Paragraph {
        void sendParagraph(ICommandSender sender);
    }
}
