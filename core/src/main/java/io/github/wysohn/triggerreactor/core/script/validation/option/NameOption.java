package io.github.wysohn.triggerreactor.core.script.validation.option;

public class NameOption extends ValidationOption {
    @Override
    public boolean canContain(Object arg) {
        return arg instanceof String;
    }

    @Override
    public String validate(Object arg, Object value) {
        return null;
    }

}
