package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.TriggerInitFailedException;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.warning.Warning;
import io.github.wysohn.triggerreactor.tools.StringUtils;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.observer.IObservable;
import io.github.wysohn.triggerreactor.tools.observer.IObserver;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class Trigger implements IObservable {
    @Inject
    InterpreterGlobalContext globalContext;
    @Inject
    IGameController gameController;
    @Inject
    IThrowableHandler throwableHandler;
    @Inject
    ExternalAPIManager externalAPIManager;
    @Inject
    IScriptEngineProvider scriptEngineProvider;

    protected final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    protected final TriggerInfo info;
    protected IObserver observer;
    protected String script;
    protected Node abstractSyntaxTree;

    /**
     * This constructor <b>does not</b> initialize the fields. It is essential to call {@link #compile()} method
     * in order to make the Trigger work properly. If you want to create a Trigger with customized
     * behavior, it's not necessary to call {@link #compile()} but need to override {@link #createInterpreter()} ()},
     * {@link #startInterpretation(Object, Map, Interpreter, boolean)}, or {@link #activate(Object, Map)} method as
     * your need
     */
    public Trigger(TriggerInfo info, String script) {
        this.info = info;
        this.script = script;

        ValidationUtil.notNull(this.info);
    }

    protected Trigger(Trigger other) {
        this.globalContext = other.globalContext;
        this.gameController = other.gameController;
        this.throwableHandler = other.throwableHandler;
        this.externalAPIManager = other.externalAPIManager;

        this.info = other.info;
        this.observer = other.observer;
        this.script = other.script;
        this.abstractSyntaxTree = other.abstractSyntaxTree;

        this.cooldowns.putAll(other.cooldowns);
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "=" + info + "]";
    }

    /**
     * The only difference is that it determines sync/async from the trigger config.
     *
     * @param scriptVars
     * @return
     */
    public boolean activate(Map<String, Object> scriptVars) {
        return activate(scriptVars, Optional.of(info).map(TriggerInfo::isSync).orElse(false));
    }

    /**
     * Start this trigger. Variables in scriptVars may be overridden if it has same name as
     * the name of fields of Event class.
     *
     * @param scriptVars the temporary local variables
     * @param sync       choose whether to run this trigger in the current thread or spawn a new thread
     *                   and run in there.
     * @return true if activated; false if on cooldown
     */
    public boolean activate(Map<String, Object> scriptVars, boolean sync) {
        if (checkCooldown(scriptVars.get(VAR_NAME_EVENT))) {
            return false;
        }

        scriptVars.putAll(externalAPIManager.getExternalAPIMap());
        Map<String, Object> customVars = gameController.getCustomVarsForTrigger(scriptVars.get(VAR_NAME_EVENT));
        if (customVars != null)
            scriptVars.putAll(customVars);

        Interpreter interpreter = createInterpreter();

        startInterpretation(scriptVars, interpreter, sync);
        return true;
    }

    /**
     * @param e
     * @return true if cooldown; false if not cooldown or 'e' is not a compatible type
     */
    protected boolean checkCooldown(Object e) {
        IPlayer iPlayer = gameController.extractPlayerFromContext(e);

        if (iPlayer != null) {
            UUID uuid = iPlayer.getUniqueId();

            if (uuid != null) {
                Long end = cooldowns.get(uuid);
                return end != null && System.currentTimeMillis() < end;
            }
        }
        return false;
    }

    protected Interpreter createInterpreter() {
        return new Interpreter(abstractSyntaxTree);
    }

    /**
     * Start interpreting the code.
     *
     * @param scriptVars  temporary variables
     * @param interpreter The Interpreter
     * @param sync        set it true will make this method run in the thread that
     *                    has called this method. This is useful when this trigger has to cancel an Event;
     *                    set it to false will let it run in separate thread. This is more efficient if you
     *                    only need to read data from Event and never interact with it.
     */
    protected void startInterpretation(Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
        Callable<Void> call = () -> {
            try (Timings.Timing t = Timings.getTiming(getTimingId()).begin(sync)) {
                start(t, scriptVars, interpreter);
            }
            return null;
        };

        if (sync) {
            if (gameController.isServerThread()) {
                try {
                    call.call();
                } catch (Exception e1) {

                }
            } else {
                Future<Void> future = gameController.callSyncMethod(call);
                try {
                    future.get(3, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e1) {

                } catch (TimeoutException e1) {
                    throwableHandler.handleException(scriptVars.get(VAR_NAME_EVENT), new RuntimeException(
                            "Took too long to process Trigger [" + info + "]! Is the server lagging?", e1));
                }
            }
        } else {
            ASYNC_POOL.submit(call);
        }
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
     * The actual execution part. The Trigger can be sync/async depends on which thread invokes this method.
     *
     * @param timing
     * @param scriptVars
     * @param interpreter
     */
    protected void start(Timings.Timing timing, Map<String, Object> scriptVars, Interpreter interpreter) {
        InterpreterLocalContext localContext = new InterpreterLocalContext(timing,
                gameController.createInterrupter(cooldowns));
        localContext.putAllVars(scriptVars);
        localContext.setExtra(Interpreter.SCRIPT_ENGINE_KEY, scriptEngineProvider.getEngine());

        try {
            interpreter.start(localContext, globalContext);
        } catch (Exception ex) {
            throwableHandler.handleException(localContext,
                    new Exception("Could not finish interpretation for [" + info + "]!", ex));
        }
    }

    public TriggerInfo getInfo() {
        return info;
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
            compile();
        } catch (TriggerInitFailedException e) {
            throw e;
        } finally {
            notifyObservers();
        }
    }

    /**
     * @throws IOException                low level exception from Lexer
     * @throws LexerException             throws if lexical analysis failed
     * @throws ParserException            throws if parsing failed
     * @throws TriggerInitFailedException
     */
    protected void compile() throws TriggerInitFailedException {
        try {
            if (script == null) {
                throw new NullPointerException(
                        "compile() was invoked, yet 'script' was null. Make sure to override init() method to in order to construct a customized Trigger.");
            }

            Charset charset = StandardCharsets.UTF_8;

            Lexer lexer = new Lexer(script, charset);
            Parser parser = new Parser(lexer);

            abstractSyntaxTree = parser.parse(true);
            List<Warning> warnings = parser.getWarnings();
        } catch (Exception ex) {
            throw new TriggerInitFailedException(
                    "Failed to initialize Trigger [" + this.getClass().getSimpleName() + " -- " + info + "]!", ex);
        }
    }

    @Override
    public void notifyObservers() {
        observer.onUpdate(this);
    }

    void setObserver(IObserver observer) {
        this.observer = observer;
    }

    /**
     * The actual execution part. The Trigger can be sync/async depends on which thread invokes this method.
     *
     * @param scriptVars
     * @param interpreter
     */
    protected void start(Map<String, Object> scriptVars, Interpreter interpreter) {
        start(Timings.LIMBO, scriptVars, interpreter);
    }

    public Trigger copy() {
        try {
            Constructor<?> copyConstructor = getClass().getConstructor(Trigger.class);
            return (Trigger) copyConstructor.newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static final ExecutorService ASYNC_POOL = Executors.newCachedThreadPool();
    public static final String VAR_NAME_EVENT = "event";
    public static final String VAR_NAME_PLAYER = "player";
}