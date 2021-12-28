package io.github.wysohn.triggerreactor.core.script.validation.option;

import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;

public class TypeOption extends ValidationOption {
    @Override
    public boolean canContain(Object arg) {
        if (arg instanceof Class<?>) {
            return true;
        }

        if (!(arg instanceof String)) {
            return false;
        }

        for (String type : types) {
            if (type.equals(arg)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String validate(Object arg, Object value) {
        if (arg instanceof Class<?>) {
            if (value == null) {
                return "%name% must not be null";
            }
            if (((Class<?>) arg).isAssignableFrom(value.getClass())) {
                return null;
            } else {
                return "%name% must be a " + ((Class<?>) arg).getSimpleName();
            }
        }

        switch ((String) arg) {
            case "int":
                if (!(value instanceof Number)) {
                    return "%name% must be a whole number";
                }
                Number num = (Number) value;
                if (Math.round(num.doubleValue()) == num.doubleValue()) {
                    return null;
                }
                return "%name% must be a whole number";

            case "number":
                if (value instanceof Number) {
                    return null;
                }
                return "%name% must be a number";
            case "string":
                if (value instanceof String) {
                    return null;
                }
                return "%name% must be a string";
            case "boolean":
                if (value instanceof Boolean) {
                    return null;
                }
                return "%name% must be a boolean";
            default:
                throw new ValidationException(
                        "Unrecognized option type option: " + arg + ", this should *never* happen.  Report this "
                                + "immediately.");
        }

    }

    private static final String[] types = {"int", "number", "boolean", "string"};
}