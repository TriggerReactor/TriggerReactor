package io.github.wysohn.triggerreactor.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExceptionUtilTest {

    @Test
    public void appendStackTraceAfter() {
        // arrange
        Exception exceptionToModify = new Exception("test");
        exceptionToModify.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("org.test.Clazz", "abc", null, -1)
        });
        StackTraceElement[] newStackTrace = new StackTraceElement[]{
                new StackTraceElement("org.other.Clazz", "ooe", "file", 1)
        };

        // act
        ExceptionUtil.appendStackTraceAfter(exceptionToModify, newStackTrace);

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

    @Test
    public void appendStackTraceBefore() {
        // arrange
        Exception exceptionToModify = new Exception("test");
        exceptionToModify.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("org.test.Clazz", "abc", null, -1)
        });
        StackTraceElement[] newStackTrace = new StackTraceElement[]{
                new StackTraceElement("org.other.Clazz", "ooe", "file", 1)
        };

        // act
        ExceptionUtil.appendStackTraceBefore(exceptionToModify, newStackTrace);

        try {
            throw exceptionToModify;
        }
        // assert
        catch (Exception e) {
            assertEquals("test", e.getMessage());
            assertEquals("org.other.Clazz", e.getStackTrace()[0].getClassName());
            assertEquals("ooe", e.getStackTrace()[0].getMethodName());
            assertEquals("file", e.getStackTrace()[0].getFileName());

            assertEquals("org.test.Clazz", e.getStackTrace()[1].getClassName());
            assertEquals("abc", e.getStackTrace()[1].getMethodName());
        }
    }

    @Test
    public void pushStackTrace() {
        // arrange
        StackTraceElement[] stackTraceElements = new StackTraceElement[]{
                new StackTraceElement("org.test.Clazz", "abc", null, -1),
                new StackTraceElement("org.test.Clazz", "def", null, -1),
                new StackTraceElement("org.test.Clazz", "ghi", null, -1)
        };

        StackTraceElement newStackTraceElement = new StackTraceElement("org.other.Clazz", "ooe", "file", 1);

        // act
        stackTraceElements = ExceptionUtil.pushStackTrace(stackTraceElements, newStackTraceElement, 1);

        // assert
        assertEquals("org.test.Clazz", stackTraceElements[0].getClassName());
        assertEquals("abc", stackTraceElements[0].getMethodName());

        assertEquals("org.other.Clazz", stackTraceElements[1].getClassName());
        assertEquals("ooe", stackTraceElements[1].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[2].getClassName());
        assertEquals("def", stackTraceElements[2].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[3].getClassName());
        assertEquals("ghi", stackTraceElements[3].getMethodName());
    }

    @Test
    public void pushStackTrace_first() {
        // arrange
        StackTraceElement[] stackTraceElements = new StackTraceElement[]{
                new StackTraceElement("org.test.Clazz", "abc", null, -1),
                new StackTraceElement("org.test.Clazz", "def", null, -1),
                new StackTraceElement("org.test.Clazz", "ghi", null, -1)
        };

        StackTraceElement newStackTraceElement = new StackTraceElement("org.other.Clazz", "ooe", "file", 1);

        // act
        stackTraceElements = ExceptionUtil.pushStackTrace(stackTraceElements, newStackTraceElement, 0);

        // assert
        assertEquals("org.other.Clazz", stackTraceElements[0].getClassName());
        assertEquals("ooe", stackTraceElements[0].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[1].getClassName());
        assertEquals("abc", stackTraceElements[1].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[2].getClassName());
        assertEquals("def", stackTraceElements[2].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[3].getClassName());
        assertEquals("ghi", stackTraceElements[3].getMethodName());
    }

    @Test
    public void pushStackTrace_last() {
        // arrange
        StackTraceElement[] stackTraceElements = new StackTraceElement[]{
                new StackTraceElement("org.test.Clazz", "abc", null, -1),
                new StackTraceElement("org.test.Clazz", "def", null, -1),
                new StackTraceElement("org.test.Clazz", "ghi", null, -1)
        };

        StackTraceElement newStackTraceElement = new StackTraceElement("org.other.Clazz", "ooe", "file", 1);

        // act
        stackTraceElements = ExceptionUtil.pushStackTrace(stackTraceElements, newStackTraceElement, 3);

        // assert
        assertEquals("org.test.Clazz", stackTraceElements[0].getClassName());
        assertEquals("abc", stackTraceElements[0].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[1].getClassName());
        assertEquals("def", stackTraceElements[1].getMethodName());

        assertEquals("org.test.Clazz", stackTraceElements[2].getClassName());
        assertEquals("ghi", stackTraceElements[2].getMethodName());

        assertEquals("org.other.Clazz", stackTraceElements[3].getClassName());
        assertEquals("ooe", stackTraceElements[3].getMethodName());
    }
}