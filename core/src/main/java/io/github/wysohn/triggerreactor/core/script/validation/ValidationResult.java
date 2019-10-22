package io.github.wysohn.triggerreactor.core.script.validation;

public class ValidationResult {
	private String error;
	private int overload;
	
	public ValidationResult(int overload) {
		error = null;
		this.overload = overload;
	}
	
	public ValidationResult(String error) {
		this.error = error;
		this.overload = -1;
	}
	
	//get the overload result of the match.  -1 if there was no match
	public int getOverload() {
		return overload;
	}
	
	//get the error that describes why no match was found.  null if a match was found
	public String getError() {
		return error;
	}
}
