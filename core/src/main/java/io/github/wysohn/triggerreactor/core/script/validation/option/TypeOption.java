package io.github.wysohn.triggerreactor.core.script.validation.option;

import io.github.wysohn.triggerreactor.core.script.validation.ValidationException;

public class TypeOption extends ValidationOption {
	static {
		ValidationOption.register(new TypeOption(), "type");
	}
	
	public static void init() {}
	
	private TypeOption() {}
	
	private static final String[] types = {"int", "number", "string"};
	
	@Override
	public boolean canContain(Object arg) {
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
		switch((String) arg) {
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
		default:
			throw new ValidationException("Unrecognized option type option: " + arg + ", this should *never* happen.  Report this immediately.");
		}
		
	}
}