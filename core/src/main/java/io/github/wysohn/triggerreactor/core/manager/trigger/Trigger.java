/*
 * Copyright (C) 2023. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.TriggerInitFailedException;
import io.github.wysohn.triggerreactor.core.script.interpreter.*;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.StringUtils;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.observer.IObservable;
import io.github.wysohn.triggerreactor.tools.observer.IObserver;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class Trigger implements Cloneable, IObservable {
    @Inject
    private ITriggerDependencyFacade triggerDependencyFacade;
    @Inject
    private TaskSupervisor taskSupervisor;
    @Inject
    private IExceptionHandle exceptionHandle;
    @Inject
    private InterpreterGlobalContext globalContext;

    protected final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    protected final TriggerInfo info;

    protected IObserver observer;

    protected String script;
    protected Node root;

    private Interpreter interpreter;
    private ExecutingTrigger lastExecution;
    private boolean ignoreSyncIfNotServerThread = false;

    /**
     * This constructor <b>does not</b> initialize the fields. It is essential to call {@link #init()} method
     * in order to make the Trigger work properly. If you want to create a Trigger with customized
     * behavior, it's not necessary to call {@link #init()} but need to override {@link #initInterpreter(Map)},
     * {@link #startInterpretation(Object, Map, Interpreter, boolean)}, or {@link #activate(Object, Map)} method as
     * your need
     */
    public Trigger(TriggerInfo info, String script) {
        super();
        this.info = info;
        this.script = script;

        ValidationUtil.notNull(this.info);
    }

    @Override
    public void notifyObservers() {
        observer.onUpdate(this);
    }

    public TriggerInfo getInfo() {
        return info;
    }

    void setObserver(IObserver observer) {
        this.observer = observer;
    }

    /**
     * Get unique id to be used as fully qualified name for the Timings System.
     * Override to alter the id.
     *
     * @return the id.
     */
    protected String getTimingId() {
        return StringUtils.dottedPath(getClass().getSimpleName(), info.getTriggerName());
    }

    /**
     * @throws IOException                low level exception from Lexer
     * @throws LexerException             throws if lexical analysis failed
     * @throws ParserException            throws if parsing failed
     * @throws TriggerInitFailedException
     */
    public void init() throws TriggerInitFailedException {
        try {
            if (script == null) {
                throw new NullPointerException("init() was invoked, yet 'script' was null. Make sure to override " +
                        "init() method in order to construct a customized Trigger.");
            }

            Charset charset = StandardCharsets.UTF_8;

            Lexer lexer = new Lexer(script, charset);
            Parser parser = new Parser(lexer);

            root = parser.parse(true);

            // This allows us to re-use the same AST for multiple threads,
            //   though, we need to absolutely make sure
            //   that Interpreter does not share any state between threads
            //   except for the AST and the global context.
            interpreter = InterpreterBuilder.start(globalContext, root)
                    .build();
        } catch (Exception ex) {
            throw new TriggerInitFailedException("Failed to initialize Trigger [" + this.getClass().getSimpleName()
                    + " -- " + info + "]!", ex);
        }
    }

    /**
     * get the actual plain code of this trigger.
     *
     * @return
     */
    public String getScript() {
        return script;
    }

    public void setScript(String script) throws TriggerInitFailedException {
        if (script == null)
            throw new RuntimeException("script cannot be null.");

        this.script = script;
        try {
            init();
        } catch (TriggerInitFailedException e) {
            throw e;
        }
    }

    /**
     * Read {@link #activate(Object, Map, boolean)}
     * <p>
     * The only difference is that it determines sync/async from the trigger config.
     *
     * @param e
     * @param scriptVars
     * @return
     */
    public boolean activate(Object e, Map<String, Object> scriptVars) {
        return activate(e, scriptVars, Optional.of(info)
                .map(TriggerInfo::isSync)
                .orElse(false));
    }

    /**
     * Start this trigger. Variables in scriptVars may be overridden if it has same name as
     * the name of fields of Event class.
     *
     * @param e          the Event associated with this Trigger
     * @param scriptVars the temporary local variables
     * @param sync       choose whether to run this trigger in the current thread or spawn a new thread
     *                   and run in there.
     * @return true if activated; false if on cooldown
     */
    public boolean activate(Object e, Map<String, Object> scriptVars, boolean sync) {
        IPlayer iPlayer = triggerDependencyFacade.extractPlayerFromContext(e);

        if (checkCooldown(iPlayer)) {
            return false;
        }

        scriptVars.put("event", e);
        scriptVars.put("cooldown", new TriggerCooldownProxy(this));
        scriptVars.putAll(triggerDependencyFacade.getExtraVariables(e));

        startInterpretation(e, scriptVars, interpreter, sync);
        return true;
    }

    /**
     * Check if this trigger is on cooldown caused by #COOLDOWN executor.
     *
     * @param iPlayer the player to check the cooldown
     * @return true if cooldown; false if not cooldown or 'e' is not a compatible type
     */
    protected boolean checkCooldown(IPlayer iPlayer) {
        if (iPlayer != null) {
            return checkCooldown(iPlayer.getUniqueId());
        }
        return false;
    }

    /**
     * Check if this trigger is on cooldown caused by #COOLDOWN executor.
     *
     * @param playerUuid the uuid of the player to check the cooldown
     * @return true if cooldown; false if not cooldown or 'e' is not a compatible type
     */
    public boolean checkCooldown(UUID playerUuid) {
        if (playerUuid != null) {
            Long end = cooldowns.get(playerUuid);
            return end != null && System.currentTimeMillis() < end;
        }
        return false;
    }

    /**
     * Check time until cooldown ends in milliseconds.
     *
     * @param iPlayer the player to check the cooldown
     * @return time until cooldown ends in milliseconds; 0 if not cooldown or 'e' is not a compatible type
     */
    protected long checkCooldownUntilMillis(IPlayer iPlayer) {
        if (iPlayer != null) {
            return checkCooldownUntilMillis(iPlayer.getUniqueId());
        }
        return 0;
    }

    /**
     * Check time until cooldown ends in milliseconds.
     *
     * @param playerUuid the uuid of the player to check the cooldown
     * @return time until cooldown ends in milliseconds; 0 if not cooldown or 'e' is not a compatible type
     */
    public long checkCooldownUntilMillis(UUID playerUuid) {
        if (playerUuid != null) {
            Long end = cooldowns.get(playerUuid);
            if (end == null) return 0;

            long current = System.currentTimeMillis();
            return Math.max(0, end - current);
        }
        return 0;
    }

    protected ProcessInterrupter createInterrupter() {
        return triggerDependencyFacade.createInterrupter(cooldowns);
    }

    /**
     * Start interpreting the code.
     *
     * @param e           The Event associated with this Trigger
     * @param scriptVars  temporary variables
     * @param interpreter The Interpreter
     * @param sync        set it true will make this method run in the thread that
     *                    has called this method. This is useful when this trigger has to cancel an Event;
     *                    set it to false will let it run in separate thread. This is more efficient if you
     *                    only need to read data from Event and never interact with it.
     */
    protected void startInterpretation(Object e,
                                       Map<String, Object> scriptVars,
                                       Interpreter interpreter,
                                       boolean sync) {
        ExecutingTrigger executingTrigger = new ExecutingTrigger(
                exceptionHandle,
                info,
                e,
                interpreter,
                scriptVars,
                createInterrupter(),
                getTimingId()
        );

        if (sync) {
            if (taskSupervisor.isServerThread()) {
                try {
                    new TaskWrapper(e, executingTrigger, true).call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            // if task is initiated from non-server thread, and this flag is set to true, then simply execute the task
            //   from the current thread.
            else if (ignoreSyncIfNotServerThread) {
                try {
                    new TaskWrapper(e, executingTrigger, false).call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                Future<Void> future = taskSupervisor.submitSync(new TaskWrapper(e, executingTrigger, true));
                try {
                    future.get(3, TimeUnit.SECONDS);
                } catch (InterruptedException e1) {
                    // ignore
                } catch (ExecutionException e1) {
                    exceptionHandle.handleException(e, new RuntimeException(
                            "Failed to process Trigger [" + info + "]!",
                            e1.getCause()));
                } catch (TimeoutException e1) {
                    exceptionHandle.handleException(e, new RuntimeException(
                            "Took too long to process Trigger [" + info + "]! Is the server lagging?",
                            e1));
                }
            }
        } else {
            taskSupervisor.submitAsync(() -> {
                try {
                    new TaskWrapper(e, executingTrigger, false).call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    /**
     * Get the copy of variables that were used in the last execution.
     *
     * @return null if the trigger is still running or has not been executed yet.
     */
    public synchronized Map<String, Object> getVarCopy() {
        return Optional.ofNullable(lastExecution)
                .map(ExecutingTrigger::getLocalContext)
                .map(InterpreterLocalContext::getVarCopy)
                .orElse(null);
    }

    public synchronized Object getResult() {
        return Optional.ofNullable(lastExecution)
                .map(ExecutingTrigger::getInterpreter)
                .map(i -> {
                    try {
                        return i.result(lastExecution.localContext);
                    } catch (InterpreterException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
    }

    public boolean isIgnoreSyncIfNotServerThread() {
        return ignoreSyncIfNotServerThread;
    }

    /**
     * Set whether to ignore the sync flag if the current thread is not the server thread.
     * The original behavior is that if the current thread is not the server thread, then
     * the task is scheduled to run in the server thread, hence, no matter what thread the
     * task is initiated from, it will always run in the server thread. And of course, if the
     * current thread is the server thread, then the task will run in the current thread as it
     * is already synchronous.
     * <p>
     * However, this is not always desirable. For example, RepeatingTriggers are activated
     * from a non-server thread, yet we don't want it to run in the server thread too, but setting
     * it async would spawn another thread under current implementation, which would cause timing issues
     * (eg. the task is scheduled to run every 1 second, but it takes 2 seconds to finish, then the next task
     * would be scheduled to run 1 second after the previous task, which then is executed before
     * the previous task is done, making both of them running at the same time).
     *
     * @param ignoreSyncIfNotServerThread true to ignore the sync flag if the current thread is not the server thread.
     */
    public void setIgnoreSyncIfNotServerThread(boolean ignoreSyncIfNotServerThread) {
        this.ignoreSyncIfNotServerThread = ignoreSyncIfNotServerThread;
    }

    @Override
    public abstract Trigger clone();

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "=" + info + "]";
    }

    private class TaskWrapper implements Callable<Void> {
        private final Object e;
        private final ExecutingTrigger executingTrigger;
        private final boolean sync;

        private TaskWrapper(Object e, ExecutingTrigger executingTrigger, boolean sync) {
            this.e = e;
            this.executingTrigger = executingTrigger;
            this.sync = sync;
        }

        @Override
        public Void call() throws Exception {
            try {
                executingTrigger.sync = sync;
                executingTrigger.call();
            } catch (Exception ex) {
                exceptionHandle.handleException(e, ex);
            } finally {
                Trigger.this.lastExecution = executingTrigger;
            }
            return null;
        }
    }

    public static class ExecutingTrigger implements Callable<Void> {
        private final IExceptionHandle exceptionHandle;
        private final TriggerInfo info;

        private final Object e;
        private final Interpreter interpreter;

        private final String timingId;

        private final InterpreterLocalContext localContext;

        private boolean isDone = false;
        private boolean sync;

        public ExecutingTrigger(IExceptionHandle exceptionHandle,
                                TriggerInfo info,
                                Object e,
                                Interpreter interpreter,
                                Map<String, Object> initialVars,
                                ProcessInterrupter interrupter,
                                String timingId) {
            this.exceptionHandle = exceptionHandle;
            this.info = info;
            this.e = e;
            this.interpreter = interpreter;
            this.timingId = timingId;

            this.localContext = new InterpreterLocalContext(Timings.getTiming(timingId))
                    .putAllVars(initialVars);
            this.localContext.setInterrupter(interrupter);
        }

        @Override
        public Void call() throws Exception {
            // execution must be created every time and executed only once.
            if (isDone) throw new IllegalStateException("Cannot reuse this object!");

            try (Timings.Timing t = Timings.getTiming(timingId).begin(sync)) {
                EXECUTING_TRIGGER.set(info.toString());
                interpreter.start(e, localContext);
            } catch (Exception ex) {
                exceptionHandle.handleException(e, new Exception(
                        "Trigger [" + info + "] produced an error!", ex));
            } finally {
                isDone = true;
                EXECUTING_TRIGGER.remove();
            }
            return null;
        }

        public InterpreterLocalContext getLocalContext() {
            return localContext;
        }

        public Interpreter getInterpreter() {
            return interpreter;
        }

        // This might not be perfectly reliable, but it should do the job for most cases.
        private static final ThreadLocal<String> EXECUTING_TRIGGER = new ThreadLocal<>();

        public static String getExecutingTriggerSummary() {
            return EXECUTING_TRIGGER.get();
        }
    }
}