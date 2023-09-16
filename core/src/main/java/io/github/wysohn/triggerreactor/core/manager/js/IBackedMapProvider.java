package io.github.wysohn.triggerreactor.core.manager.js;

import io.github.wysohn.triggerreactor.core.manager.evaluable.IEvaluable;

import java.util.Map;

public interface IBackedMapProvider<T extends IEvaluable> {
    /**
     * Get the Map of IEvaluable objects that is directly backed
     * by the underlying data structure of the managing class.
     * <p>
     * It's expected that the returned Map is immutable, so the
     * caller should not modify the returned Map, and the implementing
     * class should not return a mutable Map.
     *
     * @return The Map of IEvaluable objects that is directly backed by the
     * underlying data structure of the managing class. Changes to the managing
     * class will be reflected in the returned Map.
     */
    Map<String, T> getBackedMap();
}
