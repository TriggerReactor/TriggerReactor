package io.github.wysohn.triggerreactor.core.script.interpreter.lambda;

import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class LambdaParameter {
    final String id;
    final Object defValue;

    public LambdaParameter(Node idNode) {
        id = (String) idNode.getToken().value;
        defValue = null;
    }
}
