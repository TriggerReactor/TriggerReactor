package io.github.wysohn.triggerreactor.core.script.validation.option;

public class MinimumOption extends ValidationOption {
	static {
		ValidationOption.register(new MinimumOption(), "minimum");
	}
	
	public static void init() {}
	
	private MinimumOption() {}

	@Override
	public boolean canContain(Object o) {
		return o instanceof Number;
	}

	@Override
	public String validate(Object arg, Object value) {
		if (((Number) value).doubleValue() >= ((Number) arg).doubleValue()) {
			return null;
		}
		return "%name% must be greater than " + value;
	}
}
