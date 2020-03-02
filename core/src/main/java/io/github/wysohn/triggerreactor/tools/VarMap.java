package io.github.wysohn.triggerreactor.tools;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent hash map that supports null for get() and put()
 */
public class VarMap extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    private static final class NullObject {
    }

    private static final NullObject NULL = new NullObject();

    public VarMap() {
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            value = NULL;
        }
        return super.put(key, value);
    }

    @Override
    public Object get(Object key) {
        Object result = super.get(key);
        if (result == NULL) {
            return null;
        }
        return result;
    }
}
