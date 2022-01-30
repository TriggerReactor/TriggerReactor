package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.manager.PlayerLocationManager;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

@Component
@ManagerScope
public interface PlayerLocationManagerTestComponent {
    PlayerLocationManager getPlayerLocationManager();

    @Component.Builder
    interface Builder {
        PlayerLocationManagerTestComponent build();

        @BindsInstance
        Builder gameController(IGameController gameController);
    }
}
