package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandExecutor;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.PredefinedTabCompleters;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BukkitCommand implements ICommand {
    private final PluginCommand command;

    public BukkitCommand(PluginCommand command) {
        this.command = command;
    }

    @Override
    public void setTabCompleterMap(Map<Integer, Set<ITabCompleter>> tabCompleterMap) {
        command.setTabCompleter((sender, command12, alias, args) -> {
            if(tabCompleterMap == null
                    || tabCompleterMap.get(args.length - 1) == null
                    || tabCompleterMap.get(args.length - 1).size() == 0){
                return new ArrayList<>();
            }
            Set<ITabCompleter> finalCompleters = new HashSet<>();
            List<String> finalProvideList = new ArrayList<>();

            ConditionTabCompleterIterator:
            for(ITabCompleter tc : tabCompleterMap.get(args.length - 1)){
                if(tc.hasConditionMap()){

                    ArgumentIterator:
                    for(int i = 0; i < args.length; i++){
                        if(!tc.hasCondition(i))
                            continue ArgumentIterator;

                        Pattern pt = tc.getCondition(i);
                        if(!pt.matcher(args[i]).matches()){
                            continue ConditionTabCompleterIterator;
                        }
                    }
                }
                finalCompleters.add(tc);
            }

            FinalTabCompletionIterator:
            for(ITabCompleter finalCompleter : finalCompleters){
                if(finalCompleter.isPreDefinedValue()){
                    List<String> values = handlePreDefined(finalCompleter.getPreDefinedValue());
                    String partial = args[args.length - 1];
                    if (partial.length() < 1) { // provide hint
                        if(finalCompleter.getHint() == null)
                            finalProvideList.addAll(values);
                        else
                            finalProvideList.addAll(finalCompleter.getHint());

                    } else { // provide candidates
                        finalProvideList.addAll(values.stream()
                                .filter(val -> val.toLowerCase().startsWith(partial.toLowerCase()))
                                .collect(Collectors.toList()));
                    }
                }else{
                    String partial = args[args.length - 1];
                    if (partial.length() < 1) { // show hint if nothing is entered yet
                        finalProvideList.addAll(finalCompleter.getHint());
                    } else {
                        finalProvideList.addAll(finalCompleter.getCandidates(partial));
                    }
                }
            }

            return finalProvideList;
        });
    }
    private static List<String> handlePreDefined(PredefinedTabCompleters val){
        List<String> returning = new ArrayList<>();
        switch (val){
            case PLAYERS:
                Bukkit.getOnlinePlayers().forEach((p) -> {
                    returning.add(p.getName());
                });
                break;

        }

        return returning;
    }


    @Override
    public void setExecutor(ICommandExecutor executor) {
        command.setExecutor((sender, command1, label, args) -> {
            executor.execute(new BukkitCommandSender(sender), label, args);
            return true;
        });
    }
}
