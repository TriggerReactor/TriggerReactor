package io.github.wysohn.triggerreactor.tools;

public class ExceptionUtil {
    /**
     * @param exceptionToModify
     * @param newStackTrace
     */
    public static void appendStackTrace(Throwable exceptionToModify,
                                        StackTraceElement[] newStackTrace) {
        StackTraceElement[] existingStackTrace = exceptionToModify.getStackTrace();
        StackTraceElement[] combinedStackTrace = new StackTraceElement[existingStackTrace.length + newStackTrace.length];

        System.arraycopy(existingStackTrace, 0, combinedStackTrace, 0, existingStackTrace.length);
        System.arraycopy(newStackTrace, 0, combinedStackTrace, existingStackTrace.length, newStackTrace.length);

        exceptionToModify.setStackTrace(combinedStackTrace);
    }
}
