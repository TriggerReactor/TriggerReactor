package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.scope.PostBukkitPluginLifetime;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;

@Component(dependencies = {BukkitTriggerReactorComponent.class})
@PostBukkitPluginLifetime
public interface LatestBukkitPluginMainComponent {
    BukkitTriggerReactor bukkitTriggerReactor();

    CommandHandler commandHandler();

    IWrapper wrapper();
}
