package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.tools.VarMap;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for the mutable states of the interpreter.
 * <p>
 * <p>
 * This should be used per individual thread to avoid any race-condition.
 * Each InterpreterContext has all the necessary states, such as local variables,
 * the current stack, etc. that is relevant to the specific execution that a thread
 * is working on. In this way, a single Interpreter can be used by multiple threads,
 * and it is easier to transfer the current state to the another Interpreter (for example,
 * if we have to execute some part of AST in ASYNC blocks), all we have to do is to provide
 * the copy of the context.
 */
public class InterpreterLocalContext {
    private final Stack<Token> stack = new Stack<>();

    //TODO later, do not allow multiple threads to access any of the fields.
    //TODO for now, just keep it as it is. (ConcurrentHashMap will keep it safe... for now)
    private final Map<String, Class<?>> importMap = new ConcurrentHashMap<>();
    private Map<String, Object> vars = new VarMap();

    //TODO this is very dangerous situation because we have only one local context per each thread.
    //TODO but for now, this is not a problem since in Trigger#initInterpreter(), we create
    //TODO new Interpreter every time for each execution, so we don't technically share any
    //TODO local context. But later we will refactor the Interpreter

    private Object triggerCause = null;
    private Timings.Timing timing = null;

    private boolean stopFlag = false;
    private boolean waitFlag = false;
    private boolean breakFlag = false;
    private boolean continueFlag = false;

    private int callArgsSize = 0;

    Map<String, Class<?>> getImportMap() {
        return importMap;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    void setVars(Map<String, Object> vars) {
        this.vars = vars;
    }

    Token pushToken(Token token){
        return this.stack.push(token);
    }

    Token popToken(){
        return this.stack.pop();
    }

    boolean stackEmpty() {
        return this.stack.empty();
    }

    public Object getTriggerCause() {
        return triggerCause;
    }

    void setTriggerCause(Object triggerCause) {
        this.triggerCause = triggerCause;
    }

    Timings.Timing getTiming() {
        return timing;
    }

    void setTiming(Timings.Timing timing) {
        this.timing = timing;
    }

    boolean isStopFlag() {
        return stopFlag;
    }

    void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    boolean isWaitFlag() {
        return waitFlag;
    }

    void setWaitFlag(boolean waitFlag) {
        this.waitFlag = waitFlag;
    }

    boolean isBreakFlag() {
        return breakFlag;
    }

    void setBreakFlag(boolean breakFlag) {
        this.breakFlag = breakFlag;
    }

    boolean isContinueFlag() {
        return continueFlag;
    }

    void setContinueFlag(boolean continueFlag) {
        this.continueFlag = continueFlag;
    }

    int getCallArgsSize() {
        return callArgsSize;
    }

    void setCallArgsSize(int callArgsSize) {
        this.callArgsSize = callArgsSize;
    }

    /**
     * Copy current state, except for the current stack
     * @return
     */
    public InterpreterLocalContext copyState() {
        InterpreterLocalContext context = new InterpreterLocalContext();

        context.importMap.putAll(importMap);
        context.vars.putAll(vars);

        context.triggerCause = triggerCause;
        context.timing = Optional.ofNullable(timing)
                .map(t -> t.getTiming("LAMBDA"))
                .orElse(Timings.LIMBO); // attach lambda timings to the caller timings

        return context;
    }
}
