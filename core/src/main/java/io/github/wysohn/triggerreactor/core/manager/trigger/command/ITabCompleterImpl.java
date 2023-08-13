package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ITabCompleterImpl implements ITabCompleter {
    List<String> hint;
    List<String> candidate;
    PredefinedTabCompleters preDefinedValue;
    Map<Integer, Pattern> conditions;

    protected ITabCompleterImpl(ITabCompleter.Builder builder) {
        hint = builder.hint;
        candidate = builder.candidate;
        preDefinedValue = builder.preDefinedValue;
        conditions = builder.conditions;
    }

    @Override
    public List<String> getCandidates(String part) {
        if (preDefinedValue != null || candidate == null) {
            return null;
        } else {
            return candidate.stream()
                    .filter(val -> val.toLowerCase().startsWith(part.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getHint() {
        return hint;
    }

    @Override
    public boolean isPreDefinedValue() {
        return preDefinedValue != null;
    }

    @Override
    public PredefinedTabCompleters getPreDefinedValue() {
        return preDefinedValue;
    }

    @Override
    public boolean hasConditionMap() {
        return conditions != null && conditions.size() != 0;
    }

    @Override
    public Map<Integer, Pattern> getConditionMap() {
        return conditions;
    }

    @Override
    public boolean hasCondition(Integer index) {
        if (!hasConditionMap())
            return false;
        else
            return conditions.get(index) != null;
    }

    @Override
    public Pattern getCondition(Integer index) {
        if (!hasConditionMap())
            return null;
        else
            return conditions.get(index);
    }
}