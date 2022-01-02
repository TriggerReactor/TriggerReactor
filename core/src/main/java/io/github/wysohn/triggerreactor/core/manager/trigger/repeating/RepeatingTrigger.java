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

    private boolean autoStart = false;
    private Map<String, Object> vars;
    //////////////////////////////////////////////////////////////////////////////////////
    private boolean paused;

    @AssistedInject
    RepeatingTrigger(@Assisted TriggerInfo info, @Assisted String script) {
        super(info, script);
    }

    public RepeatingTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof RepeatingTrigger);
        RepeatingTrigger other = (RepeatingTrigger) o;

        this.throwableHandler = other.throwableHandler;
        this.autoStart = other.autoStart;
        this.vars = vars == null ? null : new HashMap<>(vars);
        this.paused = other.paused;
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

                vars.put(TRIGGER, "repeat");

                // we re-use the variables over and over.
                activate(vars);

                try {
                    Thread.sleep(getInterval());
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            throwableHandler.handleException((ICommandSender) null, e);
        }

        try {
            vars.put(TRIGGER, "stop");
            activate(vars);
        } catch (Exception e) {
            throwableHandler.handleException((ICommandSender) null, e);
        }
    }

    public long getInterval() {
        return info.getConfig().get(INTERVAL, Integer.class).orElse(1000);
    }

    public void setInterval(long interval) {
        info.getConfig().put(INTERVAL, interval);
    }

    public boolean isAutoStart() {
        return info.getConfig().get(AUTOSTART, Boolean.class).orElse(false);
    }

    public void setAutoStart(boolean autoStart) {
        info.getConfig().put(AUTOSTART, autoStart);
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

    private static final String AUTOSTART = "AutoStart";
    private static final String INTERVAL = "Interval";
    protected static final String TRIGGER = "trigger";
}
