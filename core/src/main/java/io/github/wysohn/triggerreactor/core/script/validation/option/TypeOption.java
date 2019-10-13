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
	public boolean validate(Object arg, Object value) {
		switch((String) arg) {
		case "int":
			if (!(value instanceof Number)) {
				return false;
			}
			Number num = (Number) value;
			if (Math.round(num.doubleValue()) == num.doubleValue()) {
				return true;
			}
			return false;
		
		case "number":
			if (value instanceof Number) {
				return true;
			}
			return false;
		case "string":
			if (value instanceof String) {
				return true;
			}
			return false;
		default:
			throw new ValidationException("Unrecognized option type option: " + arg + ", this should *never* happen.  Report this immediately.");
		}
		
	}
}