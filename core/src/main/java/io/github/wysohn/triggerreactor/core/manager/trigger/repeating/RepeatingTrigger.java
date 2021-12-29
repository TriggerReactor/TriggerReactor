package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RepeatingTrigger extends Trigger implements Runnable {
    @Inject
    IThrowableHandler throwableHandler;

    private long interval = 1000L;
    private boolean autoStart = false;
    private Map<String, Object> vars;
    //////////////////////////////////////////////////////////////////////////////////////
    private boolean paused;

    @AssistedInject
    RepeatingTrigger(@Assisted TriggerInfo info, @Assisted String script, @Assisted long interval) {
        super(info, script);
        this.interval = interval;
    }

    public RepeatingTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof RepeatingTrigger);
        RepeatingTrigger other = (RepeatingTrigger) o;

        this.throwableHandler = other.throwableHandler;
        this.interval = other.interval;
        this.autoStart = other.autoStart;
        this.vars = vars == null ? null : new HashMap<>(vars);
        this.paused = other.paused;
    }

    @Override
    public String toString() {
        return super.toString() + "{interval=" + interval + ", autoStart=" + autoStart + ", paused=" + paused
                + '}';
    }

    /**
     * This should be called at least once on start up so variables can be
     * initialized.
     */
    @Override
    public boolean activate(Map<String, Object> scriptVars) {
        ValidationUtil.notNull(scriptVars);
        vars = scriptVars;

        return super.activate(scriptVars);
    }

    /**
     * This should be called at least once on start up so variables can be
     * initialized.
     */
    @Override
    public boolean activate(Map<String, Object> scriptVars, boolean sync) {
        ValidationUtil.notNull(scriptVars);
        vars = scriptVars;

        return super.activate(scriptVars, sync);
    }

    /**
     * We don't use cooldown for this trigger. Just return false always
     */
    @Override
    protected boolean checkCooldown(Object e) {
        return false;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (paused && !Thread.interrupted()) {
                        this.wait();
                    }
                }

                vars.put(RepeatingTriggerManager.TRIGGER, "repeat");

                // we re-use the variables over and over.
                activate(vars);

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            throwableHandler.handleException((ICommandSender) null, e);
        }

        try {
            vars.put(RepeatingTriggerManager.TRIGGER, "stop");
            activate(vars);
        } catch (Exception e) {
            throwableHandler.handleException((ICommandSender) null, e);
        }
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;

        if (!paused) {
            synchronized (this) {
                this.notify();
            }
        }
    }
}
