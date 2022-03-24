package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.DynamicTabCompleter;
import io.github.wysohn.triggerreactor.core.scope.PostPluginLifetime;

import java.util.LinkedList;
import java.util.List;

@Module
public abstract class CoreTabCompleterModule {
    @Provides
    @IntoMap
    @StringKey("$playerlist")
    @PostPluginLifetime
    static DynamicTabCompleter providePlayerListCompleter(IGameController gameController) {
        return DynamicTabCompleter.Builder.of("$playerlist", () -> {
            List<String> candidates = new LinkedList<>();
            gameController.getOnlinePlayers().forEach(player -> candidates.add(player.getName()));
            return candidates;
        }).caching(100L).build();
    }
}
