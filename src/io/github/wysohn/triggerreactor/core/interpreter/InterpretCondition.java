package io.github.wysohn.triggerreactor.core.interpreter;

public interface InterpretCondition {
    public void setCondition(Object context, String key, Object value);
    public Object getCondition(Object context, String key);
}
