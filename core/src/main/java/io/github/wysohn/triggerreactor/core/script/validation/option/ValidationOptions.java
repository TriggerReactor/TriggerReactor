package io.github.wysohn.triggerreactor.core.script.validation.option;

import java.util.HashMap;

public final class ValidationOptions {
    final HashMap<String, ValidationOption> options = new HashMap<>();

    ValidationOptions() {
    }

    public ValidationOption forName(String name) {
        return options.get(name);
    }
}
