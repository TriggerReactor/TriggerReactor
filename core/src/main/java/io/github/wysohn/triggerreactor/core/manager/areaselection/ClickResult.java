package io.github.wysohn.triggerreactor.core.manager.areaselection;

public enum ClickResult {
    /**
     * When two selections are in different worlds
     **/
    DIFFERENTWORLD,
    /**
     * Two coordinates are ready
     **/
    COMPLETE,
    /**
     * Only left clicked coordinate is ready
     **/
    LEFTSET,
    /**
     * Only right clicked coordinated is ready
     **/
    RIGHTSET
}
