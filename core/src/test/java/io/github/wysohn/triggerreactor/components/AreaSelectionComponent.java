package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.AreaSelectionManager;

@Component
public interface AreaSelectionComponent {
    AreaSelectionManager manager();
}
