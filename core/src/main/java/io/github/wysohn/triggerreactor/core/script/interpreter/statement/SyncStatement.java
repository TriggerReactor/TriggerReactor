package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class SyncStatement implements InterpreterExecutionUnit {
    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return token.getType() == Token.Type.SYNC;
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        try {
            try (Timings.Timing t = localContext.getTiming()
                    .getTiming("SYNC (WAITING)")
                    .begin(interpreter.globalContext.task.isServerThread())) {
                interpreter.globalContext.task.submitSync(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        try (Timings.Timing t = localContext.getTiming()
                                .getTiming("SYNC (RUNNING)")
                                .begin(interpreter.globalContext.task.isServerThread())) {
                            for (Node node : node.getChildren()) {
                                //ignore whatever returns as it's impossible
                                //to handle it from the caller
                                interpreter.next(node, localContext);
                            }
                        }
                        return null;
                    }

                }).get();
            }
            return true;
        } catch (InterruptedException | ExecutionException ex) {
            throw new InterpreterException("Synchronous task error.", ex);
        }
    }
}
