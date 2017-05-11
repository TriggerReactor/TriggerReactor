package io.github.wysohn.triggerreactor.manager.wrapper;

import org.bukkit.entity.Player;

public class PlayerWrapper extends LivingEntityWrapper{
    private boolean isSneaking;
    private boolean isSprinting;

    public PlayerWrapper(Player target) {
        super(target);

        isSneaking = target.isSneaking();
        isSprinting = target.isSprinting();
    }

}
