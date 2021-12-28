package io.github.wysohn.triggerreactor.core.script.validation;

import io.github.wysohn.triggerreactor.core.script.validation.option.ValidationOption;
import io.github.wysohn.triggerreactor.core.script.validation.option.ValidationOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a list of ValidationOptions
 */
public class Arg {
    private final ValidationOptions validationOptions;

    private Map<ValidationOption, Object> options = new HashMap<>();

    public Arg(ValidationOptions validationOptions) {
        this.validationOptions = validationOptions;
    }

    void addOption(ValidationOption option, Object value) {
        options.put(option, value);
    }

    boolean hasOption(ValidationOption option) {
        return options.containsKey(option);
    }

    //returns a String describing this Arg's type, for error-construction purposes
    String typeString() {
        ValidationOption typeOption = validationOptions.forName("type");
        Object type = getOption(typeOption);
        if (type == null) {
            return "any";
        }
        if (type instanceof Class<?>) {
            return ((Class<?>) type).getSimpleName();
        }

        return type.toString();
    }

    Object getOption(ValidationOption option) {
        return options.get(option);
    }

    /**
     * null if the Object matches the validation criteria of this arg
     * A string representing the error otherwise
     */
    String validate(Object o) {
        for (Map.Entry<ValidationOption, Object> entry : options.entrySet()) {
            String error = entry.getKey().validate(entry.getValue(), o);
            if (error == null) {
                continue;
            }

            String name = name();
            if (name != null) {
                error = error.replace("%name%", name);
            }
            return error;
        }
        return null;
    }

    String name() {
        String name = (String) getOption(validationOptions.forName("name"));
        if (name == null) {
            return "null";
        }
        return name;
    }
}
