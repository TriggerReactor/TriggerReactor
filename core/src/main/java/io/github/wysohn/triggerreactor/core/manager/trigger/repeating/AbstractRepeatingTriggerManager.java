/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.TimeUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRepeatingTriggerManager extends AbstractTriggerManager<RepeatingTrigger> {
    protected static final String TRIGGER = "trigger";

    protected final Map<String, Thread> runningThreads = new ConcurrentHashMap<>();

    @Override
    public void reload() {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        triggers.clear();
        for (Entry<String, Thread> entry : runningThreads.entrySet()) {
            entry.getValue().interrupt();
        }
        runningThreads.clear();

        for (File ymlfile : folder.listFiles(filter)) {
            String triggerName = extractName(ymlfile);

            boolean autoStart = false;
            int interval = 1000;
            try {
                autoStart = this.getData(ymlfile, "AutoStart", false);
                interval = this.getData(ymlfile, "Interval", 1000);
            } catch (IOException e2) {
                e2.printStackTrace();
            }


            File triggerFile = null;
            String script = null;
            try {
                triggerFile = getTriggerFile(folder, triggerName, false);
                script = FileUtil.readFromFile(triggerFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            RepeatingTrigger trigger = null;
            try {
                trigger = new RepeatingTrigger(triggerName, triggerFile, script, interval);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            trigger.setAutoStart(autoStart);
            trigger.setInterval(interval);

            triggers.put(triggerName, trigger);

            final RepeatingTrigger triggerCopy = trigger;
            //start 1 tick later so other managers can be initialized.
            plugin.runTask(new Runnable() {
                @Override
                public void run() {
                    if (triggerCopy.isAutoStart()) {
                        startTrigger(triggerName);
                    }
                }
            });
        }
    }

    @Override
    public void saveAll() {
        for (Entry<String, RepeatingTrigger> entry : triggers.entrySet()) {
            String triggerName = entry.getKey();
            RepeatingTrigger trigger = entry.getValue();

            File ymlfile = new File(folder, triggerName + ".yml");
            File triggerfile = getTriggerFile(folder, triggerName, true);

            try {
                this.setData(ymlfile, "AutoStart", trigger.isAutoStart());
                this.setData(ymlfile, "Interval", trigger.getInterval());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileUtil.writeToFile(triggerfile, trigger.getScript());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get Repeating Trigger with specified name.
     *
     * @param triggerName name of trigger
     * @return Repeating Trigger if found; null if not found.
     */
    public RepeatingTrigger getTrigger(String triggerName) {
        return triggers.get(triggerName);
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
        if (getTrigger(triggerName) != null) {
            return false;
        }

        RepeatingTrigger trigger = new RepeatingTrigger(triggerName, file, script, interval);
        triggers.put(triggerName, trigger);

        saveInfo(trigger);

        return true;
    }

    protected abstract void saveInfo(RepeatingTrigger trigger) throws IOException;

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

    /**
     * Completely clean up the Repeating Trigger. This also stops the thread if
     * one was running already.
     *
     * @param triggerName name of the trigger
     * @return true on success; false if trigger with the name not found.
     */
    public boolean deleteTrigger(String triggerName) {
        RepeatingTrigger trigger = triggers.remove(triggerName);
        if (trigger == null) {
            return false;
        }

        // stop the thread if it's running
        if (runningThreads.containsKey(triggerName)) {
            this.stopTrigger(triggerName);
        }

        deleteInfo(trigger);

        return true;
    }

    @Override
    protected void deleteInfo(RepeatingTrigger trigger) {
        FileUtil.delete(new File(trigger.getFile().getParent(), trigger.getTriggerName() + ".yml"));
        super.deleteInfo(trigger);
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
        RepeatingTrigger trigger = triggers.get(triggerName);
        if (trigger == null) {
            return false;
        }

        if (!isRunning(triggerName)) {
            Map<String, Object> vars = new HashMap<>();
            vars.put(TRIGGER, "init");

            trigger.setSync(true);
            trigger.activate(new Object(), vars);
            trigger.setSync(false);

            Thread thread = new Thread(trigger);
            thread.setName("TRG Repeating Trigger -- " + triggerName);
            thread.setPriority(Thread.MIN_PRIORITY + 1);
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
        sender.sendMessage("Trigger: " + trigger.getTriggerName());
        sender.sendMessage("Auto Start: " + trigger.isAutoStart());
        sender.sendMessage("Interval: " + TimeUtil.milliSecondsToString(trigger.interval));
        sender.sendMessage("");
        sender.sendMessage("Paused: " + trigger.isPaused());
        sender.sendMessage("Running: " + isRunning(trigger.getTriggerName()));
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    protected interface ThrowableHandler {
        void onFail(Throwable throwable);
    }

    public AbstractRepeatingTriggerManager(TriggerReactorCore plugin, File tirggerFolder) {
        super(plugin, tirggerFolder);
    }

}