package io.github.wysohn.triggerreactor.core.script.validation.option;

public class ValidationOptionsBuilder {
    private final ValidationOptions validationOption = new ValidationOptions();

    public ValidationOptionsBuilder addOption(ValidationOption option, String name) {
        validationOption.options.put(name, option);
        return this;
    }

    public ValidationOptions build() {
        return validationOption;
    }
}