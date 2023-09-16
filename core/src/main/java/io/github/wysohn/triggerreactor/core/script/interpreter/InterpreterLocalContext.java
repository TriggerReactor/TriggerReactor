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

package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Storage for the mutable states of the interpreter.
 * <p>
 * <p>
 * This should be used per individual thread to avoid any race-condition.
 * Each InterpreterLocalContext has all the necessary states, such as local variables,
 * the current stack, etc. that is relevant to the specific execution that a thread
 * is working on. In this way, a single Interpreter can be used by multiple threads,
 * and it is easier to transfer the current state to the another Interpreter (for example,
 * if we have to execute some part of AST in ASYNC blocks), all we have to do is to provide
 * the copy of the context.
 */
public class InterpreterLocalContext {
    /**
     * Simple lock to ensure only one thread to access the context at a time.
     * Each Thread that is running the trigger script must have their own context, and
     * they should not be shared.
     * <p>
     * Even though this attempt to avoid any concurrent access to the local context, it
     * does not know if the same context is used by two or more threads in a non-concurrent
     * manner.
     */
    private final DelegatedReentrantLock lock = new DelegatedReentrantLock();
    private final Stack<Token> stack = new Stack<>();
    /**
     * Used by the #WAIT executor to sleep the current thread.
     */
    final Object waitLock = new Object();

    private final Map<String, Class<?>> importMap = new HashMap<>();
    /**
     * Extra context information to be used only internally.
     */
    private final Map<String, Object> extras = new HashMap<>();

    private final Map<String, Object> vars = new HashMap<>();
    private ProcessInterrupter interrupter = null;

    private final Timings.Timing timing;

    private boolean stopFlag = false;
    private boolean waitFlag = false;
    private boolean breakFlag = false;
    private boolean continueFlag = false;

    private int callArgsSize = 0;

    public InterpreterLocalContext(Timings.Timing timing) {
        this(timing, null);
    }

    public InterpreterLocalContext(Timings.Timing timing, ProcessInterrupter interrupter) {
        this.timing = timing;
        this.interrupter = interrupter;
    }

    /**
     * Copy current state, except for the current stack.
     *
     * @param timingsName name to be used as the timing. Since the context will be
     *                    inherited from 'this' context, the timing will be attached
     *                    as the child of the timing which the original context has.
     * @return copied context.
     */
    public InterpreterLocalContext copyState(String timingsName) {
        return tryOrThrow(() -> {
            // attach lambda timings to the caller timings
            InterpreterLocalContext context = new InterpreterLocalContext(
                    Optional.ofNullable(timing).map(t -> t.getTiming(timingsName)).orElse(Timings.LIMBO), interrupter);

            context.importMap.putAll(importMap);
            context.vars.putAll(vars);
            //TODO what exactly it was used for?
            context.extras.putAll(extras);

            return context;
        });
    }

    private <R> R tryOrThrow(Supplier<R> fn) {
        if (!lock.tryLock())
            throw new ConcurrentModificationException("Owner: " + lock.getOwner());

        try {
            return fn.get();
        } finally {
            lock.unlock();
        }
    }

    int getCallArgsSize() {
        return tryOrThrow(() -> callArgsSize);
    }

    void setCallArgsSize(int callArgsSize) {
        tryOrThrow(() -> this.callArgsSize = callArgsSize);
    }

    boolean hasImport(String name) {
        return tryOrThrow(() -> importMap.containsKey(name));
    }

    void setImport(String name, Class<?> clazz) {
        tryOrThrow(() -> importMap.put(name, clazz));
    }

    Class<?> getImport(String name) {
        return tryOrThrow(() -> importMap.get(name));
    }

    Timings.Timing getTiming() {
        return tryOrThrow(() -> timing);
    }

    /**
     * Get the copy of the variable map.
     *
     * @return
     */
    @Deprecated
    public Map<String, Object> getVars() {
        return tryOrThrow(() -> new HashMap<>(vars));
    }

    //TODO this is a temporary solution. Use the dedicated getter/setters

    public Object getVar(String key) {
        return tryOrThrow(() -> vars.get(key));
    }

    public void setVar(String key, Object val) {
        tryOrThrow(() -> vars.put(key, val));
    }

    public void removeVar(String key) {
        tryOrThrow(() -> vars.remove(key));
    }

    public void clearVars() {
        tryOrThrow(vars::clear);
    }

    private void tryOrThrow(Runnable fn) {
        tryOrThrow(() -> {
            fn.run();
            return null;
        });
    }

    public InterpreterLocalContext putAllVars(Map<String, Object> scriptVars) {
        return tryOrThrow(() -> {
            vars.putAll(scriptVars.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll));
            return InterpreterLocalContext.this;
        });
    }

    public Object getExtra(String key) {
        return tryOrThrow(() -> extras.get(key));
    }

    public void setExtra(String key, Object val) {
        tryOrThrow(() -> extras.put(key, val));
    }

    boolean isBreakFlag() {
        return tryOrThrow(() -> breakFlag);
    }

    void setBreakFlag(boolean breakFlag) {
        tryOrThrow(() -> this.breakFlag = breakFlag);
    }

    boolean isContinueFlag() {
        return tryOrThrow(() -> continueFlag);
    }

    void setContinueFlag(boolean continueFlag) {
        tryOrThrow(() -> this.continueFlag = continueFlag);
    }

    boolean isStopFlag() {
        return tryOrThrow(() -> stopFlag);
    }

    void setStopFlag(boolean stopFlag) {
        tryOrThrow(() -> this.stopFlag = stopFlag);
    }

    boolean isWaitFlag() {
        return tryOrThrow(() -> waitFlag);
    }

    void setWaitFlag(boolean waitFlag) {
        tryOrThrow(() -> this.waitFlag = waitFlag);
    }

    Token popToken() {
        return tryOrThrow(this.stack::pop);
    }

    Token pushToken(Token token) {
        return tryOrThrow(() -> this.stack.push(token));
    }

    boolean stackEmpty() {
        return tryOrThrow(this.stack::empty);
    }

    public ProcessInterrupter getInterrupter() {
        return tryOrThrow(() -> interrupter);
    }

    public void setInterrupter(ProcessInterrupter interrupter) {
        tryOrThrow(() -> this.interrupter = interrupter);
    }

    public Map<String, Object> getVarCopy() {
        return tryOrThrow(() -> new HashMap<>(vars));
    }

    @Deprecated
    public Object getTriggerCause() {
        return tryOrThrow(() -> vars.get("event"));
    }

    private class DelegatedReentrantLock extends ReentrantLock {
        @Override
        protected Thread getOwner() {
            return super.getOwner();
        }
    }
}
