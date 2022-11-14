/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.tools.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AreaTrigger extends Trigger {
    private final Area area;
    private final File folder;
    private final Map<UUID, WeakReference<IEntity>> trackedEntities = new ConcurrentHashMap<>();
    private EnterTrigger enterTrigger;
    private ExitTrigger exitTrigger;
    private AreaTriggerManager.EventType type = null;

    @Inject
    private AreaTrigger(@Assisted TriggerInfo info,
                        @Assisted Area area,
                        @Assisted File folder) {
        super(info, null); // area trigger has scripts in its folder
        this.area = area;
        this.folder = folder;
    }

    public Area getArea() {
        return area;
    }

    //we don't need interpreter for area trigger but enter and exit trigger
    @Override
    protected Interpreter initInterpreter(Map<String, Object> scriptVars) {
        return null;
    }

    public void activate(Object e, Map<String, Object> scriptVars, AreaTriggerManager.EventType type) {
        this.type = type;

        super.activate(e, scriptVars);
    }

    //intercept and pass interpretation to appropriate trigger
    @Override
    protected void startInterpretation(Object e,
                                       Map<String, Object> scriptVars,
                                       Interpreter interpreter,
                                       boolean sync) {
        switch (type) {
            case ENTER:
                if (getEnterTrigger() != null)
                    getEnterTrigger().activate(e, scriptVars);
                break;
            case EXIT:
                if (getExitTrigger() != null)
                    getExitTrigger().activate(e, scriptVars);
                break;
            default:
                throw new RuntimeException("Unknown area event type " + type);
        }
    }

    @Override
    public Trigger clone() {
        return null;
    }

    @Override
    protected String getTimingId() {
        return StringUtils.dottedPath(super.getTimingId(), area.toString());
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "area=" + area +
                '}';
    }

    public EnterTrigger getEnterTrigger() {
        return enterTrigger;
    }

    public void setEnterTrigger(String script) throws AbstractTriggerManager.TriggerInitFailedException {
        enterTrigger = new EnterTrigger(this.getInfo(), script, this);
    }

    public void setEnterTrigger(EnterTrigger enterTrigger) {
        this.enterTrigger = enterTrigger;
    }

    public ExitTrigger getExitTrigger() {
        return exitTrigger;
    }

    public void setExitTrigger(String script) throws AbstractTriggerManager.TriggerInitFailedException {
        exitTrigger = new ExitTrigger(this.getInfo(), script, this);
    }

    public void setExitTrigger(ExitTrigger exitTrigger) {
        this.exitTrigger = exitTrigger;
    }

    public void addEntity(IEntity entity) {
        WeakReference<IEntity> ref = new WeakReference<>(entity);
        this.trackedEntities.put(entity.getUniqueId(), ref);
    }

    public void removeEntity(UUID uuid) {
        this.trackedEntities.remove(uuid);
    }

    public IEntity getEntity(UUID uuid) {
        WeakReference<IEntity> ref = this.trackedEntities.get(uuid);
        if (ref == null)
            return null;

        IEntity entity = ref.get();
        //just remove it as it's got garbage-collected.
        if (entity == null) {
            this.trackedEntities.remove(uuid);
        }

        return entity;
    }

    public List<IEntity> getEntities() {
        List<IEntity> entities = new ArrayList<>();

        Set<UUID> remove = new HashSet<>();
        for (Map.Entry<UUID, WeakReference<IEntity>> entry : this.trackedEntities.entrySet()) {
            WeakReference<IEntity> ref = entry.getValue();
            IEntity entity = ref.get();
            if (entity != null) {
                entities.add(entity);
            } else {
                remove.remove(entry.getKey());
            }
        }

        for (UUID uuid : remove) {
            this.trackedEntities.remove(uuid);
        }

        return entities;
    }

    public static class EnterTrigger extends Trigger {
        private final AreaTrigger areaTrigger;

        public EnterTrigger(TriggerInfo info, String script, AreaTrigger areaTrigger) throws
                AbstractTriggerManager.TriggerInitFailedException {
            super(info, script);
            this.areaTrigger = areaTrigger;

            init();
        }

        @Override
        protected String getTimingId() {
            return StringUtils.dottedPath(areaTrigger.getTimingId(), "Enter");
        }

        @Override
        public Trigger clone() {
            try {
                return new EnterTrigger(info, script, areaTrigger);
            } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static class ExitTrigger extends Trigger {
        private final AreaTrigger areaTrigger;

        public ExitTrigger(TriggerInfo info, String script, AreaTrigger areaTrigger) throws
                AbstractTriggerManager.TriggerInitFailedException {
            super(info, script);
            this.areaTrigger = areaTrigger;

            init();
        }

        @Override
        protected String getTimingId() {
            return StringUtils.dottedPath(areaTrigger.getTimingId(), "Exit");
        }

        @Override
        public Trigger clone() {
            try {
                return new ExitTrigger(info, script, areaTrigger);
            } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
