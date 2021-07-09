package io.github.wysohn.triggerreactor.core.utils;

import io.github.wysohn.triggerreactor.tools.ErrorProneRunnable;
import org.junit.Assert;

public class TestUtil {
    //assert that a runnable threw an error
    public static void assertError(ErrorProneRunnable run) {
        try {
            run.run();
        } catch (Exception e) {
            return;
        }
        Assert.fail("runnable did not throw any exception");
    }

    //assert that a runnable threw an error message with the content Error: + expectedMessage
    public static void assertJSError(ErrorProneRunnable run, String expectedMessage) {
        try {
            run.run();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("\n" + expectedMessage);

            if (e.getCause().getMessage().contains("Error: " + expectedMessage)) {
                return;
            } else {
                Assert.fail(e.getCause().getMessage() + ", expected: Error: " + expectedMessage);
            }
        }
    }

    //assert that a runnable threw an error message that matches the predicate
    public static void assertError(ErrorProneRunnable run, Class<? extends Exception> exceptionType) {
        try {
            run.run();
        } catch (Exception e) {
            if (exceptionType.isAssignableFrom(e.getClass())) return;
            Assert.fail("Wrong type of exception thrown: " + e.getClass().getSimpleName() + ", expected: " +
                    exceptionType.getClass().getSimpleName());
        }
        Assert.fail("runnable did not throw any exception");
    }
}
