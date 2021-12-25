package io.github.wysohn.triggerreactor.core.script.validation;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    private static final long serialVersionUID = 1L;
}
