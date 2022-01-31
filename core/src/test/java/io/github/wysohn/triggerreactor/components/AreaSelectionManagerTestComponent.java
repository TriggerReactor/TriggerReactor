package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

@Component
@ManagerScope
public interface AreaSelectionManagerTestComponent {
    AreaSelectionManager getAreaSelectionManager();
}
