package io.github.wysohn.triggerreactor.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExceptionUtilTest {

    @Test
    public void appendStackTrace() {
        // arrange
        Exception exceptionToModify = new Exception("test");
        exceptionToModify.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("org.test.Clazz", "abc", null, -1)
        });
        StackTraceElement[] newStackTrace = new StackTraceElement[]{
                new StackTraceElement("org.other.Clazz", "ooe", "file", 1)
        };

        // act
        ExceptionUtil.appendStackTrace(exceptionToModify, newStackTrace);

        try {
            throw exceptionToModify;
        }
        // assert
        catch (Exception e) {
            assertEquals("test", e.getMessage());
            assertEquals("org.test.Clazz", e.getStackTrace()[0].getClassName());
            assertEquals("abc", e.getStackTrace()[0].getMethodName());

            assertEquals("org.other.Clazz", e.getStackTrace()[1].getClassName());
            assertEquals("ooe", e.getStackTrace()[1].getMethodName());
            assertEquals("file", e.getStackTrace()[1].getFileName());
        }
    }
}