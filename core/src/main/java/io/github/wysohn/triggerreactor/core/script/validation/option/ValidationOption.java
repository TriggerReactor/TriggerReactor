package io.github.wysohn.triggerreactor.core.script.validation.option;

import java.util.HashMap;

public abstract class ValidationOption {
	
	private static final HashMap<String, ValidationOption> options = new HashMap<>();
	
	static {
		//force static{} blocks to be run
		MinimumOption.init();
		TypeOption.init();
	}
	
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
	 * @param arg the configuration value this option has
	 * @param value the object to test
	 * @return true if the validation succeeds, false otherwise
	 */
	public abstract boolean validate(Object arg, Object value);
	
	static void register(ValidationOption option, String name) {
		options.put(name, option);
	}
	
	public static ValidationOption forName(String name) {
		return options.get(name);
	}
}