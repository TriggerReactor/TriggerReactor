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
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.annotation.TriggerRuntimeDependency;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class RepeatingTrigger extends Trigger implements Runnable {
    @Inject
    private IRepeatingTriggerFactory factory;

    @Inject
    private Logger logger;
    @Inject
    private IExceptionHandle exceptionHandle;

    @TriggerRuntimeDependency
    private Map<String, Object> vars;

    @AssistedInject
    private RepeatingTrigger(@Assisted TriggerInfo info,
                             @Assisted String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
    }

    /**
     * This should be called at least once on start up so variables can be
     * initialized.
     */
    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars, boolean sync) {
        ValidationUtil.notNull(scriptVars);

        boolean result = super.activate(e, scriptVars, sync);

        // update variable state, so we can use it in the next iteration.
        vars = new HashMap<>();
        Optional.ofNullable(getVarCopy())
                .ifPresent(vars::putAll);

        return result;
    }

    /**
     * We don't use cooldown for this trigger. Just return false always
     */
    @Override
    protected boolean checkCooldown(Object e) {
        return false;
    }

    public long getInterval() {
        return info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, Long.class)
                .filter(i -> i > 0L)
                .orElse(1000L);
    }

    public void setInterval(long interval) {
        info.put(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, interval);
    }

    public boolean isAutoStart() {
        return info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, Boolean.class)
                .orElse(true);
    }

    public void setAutoStart(boolean autoStart) {
        info.put(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, autoStart);
    }

    @Override
    public RepeatingTrigger clone() {
        return factory.create(info, script);
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "interval=" + getInterval() +
                ", autoStart=" + isAutoStart() +
                ", paused=" + paused +
                '}';
    }

    //////////////////////////////////////////////////////////////////////////////////////
    @TriggerRuntimeDependency
    private boolean paused;
    @TriggerRuntimeDependency
    private boolean running = true;

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

    public void stop() {
        if (!running)
            return;

        running = false;

        synchronized (this) {
            this.notify();
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.interrupted()) {
                synchronized (this) {
                    while (paused && !Thread.interrupted()) {
                        this.wait();
                    }
                }

                if (!running)
                    break;

                task("repeat");

                Thread.sleep(getInterval());
            }
        } catch (InterruptedException e) {
            // ignore
        } catch (Exception e) {
            exceptionHandle.handleException(null, e);
        }

        try {
            task("stop");
        } catch (Exception e) {
            exceptionHandle.handleException(null, e);
        }
    }

    void task(String trigger) {
        vars.put(RepeatingTriggerManager.TRIGGER, trigger);
        // we re-use the variables over and over.
        activate(new Object(), vars, true);
    }
}
