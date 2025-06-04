package io.github.wysohn.triggerreactor.core.manager;

import java.util.HashMap;

public interface IGlobalVariableManager {
    HashMap<String, Object> getGlobalVariableAdapter();
    HashMap<String, Object> getTempGlobalVariableAdapter();
}
