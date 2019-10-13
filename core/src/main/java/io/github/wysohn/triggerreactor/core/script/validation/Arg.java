package io.github.wysohn.triggerreactor.core.script.validation;

import java.util.HashMap;
import java.util.Map;

import io.github.wysohn.triggerreactor.core.script.validation.option.ValidationOption;

/**
 * Represents a list of ValidationOptions
 */
public class Arg {
	private Map<ValidationOption, Object> options = new HashMap<>();

	public Arg() {}
	
	void addOption(ValidationOption option, Object value) {
		options.put(option, value);
	}
	
	Object getOption(ValidationOption option) {
		return options.get(option);
	}
	
	boolean hasOption(ValidationOption option) {
		return options.containsKey(option);
	}
	
	/**
	 * true if the Object matches the validation criteria of this arg
	 */
	boolean validate(Object o) {
		for (Map.Entry<ValidationOption, Object> entry : options.entrySet()) {
			if (!entry.getKey().validate(entry.getValue(), o)) {
				return false;
			}
		}
		
		return true;
	}
}
