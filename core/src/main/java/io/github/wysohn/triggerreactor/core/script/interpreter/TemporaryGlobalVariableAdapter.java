package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryGlobalVariableAdapter extends GlobalVariableManager.GlobalVariableAdapter {

    private final ConcurrentHashMap<String, Object> temp_map = new ConcurrentHashMap<>();
    @Override
    public Object get(Object key){
        Object value = temp_map.get(key);

        if (value == null) {
            GlobalVariableTreeChildren structure = new GlobalVariableTreeChildren(temp_map, (String) key);
            return structure.size() == 0
                    ? null
                    : structure;
        }

        return value;
    }

    @Override
    public boolean containsKey(Object key) {
        return temp_map.contains(key);
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            return remove(key);
        }

        GlobalVariableTreeChildren child = new GlobalVariableTreeChildren(temp_map, (String) key);
        if (child.size() > 0) {
            remove(key);
        }

        temp_map.put(key, value);
        return null;
    }

    @Override
    public Object remove(Object key) {
        temp_map.remove(key);

        GlobalVariableTreeChildren child = new GlobalVariableTreeChildren(temp_map, (String) key);
        for (String k : child.getAllOriginKeys()) {
            temp_map.remove(k);
        }

        return null;
    }
}
