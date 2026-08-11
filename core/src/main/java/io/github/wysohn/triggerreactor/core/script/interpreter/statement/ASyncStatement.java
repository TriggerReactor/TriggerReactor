package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.*;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.tools.ExceptionUtil;

public class ASyncStatement implements InterpreterExecutionUnit {
    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return token.getType() == Token.Type.ASYNC;
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        // WARNING) Remember that 'localContext' is not thread-safe. It should not be
        //          used in the asynchronous task.
        final Object triggerCause = localContext.getTriggerCause();
        final InterpreterLocalContext copiedContext = localContext.copyState("ASYNC");
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        interpreter.globalContext.task.submitAsync(() -> {
            Node rootCopy = new Node(new Token(Token.Type.ROOT, "<ROOT>", -1, -1));
            rootCopy.getChildren().addAll(node.getChildren());

            Interpreter copy = InterpreterBuilder.start(interpreter.globalContext, rootCopy)
                    .build();

            try {
                copy.start(triggerCause, copiedContext);
            } catch (InterpreterException e) {
                ExceptionUtil.appendStackTraceAfter(e, ExceptionUtil.pushStackTrace(stackTrace,
                        new StackTraceElement(InterpreterException.class.getName(),
                                "[TriggerReactor ASYNC]",
                                "Interpreter.java",
                                -1),
                        0));
                interpreter.globalContext.exceptionHandle.handleException(triggerCause, e);
            }
        });

        return true;
    }
}
