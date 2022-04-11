package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;

@Component
@PluginLifetime
public interface AreaSelectionTestComponent {
    AreaSelectionManager manager();
}
