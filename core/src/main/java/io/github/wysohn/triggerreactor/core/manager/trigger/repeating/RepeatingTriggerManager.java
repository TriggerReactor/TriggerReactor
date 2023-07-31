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

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.TimeUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class RepeatingTriggerManager extends AbstractTriggerManager<RepeatingTrigger> {
    @Inject
    private TaskSupervisor task;

    protected static final String TRIGGER = "trigger";

    protected final Map<String, Thread> runningThreads = new ConcurrentHashMap<>();


    @Inject
    private RepeatingTriggerManager(@Named("DataFolder") File dataFolder,
                                    @Named("RepeatingTriggerManagerFolder") String folderName) {
        super(new File(dataFolder, folderName));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
        super.reload();

        for (Entry<String, Thread> entry : runningThreads.entrySet()) {
            entry.getValue().interrupt();
        }
        runningThreads.clear();

        for (RepeatingTrigger trigger : getAllTriggers()) {
            final RepeatingTrigger triggerCopy = trigger;
            //start 1 tick later so other managers can be initialized.
            task.runTask(() -> {
                if (triggerCopy.isAutoStart()) {
                    startTrigger(trigger.getInfo().getTriggerName());
                }
            });
        }
    }

    @Override
    public void shutdown() {

    }

    /**
     * Create trigger.
     *
     * @param triggerName name of the trigger.
     * @param script      the code.
     * @param interval    interval in milliseconds.
     * @return true on success; false if already exists.
     * @throws IOException     See {@link Trigger#init()}
     * @throws LexerException  See {@link Trigger#init()}
     * @throws ParserException See {@link Trigger#init()}
     */
    public boolean createTrigger(String triggerName, File file, String script, long interval)
            throws TriggerInitFailedException, IOException {
        if (get(triggerName) != null) {
            return false;
        }

        String name = TriggerInfo.extractName(file);
        IConfigSource config = configSourceFactory.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        RepeatingTrigger trigger = new RepeatingTrigger(info, script, interval);
        put(triggerName, trigger);

        return true;
    }

    /**
     * Create trigger. Interval is 1000L by default.
     *
     * @param triggerName name of the trigger.
     * @param script      the code.
     * @return true on success; false if already exists.
     * @throws IOException     See {@link Trigger#init()}
     * @throws LexerException  See {@link Trigger#init()}
     * @throws ParserException See {@link Trigger#init()}
     */
    public boolean createTrigger(String triggerName, String script)
            throws TriggerInitFailedException, IOException {
        File triggerFile = getTriggerFile(folder, triggerName, true);
        return createTrigger(triggerName, triggerFile, script, 1000L);
    }

    @Override
    public RepeatingTrigger remove(String name) {
        RepeatingTrigger remove = super.remove(name);

        // stop the thread if it's running
        if (runningThreads.containsKey(remove.getInfo().getTriggerName())) {
            this.stopTrigger(remove.getInfo().getTriggerName());
        }

        return remove;
    }

    /**
     * Checks whether the specified trigger is running. However, this also can
     * return false even if the trigger with name 'triggerName' does not exists.
     *
     * @param triggerName
     * @return
     */
    public boolean isRunning(String triggerName) {
        return runningThreads.containsKey(triggerName);
    }

    /**
     * Attempts to start the trigger with provided trigger name. Return false if
     * no such trigger with that name, yet return value of true does not
     * necessarily start the thread; it checks if the trigger thread is already
     * running, and if it is running, it will skip. So return value true
     * actually just guarantees that the repeating trigger is running. If you
     * want to check whether the repeating trigger is running, use
     * {@link #isRunning(String)} instead.
     *
     * @param triggerName name of the repeating trigger.
     * @return true on success; false if trigger not found.
     */
    public boolean startTrigger(String triggerName) {
        RepeatingTrigger trigger = get(triggerName);
        if (trigger == null) {
            return false;
        }

        if (!isRunning(triggerName)) {
            Map<String, Object> vars = new HashMap<>();
            vars.put(TRIGGER, "init");

            trigger.activate(new Object(), vars, true);

            Thread thread = task.newThread(trigger,
                                           "RepeatingTrigger-" + triggerName,
                                           Thread.MIN_PRIORITY + 1);
            thread.start();

            runningThreads.put(triggerName, thread);
        }

        return true;
    }

    /**
     * Attempts to stop the trigger.
     *
     * @param triggerName name of the repeating trigger.
     * @return true on success; false if no such trigger found with name.
     */
    public boolean stopTrigger(String triggerName) {
        Thread thread = runningThreads.remove(triggerName);
        if (thread == null) {
            return false;
        }

        thread.interrupt();
        return true;
    }

    public void showTriggerInfo(ICommandSender sender, RepeatingTrigger trigger) {
        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: " + trigger.getInfo());
        sender.sendMessage("Auto Start: " + trigger.isAutoStart());
        sender.sendMessage("Interval: " + TimeUtil.milliSecondsToString(trigger.getInterval()));
        sender.sendMessage("");
        sender.sendMessage("Paused: " + trigger.isPaused());
        sender.sendMessage("Running: " + isRunning(trigger.getInfo().getTriggerName()));
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    protected interface ThrowableHandler {
        void onFail(Throwable throwable);
    }

}