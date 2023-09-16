package io.github.wysohn.triggerreactor.tools;

public class ExceptionUtil {
    /**
     * @param exceptionToModify
     * @param newStackTrace
     */
    public static void appendStackTraceAfter(Throwable exceptionToModify,
                                             StackTraceElement[] newStackTrace) {
        StackTraceElement[] existingStackTrace = exceptionToModify.getStackTrace();
        StackTraceElement[] combinedStackTrace = new StackTraceElement[existingStackTrace.length + newStackTrace.length];

        System.arraycopy(existingStackTrace, 0, combinedStackTrace, 0, existingStackTrace.length);
        System.arraycopy(newStackTrace, 0, combinedStackTrace, existingStackTrace.length, newStackTrace.length);

        exceptionToModify.setStackTrace(combinedStackTrace);
    }

    public static void appendStackTraceBefore(Throwable exceptionToModify,
                                              StackTraceElement[] newStackTrace) {
        StackTraceElement[] existingStackTrace = exceptionToModify.getStackTrace();
        StackTraceElement[] combinedStackTrace = new StackTraceElement[existingStackTrace.length + newStackTrace.length];

        System.arraycopy(newStackTrace, 0, combinedStackTrace, 0, newStackTrace.length);
        System.arraycopy(existingStackTrace, 0, combinedStackTrace, newStackTrace.length, existingStackTrace.length);

        exceptionToModify.setStackTrace(combinedStackTrace);
    }

    public static StackTraceElement[] pushStackTrace(StackTraceElement[] stackTraceElements,
                                                     StackTraceElement newStackTraceElement,
                                                     int index) {
        StackTraceElement[] newStackTrace = new StackTraceElement[stackTraceElements.length + 1];
        System.arraycopy(stackTraceElements, 0, newStackTrace, 0, index);
        newStackTrace[index] = newStackTraceElement;
        System.arraycopy(stackTraceElements, index, newStackTrace, index + 1, stackTraceElements.length - index);
        return newStackTrace;
    }
}
