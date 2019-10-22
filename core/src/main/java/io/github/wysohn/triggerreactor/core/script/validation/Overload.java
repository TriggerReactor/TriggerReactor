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
	 * return a string representing this overload.  Will include type and arg names
	 */
	String overloadInfo() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			Arg arg = args[i];
			builder.append(arg.name());
			builder.append(" (");
			builder.append(arg.typeString());
			builder.append(")");
			//if not at the last arg
			if (args.length - 1 != i) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}
	
	/**
	 * @param args the Object this Overload should attempt to match
	 *        args.length and length() are expected to match
	 * @return null if the args match, an error otherwise
	 */
	String matches(Object... args) {
		if (length() != args.length) {
			throw new ValidationException("the length of args and the length of the overload should match;");
		}
		for (int i = 0; i < args.length; i++) {
			String error = this.args[i].validate(args[i]);
			if (error != null) {
				return error;
			}
		}
		return null;
	}
}
