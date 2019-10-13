package io.github.wysohn.triggerreactor.tools;

import java.util.Iterator;

import jdk.nashorn.api.scripting.JSObject;

/**
 * Iterates over JSObject arrays.
 */
public class JSArrayIterator implements Iterable<Object> {
	private static class RealJSArrayIterator implements Iterator<Object> {
		private final JSObject array;
		private int index = 0;
		
		public RealJSArrayIterator(JSObject array) {
			this.array = array;
		}
	
		@Override
		public boolean hasNext() {
			return array.hasSlot(index);
		}
	
		@Override
		public Object next() {
			return array.getSlot(index++);
		}
	}
	
	private final JSObject array;
	
	public JSArrayIterator(JSObject array) {
		if (!(array.isArray())) {
			throw new IllegalArgumentException("Cannot construct a JSIterator over a non-array");
		}
		this.array = array;
	}

	@Override
	public Iterator<Object> iterator() {
		return new RealJSArrayIterator(array);
	}
}
