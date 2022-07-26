package io.github.wysohn.triggerreactor.core.script.validation.option;

public class MatchesOption extends ValidationOption {
    @Override
    public boolean canContain(Object o) {
        return o instanceof String;
    }

    @Override
    public String validate(Object arg, Object value) {
        if (((String) value).matches((String) arg))
            return null;
            
        return "%name% must match with " + arg;
    }
}
