package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IWorld;

/**
 * Modify or retrieve the game state.
 * Game state is anything related to the game world that changing the state of
 * it will affect the game play in any way (e.g. world, weather, time, entity, etc).
 */
public interface IGameStateSupervisor {
    Iterable<IWorld> getWorlds();
    IWorld getWorld(String world);
}
