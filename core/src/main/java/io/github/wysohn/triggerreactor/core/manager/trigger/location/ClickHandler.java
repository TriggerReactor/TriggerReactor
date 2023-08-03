package io.github.wysohn.triggerreactor.core.manager.trigger.location;

public interface ClickHandler {
    /**
     * Check if click is allowed.
     *
     * @param activity the activity involved with this click
     * @return true if allowed; false if not (the click will be ignored in this case)
     */
    boolean allow(Activity activity);

    ClickHandler DEFAULT = (activity) -> activity == Activity.LEFT_CLICK_BLOCK
            || activity == Activity.RIGHT_CLICK_BLOCK;
}
