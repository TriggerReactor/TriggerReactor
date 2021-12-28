package io.github.wysohn.triggerreactor.tools;

import java.util.HashMap;

/**
 * A hash map that supports null for get() and put()
 */
public class VarMap extends HashMap<String, Object> {
    public VarMap() {
    }

    @Override
    public Object get(Object key) {
        Object result = super.get(key);
        if (result == NULL) {
            return null;
        }
        return result;
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            value = NULL;
        }
        return super.put(key, value);
    }

    private static final long serialVersionUID = 1L;
    private static final NullObject NULL = new NullObject();

    private static final class NullObject {
    }
}
