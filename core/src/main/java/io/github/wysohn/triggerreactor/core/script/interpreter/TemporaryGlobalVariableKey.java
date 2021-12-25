package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.Objects;

public class TemporaryGlobalVariableKey {
    private String key = null;

    public TemporaryGlobalVariableKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemporaryGlobalVariableKey that = (TemporaryGlobalVariableKey) o;
        return Objects.equals(key, that.key);
    }

    public String getKey() {
        return key;
    }
}
