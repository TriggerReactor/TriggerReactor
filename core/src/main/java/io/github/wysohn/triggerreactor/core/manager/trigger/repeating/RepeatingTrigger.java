package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.util.Map;

public class RepeatingTrigger extends Trigger implements Runnable {
    private long interval = 1000L;
    private boolean autoStart = false;
    private Map<String, Object> vars;
    //////////////////////////////////////////////////////////////////////////////////////
    private boolean paused;

    public RepeatingTrigger(ITriggerReactorAPI api,
                            TriggerInfo info,
                            String script) throws AbstractTriggerManager.TriggerInitFailedException {
        this(api, info, script, 1000L);

        init();
    }

    public RepeatingTrigger(ITriggerReactorAPI api,
                            TriggerInfo info,
                            String script,
                            long interval) throws AbstractTriggerManager.TriggerInitFailedException {
        super(api, info, script);
        this.interval = interval;

        init();
    }

    @Override
    public RepeatingTrigger clone() {
        try {
            return new RepeatingTrigger(api, info, script, interval);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + "interval=" + interval + ", autoStart=" + autoStart + ", paused=" + paused + '}';
    }

    /**
     * This should be called at least once on start up so variables can be
     * initialized.
     */
    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars, boolean sync) {
        ValidationUtil.notNull(scriptVars);
        vars = scriptVars;

        return super.activate(e, scriptVars, sync);
    }

    /**
     * This should be called at least once on start up so variables can be
     * initialized.
     */
    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars) {
        ValidationUtil.notNull(scriptVars);
        vars = scriptVars;

        return super.activate(e, scriptVars);
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

                vars.put(AbstractRepeatingTriggerManager.TRIGGER, "repeat");

                // we re-use the variables over and over.
                activate(new Object(), vars);

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            api.getThrowableHandler().handleException(null, e);
        }

        try {
            vars.put(AbstractRepeatingTriggerManager.TRIGGER, "stop");
            activate(new Object(), vars);
        } catch (Exception e) {
            api.getThrowableHandler().handleException(null, e);
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
