package io.github.wysohn.triggerreactor.manager.wrapper;

import org.bukkit.entity.LivingEntity;

public class LivingEntityWrapper extends EntityWrapper {
    private double health;
    private PlayerWrapper killer;

    LivingEntityWrapper(LivingEntity target) {
        super(target);
        health = target.getHealth();
        if(target.getKiller() != null)
            killer = new PlayerWrapper(target.getKiller());
    }

}
