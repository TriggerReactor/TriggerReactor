package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;

import java.util.Map;
import java.util.UUID;

public interface ITriggerDependencyFacade {
    Map<String, Object> getExtraVariables(Object e);

    IPlayer extractPlayerFromContext(Object e);

    ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns);
}
