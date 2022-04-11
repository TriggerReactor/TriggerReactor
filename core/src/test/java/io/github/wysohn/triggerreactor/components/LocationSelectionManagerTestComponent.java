package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.selection.LocationSelectionManager;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;

import javax.inject.Named;

@Component
@PluginLifetime
public interface LocationSelectionManagerTestComponent {
    LocationSelectionManager getLocationSelectionManager();

    @Component.Builder
    interface Builder {
        LocationSelectionManagerTestComponent build();

        @BindsInstance
        Builder permissionString(@Named("Permission") String permissionString);
    }
}
