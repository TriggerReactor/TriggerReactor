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
	
	//returns a String describing this Arg's type, for error-construction purposes
	String typeString() {
		ValidationOption typeOption = ValidationOption.forName("type");
		Object type = getOption(typeOption);
		if (type == null) {
			return "any";
		}
		if (type instanceof Class<?>) {
			return ((Class<?>) type).getSimpleName();
		}
		
		return type.toString();
	}
	
	String name() {
		String name = (String) getOption(ValidationOption.forName("name"));
		if (name == null) {
			return "null";
		}
		return name;
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
}
