package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.tools.StringUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AreaTrigger extends Trigger {
    final Area area;
    final File folder;

    private EnterTrigger enterTrigger;
    private ExitTrigger exitTrigger;

    private Map<UUID, WeakReference<IEntity>> trackedEntities = new ConcurrentHashMap<>();

    public AreaTrigger(Area area, File folder, String name) {
        super(name, null, null);
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

    private AbstractAreaTriggerManager.EventType type = null;

    public void activate(Object e, Map<String, Object> scriptVars, AbstractAreaTriggerManager.EventType type) {
        this.type = type;

        super.activate(e, scriptVars);
    }

    //intercept and pass interpretation to appropriate trigger
    @Override
    protected void startInterpretation(Object e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
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

    void setEnterTrigger(String script, File triggerFile) throws AbstractTriggerManager.TriggerInitFailedException {
        enterTrigger = new EnterTrigger(this, triggerFile, script);
    }

    public void setEnterTrigger(String script) throws AbstractTriggerManager.TriggerInitFailedException {
        File triggerFile = AbstractTriggerManager.getTriggerFile(folder, "Enter", true);
        setEnterTrigger(script, triggerFile);
    }

    void setExitTrigger(String script, File triggerFile) throws AbstractTriggerManager.TriggerInitFailedException {
        exitTrigger = new ExitTrigger(this, triggerFile, script);
    }

    public void setExitTrigger(String script) throws AbstractTriggerManager.TriggerInitFailedException {
        File triggerFile = AbstractTriggerManager.getTriggerFile(folder, "Enter", true);
        setExitTrigger(script, triggerFile);
    }

    public EnterTrigger getEnterTrigger() {
        return enterTrigger;
    }

    public void setEnterTrigger(EnterTrigger enterTrigger) {
        this.enterTrigger = enterTrigger;
    }

    public ExitTrigger getExitTrigger() {
        return exitTrigger;
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

        public EnterTrigger(AreaTrigger areaTrigger, File file, String script) throws AbstractTriggerManager.TriggerInitFailedException {
            super(areaTrigger.triggerName, file, script);
            this.areaTrigger = areaTrigger;

            init();
        }

        @Override
        protected String getTimingId() {
            return StringUtils.dottedPath(areaTrigger.getTimingId(), "Enter");
        }

        @Override
        public boolean isSync() {
            return areaTrigger.isSync();
        }

        @Override
        public void setSync(boolean sync) {
            areaTrigger.setSync(sync);
        }

        @Override
        public Trigger clone() {
            try {
                return new EnterTrigger(areaTrigger, file, script);
            } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static class ExitTrigger extends Trigger {
        private final AreaTrigger areaTrigger;

        public ExitTrigger(AreaTrigger areaTrigger, File file, String script) throws AbstractTriggerManager.TriggerInitFailedException {
            super(areaTrigger.getTriggerName(), file, script);
            this.areaTrigger = areaTrigger;

            init();
        }

        @Override
        protected String getTimingId() {
            return StringUtils.dottedPath(areaTrigger.getTimingId(), "Exit");
        }

        @Override
        public boolean isSync() {
            return areaTrigger.isSync();
        }

        @Override
        public void setSync(boolean sync) {
            areaTrigger.setSync(sync);
        }

        @Override
        public Trigger clone() {
            try {
                return new ExitTrigger(areaTrigger, file, script);
            } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
