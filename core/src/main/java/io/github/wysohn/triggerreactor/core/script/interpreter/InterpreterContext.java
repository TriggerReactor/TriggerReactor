package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.tools.VarMap;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;
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
public class InterpreterContext {
    final Map<String, Class<?>> importMap = new ConcurrentHashMap<>();
    Map<String, Object> vars = new VarMap();

    final Stack<Token> stack = new Stack<>();

    Object triggerCause = null;
    Timings.Timing timing = null;
    boolean sync = false;

    boolean stopFlag = false;
    boolean waitFlag = false;
    boolean breakFlag = false;
    boolean continueFlag = false;

    int callArgsSize = 0;
}
