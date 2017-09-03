package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public abstract class AbstractTriggerManager extends Manager {
    private static SelfReference common;

    private static Map<String, AbstractAPISupport> sharedVars = new HashMap<>();

    public AbstractTriggerManager(TriggerReactor plugin, SelfReference ref,  Map<String, Class<? extends AbstractAPISupport>> vars) {
        super(plugin);

        if(common == null)
            common = ref;

        for(Entry<String, Class<? extends AbstractAPISupport>> entry : vars.entrySet()){
            AbstractAPISupport.addSharedVar(sharedVars, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Delete this trigger from file system. It is abstract as we don't know what file structure the
     * TriggerManager is using.
     * @param trigger trigger to delete
     */
    protected abstract void deleteInfo(Trigger trigger);

    public static abstract class Trigger implements Cloneable{
        protected final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
        protected String triggerName;
        protected String script;

        protected Node root;
        protected Map<String, Executor> executorMap;
        protected Map<String, Object> gvarMap;

        private boolean sync = false;

        /**
         * This constructor <b>does not</b> initialize the fields. It is essential to call {@link #init()} method
         * in order to make the Trigger work properly. If you want to create a Trigger with customized
         * behavior, it's not necessary to call {@link #init()} but need to override {@link #initInterpreter(Map)},
         * {@link #startInterpretation(Object, Map, Interpreter, boolean)}, or {@link #activate(Object, Map)} method as your need
         * @param script
         */
        public Trigger(String triggerName, String script)  {
            super();

            this.triggerName = triggerName;
            this.script = script;
        }

        /**
         * Get this trigger's name.
         * @return
         */
        public String getTriggerName() {
            return triggerName;
        }

        public void setTriggerName(String triggerName) {
            this.triggerName = triggerName;
        }

        /**
         *
         * @throws IOException low level exception from Lexer
         * @throws LexerException throws if lexical analysis failed
         * @throws ParserException throws if parsing failed
         * @throws TriggerInitFailedException
         */
        public void init() throws TriggerInitFailedException{
            try{
                Charset charset = Charset.forName("UTF-8");

                Lexer lexer = new Lexer(script, charset);
                Parser parser = new Parser(lexer);

                root = parser.parse();
                executorMap = TriggerReactor.getInstance().getExecutorManager().getExecutorMap();
                gvarMap = TriggerReactor.getInstance().getVariableManager().getGlobalVariableAdapter();
            } catch (Exception ex) {
                throw new TriggerInitFailedException("Failed to initialize Trigger [" + this.getClass().getSimpleName()
                        + " -- " + triggerName + "]!", ex);
            }
        }

        /**
         * get the actual plain code of this trigger.
         * @return
         */
        public String getScript() {
            return script;
        }

        public void setScript(String script) throws TriggerInitFailedException {
            if(script == null)
                throw new RuntimeException("script cannot be null.");

            String temp = this.script;

            boolean failed = false;
            this.script = script;
            try{
                init();
            } catch (TriggerInitFailedException e) {
                failed = true;
                throw e;
            } finally {
                if(failed)
                    this.script = temp;
            }
        }

        /**
         * Check if this Trigger is sync mode.
         * @return
         */
        public boolean isSync() {
            return sync;
        }

        /**
         * Set this Trigger's sync mode.
         * @param sync
         */
        public void setSync(boolean sync) {
            this.sync = sync;
        }

        /**
         * Start this trigger. Variables in scriptVars may be overridden if it has same name as
         *  the name of fields of Event class.
         * @param e the Event associated with this Trigger
         * @param scriptVars the temporary local variables
         * @return true if activated; false if on cooldown
         */
        public boolean activate(Object e, Map<String, Object> scriptVars) {
            if(checkCooldown(e)){
                return false;
            }

            scriptVars.put("event", e);
            scriptVars.putAll(ReflectionUtil.extractVariablesWithEnumAsString(e));

            Interpreter interpreter = initInterpreter(scriptVars);

            startInterpretation(e, scriptVars, interpreter, isSync());
            return true;
        }

        /**
         *
         * @param e
         * @return true if cooldown; false if not cooldown or 'e' is not a compatible type
         */
        protected boolean checkCooldown(Object e) {
            UUID uuid = TriggerReactor.getInstance().extractUUIDFromContext(e);

            if(uuid != null){
                Long end = cooldowns.get(uuid);
                if(end != null && System.currentTimeMillis() < end){
                    return true;
                }

                return false;
            }

            return false;
        }

        /**
         * Create interpreter with appropriate options and variables.
         * @param scriptVars
         * @return
         */
        protected Interpreter initInterpreter(Map<String, Object> scriptVars) {
            Interpreter interpreter = new Interpreter(root, executorMap, gvarMap, scriptVars, common);
            interpreter.setSync(isSync());

            interpreter.getVars().putAll(sharedVars);

            return interpreter;
        }

        /**
         * Start interpreting the code.
         *
         * @param e
         *            The Event associated with this Trigger
         * @param scriptVars
         *            temporary variables
         * @param interpreter
         *            The Interpreter
         * @param sync
         *            set it true will make this method run in the thread that
         *            has called this method. This is useful when this trigger has to cancel an Event;
         *            set it to false will let it run in separate thread. This is more efficient if you
         *            only need to read data from Event and never interact with it.
         */
        protected void startInterpretation(Object e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            if(sync){
                start(e, scriptVars, interpreter, sync);
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        start(e, scriptVars, interpreter, sync);
                    }
                }).start();
            }
        }

        /**
         * The actual execution part. The Trigger can be sync/async depends on which thread invokes this method.
         * @param e
         * @param scriptVars
         * @param interpreter
         * @param sync
         */
        protected void start(Object e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            try{
                interpreter.startWithContextAndInterrupter(e, TriggerReactor.getInstance().createInterrupter(e, interpreter, cooldowns));
            }catch(Exception ex){
                TriggerReactor.getInstance().handleException(e, ex);
            }
        }

        @Override
        public abstract Trigger clone();
    }

    @SuppressWarnings("serial")
    public static final class TriggerInitFailedException extends Exception{

        public TriggerInitFailedException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}