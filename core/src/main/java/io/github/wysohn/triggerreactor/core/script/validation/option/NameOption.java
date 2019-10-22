package io.github.wysohn.triggerreactor.core.script.validation.option;

public class NameOption extends ValidationOption {
	static {
		ValidationOption.register(new NameOption(), "name");
	}
	
	static void init() {}
	
	private NameOption() {}

	@Override
	public boolean canContain(Object arg) {
		return arg instanceof String;
	}

	@Override
	public String validate(Object arg, Object value) {
		return null;
	}

}
