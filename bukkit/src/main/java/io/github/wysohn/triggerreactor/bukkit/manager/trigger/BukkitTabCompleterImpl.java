package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.TabCompleterPreDefinedValue;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BukkitTabCompleterImpl implements TabCompleter {
    CommandTrigger trigger;

    public BukkitTabCompleterImpl(CommandTrigger trg){
        this.trigger = trg;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command12, String alias, String[] args) {
        Map<Integer, Set<ITabCompleter>> tabCompleterMap = trigger.getTabCompleterMap();
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
                            .filter(val -> val.startsWith(partial))
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
            //TODO - handle Tab Completer
        }

        return finalProvideList;
    }

    private static List<String> handlePreDefined(TabCompleterPreDefinedValue val){
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
}
