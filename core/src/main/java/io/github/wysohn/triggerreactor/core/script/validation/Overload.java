package io.github.wysohn.triggerreactor.core.script.validation;

public class Overload {
	private final Arg[] args;
	
	Overload(Arg[] args) {
		this.args = args;
	}
	
	/**
	 * @return the number of args this Overload contains
	 */
	int length() {
		return args.length;
	}
	
	/**
	 * Get a specific Arg
	 */
	Arg get(int index) {
		return args[index];
	}
	
	/**
	 * @param args the Object this Overload should attempt to match
	 * @return true if the args match, false otherwise
	 */
	boolean matches(Object... args) {
		if (length() != args.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			if (!(this.args[i].validate(args[i]))) {
				return false;
			}
		}
		return true;
	}
}
