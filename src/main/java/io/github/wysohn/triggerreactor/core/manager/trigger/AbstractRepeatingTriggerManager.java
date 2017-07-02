package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.TimeUtil;

public abstract class AbstractRepeatingTriggerManager extends TriggerManager {
    protected static final String TRIGGER = "trigger";

    protected final Map<String, RepeatingTrigger> repeatTriggers = new ConcurrentHashMap<>();
    protected final Map<String, Thread> runningThreads = new ConcurrentHashMap<>();

    /**
     * Get Repeating Trigger with specified name.
     *
     * @param triggerName
     *            name of trigger
     * @return Repeating Trigger if found; null if not found.
     */
    public RepeatingTrigger getTrigger(String triggerName) {
        return repeatTriggers.get(triggerName);
    }

    /**
     *
     * Create trigger.
     *
     * @param triggerName
     *            name of the trigger.
     * @param script
     *            the code.
     * @param interval
     *            interval in milliseconds.
     * @return true on success; false if already exists.
     * @throws IOException
     *             See {@link Trigger#init()}
     * @throws LexerException
     *             See {@link Trigger#init()}
     * @throws ParserException
     *             See {@link Trigger#init()}
     */
    public boolean createTrigger(String triggerName, String script, long interval)
            throws IOException, LexerException, ParserException {
        if (getTrigger(triggerName) != null) {
            return false;
        }

        RepeatingTrigger trigger = new RepeatingTrigger(triggerName, script, interval);
        repeatTriggers.put(triggerName, trigger);

        saveInfo(trigger);

        return true;
    }

    protected abstract void saveInfo(RepeatingTrigger trigger) throws IOException;

    /**
     * Create trigger. Interval is 1000L by default.
     *
     * @param triggerName
     *            name of the trigger.
     * @param script
     *            the code.
     * @return true on success; false if already exists.
     * @throws IOException
     *             See {@link Trigger#init()}
     * @throws LexerException
     *             See {@link Trigger#init()}
     * @throws ParserException
     *             See {@link Trigger#init()}
     */
    public boolean createTrigger(String triggerName, String script)
            throws IOException, LexerException, ParserException {
        return createTrigger(triggerName, script, 1000L);
    }

    /**
     * Completely clean up the Repeating Trigger. This also stops the thread if
     * one was running already.
     *
     * @param triggerName
     *            name of the trigger
     * @return true on success; false if trigger with the name not found.
     */
    public boolean deleteTrigger(String triggerName) {
        RepeatingTrigger trigger = repeatTriggers.remove(triggerName);
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
     * @param triggerName
     *            name of the repeating trigger.
     * @return true on success; false if trigger not found.
     */
    public boolean startTrigger(String triggerName) {
        RepeatingTrigger trigger = repeatTriggers.get(triggerName);
        if (trigger == null) {
            return false;
        }

        if (!isRunning(triggerName)) {
            Map<String, Object> vars = new HashMap<>();
            vars.put(TRIGGER, "init");

            trigger.activate(new Object(), vars);

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
     * @param triggerName
     *            name of the repeating trigger.
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

    public static class RepeatingTrigger extends Trigger implements Runnable {
        private final ThrowableHandler throwableHandler = new ThrowableHandler() {
            @Override
            public void onFail(Throwable throwable) {
                throwable.printStackTrace();
                TriggerReactor.getInstance().getLogger()
                        .warning("Repeating Trigger [" + triggerName + "] encountered an error!");
                TriggerReactor.getInstance().getLogger().warning(throwable.getMessage());
                TriggerReactor.getInstance().getLogger()
                        .warning("If you are an administrator, see console for more details.");
            }
        };

        private long interval = 1000L;
        private boolean autoStart = false;
        private Map<String, Object> vars;

        public RepeatingTrigger(String name, String script) throws IOException, LexerException, ParserException {
            super(name, script);

            init();
        }

        public RepeatingTrigger(String name, String script, long interval)
                throws IOException, LexerException, ParserException {
            this(name, script);

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
        public Trigger clone() {
            try {
                return new RepeatingTrigger(this.triggerName, this.getScript(), interval);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }

            return null;
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

                    vars.put(TRIGGER, "repeat");

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
                vars.put(TRIGGER, "stop");
                activate(new Object(), vars);
            } catch (Exception e) {
                throwableHandler.onFail(e);
            }
        }

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

    public AbstractRepeatingTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}