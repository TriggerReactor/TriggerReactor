package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EntityTrackingThread extends Thread {
    private final AreaTriggerManager manager;

    public EntityTrackingThread(AreaTriggerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        while (manager.pluginLifecycleController.isEnabled() && !Thread.interrupted()) {
            try {
                //track entity locations
                for (IWorld w : manager.gameController.getWorlds()) {
                    Collection<IEntity> entityCollection = getEntitiesSync(w);
                    for (IEntity entity : entityCollection) {
                        UUID uuid = entity.getUniqueId();

                        if (!manager.pluginLifecycleController.isEnabled())
                            break;

                        Future<Boolean> future = manager.taskSupervisor.submitSync(
                                () -> !entity.isDead() && entity.isValid());

                        boolean valid = false;
                        try {
                            if (future != null)
                                valid = future.get();
                        } catch (InterruptedException | CancellationException e1) {
                            break;
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }

                        if (!valid)
                            continue;

                        if (!manager.entityLocationMap.containsKey(uuid))
                            continue;

                        SimpleLocation previous = manager.entityLocationMap.get(uuid);
                        SimpleLocation current = entity.getLocation();

                        //update location if equal
                        if (!previous.equals(current)) {
                            manager.entityLocationMap.put(uuid, current);
                            manager.onEntityBlockMoveAsync(uuid, entity.get(), previous, current);
                        }

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // some other unknown issues.
                return;
            }

            try {
                Thread.sleep(50L);//same as one tick
            } catch (InterruptedException ignored) {
            }
        }
    }

    private Collection<IEntity> getEntitiesSync(IWorld w) {
        Collection<IEntity> entities = new LinkedList<>();

        try {
            manager.taskSupervisor.submitSync(() -> {
                for (IEntity e : w.getEntities())
                    entities.add(e);
                return null;
            }).get();
        } catch (Exception ignored) {
        }

        return entities;
    }
}
