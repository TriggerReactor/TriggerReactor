package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.IGlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.SharedVariableManager;
import io.github.wysohn.triggerreactor.core.manager.js.IBackedMapProvider;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

@Singleton
public class TriggerDependencyFacade implements ITriggerDependencyFacade {
    @Inject
    private SharedVariableManager sharedVariableManager;
    @Inject
    private IBackedMapProvider<Executor> executorManager;
    @Inject
    private IBackedMapProvider<Placeholder> placeholderManager;
    @Inject
    private IGlobalVariableManager globalVariableManager;
    @Inject
    private IPluginManagement pluginManagement;


    @Inject
    private TriggerDependencyFacade() {
    }

    @Override
    public Map<String, Executor> getExecutorMap() {
        return executorManager.getBackedMap();
    }

    @Override
    public Map<String, Placeholder> getPlaceholderMap() {
        return placeholderManager.getBackedMap();
    }

    @Override
    public Map<Object, Object> getGlobalVariableAdapter() {
        return globalVariableManager.getGlobalVariableAdapter();
    }

    @Override
    public Map<String, ?> getSharedVars() {
        return sharedVariableManager.getSharedVars();
    }

    @Override
    public Map<String, Object> getCustomVarsForTrigger(Object e) {
        return pluginManagement.getCustomVarsForTrigger(e);
    }

    @Override
    public IPlayer extractPlayerFromContext(Object e) {
        return pluginManagement.extractPlayerFromContext(e);
    }

    @Override
    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return pluginManagement.createInterrupter(cooldowns);
    }
}