package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import org.junit.Test;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class InterpreterLocalContextTest {
    InterpreterLocalContext context = new InterpreterLocalContext(Timings.LIMBO);

    @Test
    public void putAllVars() {
        // arrange
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        vars.put("key2", "value2");

        // act
        context.putAllVars(vars);

        // assert
        assertEquals("value", context.getVar("key"));
        assertEquals("value2", context.getVar("key2"));
    }

    /**
     * ConcurrentHashMap do not accept null value, so we must check if it is null
     * before putting it into the map.
     */
    @Test
    public void putAllVars_nullValue() {
        // arrange
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        vars.put("key2", null);

        // act
        context.putAllVars(vars);

        // assert
        assertEquals("value", context.getVar("key"));
        assertNull(context.getVar("key2"));
    }

    @Test(expected = EmptyStackException.class)
    public void copyState() {
        // arrange
        ProcessInterrupter interrupter = mock(ProcessInterrupter.class);
        Token mockToken = mock(Token.class);

        context.setBreakFlag(true);
        context.setContinueFlag(true);
        context.setInterrupter(interrupter);
        context.setExtra("extra", "extraValue");
        context.setVar("var", "varValue");
        context.setCallArgsSize(10);
        context.pushToken(mockToken);
        context.setImport(this.getClass().getSimpleName(), this.getClass());

        // act
        InterpreterLocalContext otherContext = context.copyState("timingsName");

        // assert
        assertEquals(interrupter, otherContext.getInterrupter());
        assertEquals("extraValue", otherContext.getExtra("extra"));
        assertEquals("varValue", otherContext.getVar("var"));
        assertTrue(otherContext.hasImport(this.getClass().getSimpleName()));
        assertEquals(this.getClass(), otherContext.getImport(this.getClass().getSimpleName()));

        assertEquals(0, otherContext.getCallArgsSize()); // part of execution stack
        assertFalse(otherContext.isBreakFlag()); // part of execution stack
        assertFalse(otherContext.isContinueFlag()); // part of execution stack
        otherContext.popToken();
    }

    @Test
    public void copyState_condition() throws InterruptedException, ExecutionException {
        // arrange
        int count = 100000;

        Runnable stateChange = () -> {
            for (int i = 0; i < count; i++) {
                context.setBreakFlag(!context.isBreakFlag());
                context.setContinueFlag(!context.isContinueFlag());
                context.setStopFlag(!context.isStopFlag());
                context.setWaitFlag(!context.isWaitFlag());
            }
        };

        Runnable stateCopy = () -> {
            for (int i = 0; i < count; i++) {
                context.copyState("timingsName");
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // act
        Future<?> thread1 = executorService.submit(stateChange);
        Future<?> thread2 = executorService.submit(stateCopy);

        executorService.shutdown();
        executorService.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);

        // assert
        thread1.get();
        thread2.get();
    }
}