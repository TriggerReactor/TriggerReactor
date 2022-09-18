package io.github.wysohn.triggerreactor.core.bridge;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

public interface IWorld {
    Iterable<IEntity> getEntities();

    IBlock getBlock(SimpleLocation clicked);
    IBlock getBlock(ILocation location);
}
