package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;

import java.io.File;
import java.util.Map;

public class RepeatingTrigger extends AbstractTriggerManager.Trigger implements Runnable {
    private final AbstractRepeatingTriggerManager.ThrowableHandler throwableHandler = new AbstractRepeatingTriggerManager.ThrowableHandler() {
        @Override
        public void onFail(Throwable throwable) {
            throwable.printStackTrace();
            TriggerReactorCore.getInstance().getLogger()
                    .warning("Repeating Trigger [" + triggerName + "] encountered an error!");
            TriggerReactorCore.getInstance().getLogger().warning(throwable.getMessage());
            TriggerReactorCore.getInstance().getLogger()
                    .warning("If you are an administrator, see console for more details.");
        }
    };

    long interval = 1000L;
    boolean autoStart = false;
    Map<String, Object> vars;

    public RepeatingTrigger(String name, File file, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(name, file, script);

        init();
    }

    public RepeatingTrigger(String name, File file, String script, long interval)
            throws AbstractTriggerManager.TriggerInitFailedException {
        this(name, file, script);

        this.interval = interval;
    }

    /**
     * This should be called at least once on start up so variables can be
     * initialized.
     */
    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars) {
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

    @Override
    public AbstractTriggerManager.Trigger clone() {
        try {
            return new RepeatingTrigger(this.triggerName, file, this.getScript(), interval);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "interval=" + interval +
                ", autoStart=" + autoStart +
                ", paused=" + paused +
                '}';
    }

    //////////////////////////////////////////////////////////////////////////////////////
    private boolean paused;

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
            throwableHandler.onFail(e);
        }

        try {
            vars.put(AbstractRepeatingTriggerManager.TRIGGER, "stop");
            activate(new Object(), vars);
        } catch (Exception e) {
            throwableHandler.onFail(e);
        }
    }

}
