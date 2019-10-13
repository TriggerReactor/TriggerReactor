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
	public boolean validate(Object arg, Object value) {
		return ((Number) value).doubleValue() >= ((Number) arg).doubleValue();
	}
}
