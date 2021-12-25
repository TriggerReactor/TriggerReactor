package io.github.wysohn.triggerreactor.tools;

import java.util.Iterator;
import java.util.Map;

/**
 * Iterates over JSObject arrays.
 */
public class JSArrayIterator implements Iterable<Object> {
    private final Map<String, Object> array;

    public JSArrayIterator(Map<String, Object> array) {
        this.array = array;
    }

    @Override
    public Iterator<Object> iterator() {
        return new RealJSArrayIterator(array);
    }

    private static class RealJSArrayIterator implements Iterator<Object> {
        private final Map<String, Object> array;
        private int index = 0;

        public RealJSArrayIterator(Map<String, Object> array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return array.containsKey(String.valueOf(index));
        }

        @Override
        public Object next() {
            return array.get(String.valueOf(index++));
        }
    }
}
