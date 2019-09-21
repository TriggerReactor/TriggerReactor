package io.github.wysohn.triggerreactor.core.script.parser;

import io.github.wysohn.triggerreactor.core.script.Token;

public interface DeprecationSupervisor {
    /**
     * Check if this value is deprecated so that the parser can warn user while parsing the code.
     * @param type the type of deprecated token
     * @param value the actual value of token
     * @return true if deprecated; false otherwise
     */
    boolean isDeprecated(Token.Type type, String value);
}
