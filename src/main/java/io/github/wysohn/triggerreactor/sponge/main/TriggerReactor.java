package io.github.wysohn.triggerreactor.sponge.main;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "triggerreactor")
public class TriggerReactor {
    @Listener
    public void onEnable(GameStartingServerEvent e){

    }

    @Listener
    public void onDisable(GameStoppingServerEvent e){

    }
}
