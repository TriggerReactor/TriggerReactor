package io.github.wysohn.triggerreactor.manager.wrapper;

import org.bukkit.entity.Entity;

public class EntityWrapper extends Wrapper<Entity> {
    private String name;
    private LocationWrapper location;

    EntityWrapper(Entity target) {
        super(target);

        name = target.getName();
        location = new LocationWrapper(target.getLocation());
    }

}
