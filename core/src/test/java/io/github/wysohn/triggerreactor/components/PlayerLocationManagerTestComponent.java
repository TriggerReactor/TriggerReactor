package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.manager.PlayerLocationManager;

@Component
public interface PlayerLocationManagerTestComponent {
    PlayerLocationManager getPlayerLocationManager();

    @Component.Builder
    interface Builder {
        PlayerLocationManagerTestComponent build();

        @BindsInstance
        Builder gameController(IGameController gameController);
    }
}
