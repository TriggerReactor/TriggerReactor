package io.github.wysohn.triggerreactor.core.script.validation;

import io.github.wysohn.triggerreactor.core.script.validation.option.*;
import io.github.wysohn.triggerreactor.tools.JSArrayIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Validator {
    private final Overload[] overloads;

    private Validator() {
        overloads = null;
    }

    private Validator(Overload[] overloads) {
        this.overloads = overloads;
		/*
		for (Overload overload : overloads) {
			lengths.add(overload.length());
		}
		this.lengths = lengths.toArray(new Integer[0]);
		*/
    }

    /**
     * Finds which overload the list of args matches, if any
     *
     * @return the overload it matched, if any, else -1
     */
    public ValidationResult validate(Object... args) {
        String[] errorList = new String[overloads.length];
        boolean lengthMatchFound = false;
        for (int i = 0; i < overloads.length; i++) {
            String error = null;
            if (overloads[i].length() == args.length) {
                error = overloads[i].matches(args);
                lengthMatchFound = true;
            } else {
                continue;
            }
            if (error == null) {
                return new ValidationResult(i);
            }
            errorList[i] = error;
        }
        if (!lengthMatchFound) {
            return new ValidationResult("Incorrect number of arguments: " + args.length);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < overloads.length; i++) {
            if (errorList[i] == null) {
                continue;
            }
            builder.append("Could not match <");
            builder.append(overloads[i].overloadInfo());
            builder.append("> because ");
            builder.append(errorList[i]);
            builder.append("\n");
        }
        return new ValidationResult(builder.toString());
    }

    private static Object getOrFail(Map<String, Object> js, String slot) {
        if (!(js.containsKey(slot))) {
            throw new ValidationException("Could not find property " + slot + " while processing validation info.");
        }
        return js.get(slot);
    }

    public static Validator from(Map<String, Object> js) {
        try {
            Map<String, Object> overloads = (Map<String, Object>) getOrFail(js, "overloads");
            List<Overload> overloadList = new ArrayList<>();

            for (Object overload : new JSArrayIterator(overloads)) {
                List<Arg> argList = new ArrayList<>();

                for (Object argObject : new JSArrayIterator((Map<String, Object>) overload)) {
                    Arg arg = new Arg(validationOptions);

                    for (String key : ((Map<String, Object>) argObject).keySet()) {
                        ValidationOption option = validationOptions.forName(key);
                        Object value = getOrFail((Map<String, Object>) argObject, key);
                        if (!(option.canContain(value))) {
                            throw new ValidationException("Invalid value for option " + option.getClass().getSimpleName() +
                                    " : " + value);
                        }

                        arg.addOption(option, value);
                    }
                    argList.add(arg);
                }

                Arg[] args = argList.toArray(new Arg[0]);
                overloadList.add(new Overload(args));
            }

            return new Validator(overloadList.toArray(new Overload[0]));
        } catch (ClassCastException e) {
            throw new ValidationException("Incorrect data type found while processing validation info", e);
        }
    }

    private static final ValidationOptions validationOptions = new ValidationOptionsBuilder()
            .addOption(new MinimumOption(), "minimum")
            .addOption(new MaximumOption(), "maximum")
            .addOption(new MatchesOption(), "matches")
            .addOption(new NameOption(), "name")
            .addOption(new TypeOption(), "type")
            .build();
}
