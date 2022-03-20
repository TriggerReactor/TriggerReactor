package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;

@Component
public interface AreaSelectionManagerTestComponent {
    AreaSelectionManager getAreaSelectionManager();
}
