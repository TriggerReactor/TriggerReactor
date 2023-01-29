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

package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.util.Map;
import java.util.logging.Logger;

public class RepeatingTrigger extends Trigger implements Runnable {
    @Inject
    private Logger logger;

    private long interval = 1000L;
    private boolean autoStart = false;
    private Map<String, Object> vars;

    @AssistedInject
    private RepeatingTrigger(@Assisted TriggerInfo info,
                             @Assisted String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
    }

    @AssistedInject
    public RepeatingTrigger(@Assisted TriggerInfo info,
                            @Assisted String script,
                            @Assisted long interval) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.interval = interval;
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
    public RepeatingTrigger clone() {
        try {
            return new RepeatingTrigger(info, script, interval);
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

                vars.put(RepeatingTriggerManager.TRIGGER, "repeat");

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
            vars.put(RepeatingTriggerManager.TRIGGER, "stop");
            activate(new Object(), vars);
        } catch (Exception e) {
            throwableHandler.onFail(e);
        }
    }

    private final RepeatingTriggerManager.ThrowableHandler throwableHandler =
            new RepeatingTriggerManager.ThrowableHandler() {
                @Override
                public void onFail(Throwable throwable) {
                    throwable.printStackTrace();
                    logger.warning("Repeating Trigger [" + getInfo() + "] encountered an error!");
                    logger.warning(throwable.getMessage());
                    logger.warning("If you are an administrator, see console for more details.");
                }
            };
}
