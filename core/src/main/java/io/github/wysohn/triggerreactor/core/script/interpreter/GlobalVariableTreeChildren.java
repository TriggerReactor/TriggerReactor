package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalVariableTreeChildren {

    public final String key;
    private final Set<String> vars;

    public GlobalVariableTreeChildren(Map<String, Object> vars, String key) {
        this.vars = vars.keySet().stream()
                .filter(k -> k.startsWith(key) && !k.equals(key))
                .collect(Collectors.toSet());
        this.key = key;
    }

    public Set<String> keySet() {
        return getAllOriginKeys().stream()
                .map(k -> k.replaceFirst(key.isEmpty() ? key : key + ".", "")
                        .split("\\.")[0])
                .collect(Collectors.toSet());
    }

    public Set<String> getAllOriginKeys() {
        return vars;
    }

    public int size() {
        return vars.size();
    }

    @Override
    public String toString() {
        return "GlobalVariableTreeChildren{" +
                "key='" + key + '\'' +
                ", childKeys=[" + String.join(", ", keySet()) + "]" +
                '}';
    }
}
