package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Simple proxy that is passed to the trigger as 'cooldown' variable.
 */
public class TriggerCooldownProxy {
    private final Trigger trigger;

    public TriggerCooldownProxy(Trigger trigger) {
        this.trigger = trigger;
    }

    public boolean isCooldown(UUID playerUuid) {
        return trigger.checkCooldown(playerUuid);
    }

    public long getCooldown(UUID playerUuid, TimeUnit unit) {
        Map<UUID, Long> cooldownEnds = trigger.cooldowns;

        if (cooldownEnds.containsKey(playerUuid)) {
            long end = cooldownEnds.get(playerUuid);
            long now = System.currentTimeMillis();

            long diff = end - now;
            if (diff > 0L) {
                return unit.convert(diff, TimeUnit.MILLISECONDS);
            }
        }

        return 0L;
    }
}
