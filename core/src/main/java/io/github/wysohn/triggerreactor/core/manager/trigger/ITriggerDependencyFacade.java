package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;

import java.util.Map;
import java.util.UUID;

public interface ITriggerDependencyFacade {
    Map<String, Executor> getExecutorMap();

    Map<String, Placeholder> getPlaceholderMap();

    Map<Object, Object> getGlobalVariableAdapter();

    Map<String, ?> getSharedVars();

    Map<String, Object> getCustomVarsForTrigger(Object e);

    IPlayer extractPlayerFromContext(Object e);

    ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns);
}
