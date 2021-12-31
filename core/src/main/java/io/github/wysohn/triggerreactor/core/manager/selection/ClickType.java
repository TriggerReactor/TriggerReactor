package io.github.wysohn.triggerreactor.core.manager.selection;

public enum ClickType {
    LEFT_CLICK("left"),
    LEFT_CLICK_AIR("left_air"),
    RIGHT_CLICK("right"),
    RIGHT_CLICK_AIR("right_air"),
    UNKNOWN("unknown"),
    ;

    private final String variable;

    ClickType(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }
}
