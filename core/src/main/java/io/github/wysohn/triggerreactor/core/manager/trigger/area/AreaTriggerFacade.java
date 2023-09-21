package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerFacade;

import java.util.List;
import java.util.UUID;

public class AreaTriggerFacade extends TriggerFacade {
    public AreaTriggerFacade(AreaTrigger trigger) {
        super(trigger);
    }

    public Area getArea() {
        return ((AreaTrigger) trigger).getArea();
    }

    public TriggerFacade getEnterTrigger() {
        return ((AreaTrigger) trigger).getEnterTrigger().getTriggerFacade();
    }

    public TriggerFacade getExitTrigger() {
        return ((AreaTrigger) trigger).getExitTrigger().getTriggerFacade();
    }

    public IEntity getEntity(UUID uuid) {
        return ((AreaTrigger) trigger).getEntity(uuid);
    }

    public IEntity getEntity(String uuid) {
        return getEntity(UUID.fromString(uuid));
    }

    public List<IEntity> getEntities() {
        return ((AreaTrigger) trigger).getEntities();
    }

}
