package io.github.wysohn.triggerreactor.core.script.validation;

import java.util.ArrayList;
import java.util.List;

import io.github.wysohn.triggerreactor.core.script.validation.option.ValidationOption;
import io.github.wysohn.triggerreactor.tools.JSArrayIterator;
import jdk.nashorn.api.scripting.JSObject;

public class Validator {
	private final Overload[] overloads;
	
	private Validator() {overloads = null;}
	
	private Validator(Overload[] overloads) {
		this.overloads = overloads;
	}
	
	/**
	 * Fings which overload the list of args matches, if any
	 * 
	 * @return the overload it matched, if any, else -1
	 */
	public int validate(Object... args) {
		for (int i = 0; i < overloads.length; i++) {
			if (overloads[i].matches(args)) {
				return i;
			}
		}
		return -1;
	}
	
	private static Object getOrFail(JSObject js, String slot) {
		if (!(js.hasMember(slot))) {
			throw new ValidationException("Could not find property " + slot + " while processing validation info.");
		}
		return js.getMember(slot);
	}
	
	public static Validator from(JSObject js) {
		try {
			JSObject overloads = (JSObject) getOrFail(js, "overloads");
			List<Overload> overloadList = new ArrayList<>();
			
			for (Object overload : new JSArrayIterator(overloads)) {
				List<Arg> argList = new ArrayList<>();
				
				for (Object argObject : new JSArrayIterator((JSObject) overload)) {
					Arg arg = new Arg();
					
					for (String key : ((JSObject) argObject).keySet()) {
						ValidationOption option = ValidationOption.forName(key);
						Object value = getOrFail((JSObject) argObject, key);
						if (!(option.canContain(value))) {
							throw new ValidationException("Invalid value for option " + option.getClass().getSimpleName() +
									" : " + value);
						}
						
						arg.addOption(option, value);
					}
					argList.add(arg);
				}
				
				Arg[] args = argList.toArray(new Arg[0]);
				overloadList.add(new Overload(args));
			}
			
			return new Validator(overloadList.toArray(new Overload[0]));
		} catch (ClassCastException e) {
			throw new ValidationException("Incorrect data type found while processing validation info", e);
		}
	}
}
