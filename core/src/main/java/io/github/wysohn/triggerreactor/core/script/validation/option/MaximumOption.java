package io.github.wysohn.triggerreactor.core.script.validation.option;

public class MaximumOption extends ValidationOption {
    @Override
    public boolean canContain(Object o) {
        return o instanceof Number;
    }

    @Override
    public String validate(Object arg, Object value) {
        if (((Number) value).doubleValue() <= ((Number) arg).doubleValue()) {
            return null;
        }
        return "%name% must be less than or equal to " + value;
    }
}
