package io.github.wysohn.triggerreactor.tools;

import java.util.HashMap;

public class CaseInsensitiveStringMap<T> extends HashMap<String, T> {

    private static final long serialVersionUID = 1L;

    @Override
    public T get(Object key) {
        if (!(key instanceof String)) {
            return super.get(key);
        }

        String match = caseInsensitiveMatch((String) key);
        return match == null ? null : super.get(match);
    }

    /**
     * Case insensitive test for a key in the map.
     */
    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return super.containsKey(key);
        }

        return caseInsensitiveMatch((String) key) != null;
    }

    @Override
    public T put(String key, T value) {
        String match = caseInsensitiveMatch(key);
        return super.put(match == null ? key : match, value);
    }

    //if a version of key exists in the Map with similar case, return that, else return null
    private String caseInsensitiveMatch(String key) {
        for (String s : keySet()) {
            if (s.equalsIgnoreCase(key)) {
                return s;
            }
        }

        return null;
    }
}
