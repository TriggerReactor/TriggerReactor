package io.github.wysohn.triggerreactor.core.script.validation.option;

public abstract class ValidationOption {

    /**
     * Verify that this option can contain the given Object as configuration
     * For example MinimumOption can only contain numbers, not Strings
     *
     * @param arg the Object to test
     * @return true if arg is valid, false otherwise
     */
    public abstract boolean canContain(Object arg);

    /**
     * Validate an Object against the criteria given by this option
     *
     * @param arg   the configuration value this option has
     * @param value the object to test
     * @return null if successful, an error message if it failed
     */
    public abstract String validate(Object arg, Object value);
}