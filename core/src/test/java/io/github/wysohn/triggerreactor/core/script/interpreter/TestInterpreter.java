/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.junit.Before;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TestInterpreter {

    private TaskSupervisor mockTask;

    @Before
    public void init() {
        mockTask = mock(TaskSupervisor.class);
    }

    @org.junit.Test
    public void testMethod() throws Exception {
        // Arrange
        String text = ""
                + "rand = common.getHealth();"
                + "#MESSAGE rand\n"
                + "ENDIF\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .addScriptVariable("common", new InTest2())
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(5.23));
    }

    @org.junit.Test
    public void testMethodWithEnumParameter() throws Exception {
        // Arrange
        String text = "{\"temp1\"} = temp.testEnumMethod(\"IMTEST\");"
                + "{\"temp2\"} = temp2.testEnumMethod(\"Something\");";


        Test test = Test.Builder.of(text)
                .addScriptVariable("temp", new TheTest())
                .addScriptVariable("temp2", new TheTest2())
                .build();

        // Act
        test.test();

        // Assert
        // the only method matching is the one with enum parameter. Expect it to be converted
        assertEquals(TestEnum.IMTEST, test.getGlobalVar("temp1"));
        // there is an overloaded method which takes in String in the place of enum. Overloaded method has higher
        // priority.
        assertEquals("Something", test.getGlobalVar("temp2"));
    }

    @org.junit.Test
    public void testReference() throws Exception {
        // Arrange
        String text = ""
                + "X = 5\n"
                + "str = \"abc\"\n"
                + "WHILE 1 > 0\n"
                + "    str = str + X\n"
                + "    IF player.in.health > 2 && player.in.health > 0\n"
                + "        #MESSAGE 3*4\n"
                + "    ELSE\n"
                + "        #MESSAGE str\n"
                + "    ENDIF\n"
                + "    #MESSAGE text\n"
                + "    player.getTest().in.health = player.getTest().in.getHealth() + 1.2\n"
                + "    #MESSAGE player.in.hasPermission(\"t\")\n"
                + "    X = X - 1\n"
                + "    IF X < 0\n"
                + "        #STOP\n"
                + "    ENDIF\n"
                + "ENDWHILE";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);
        TheTest reference = new TheTest();

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .addScriptVariable("player", reference)
                .addScriptVariable("text", "hello")
                .overrideTaskSupervisor(mockTask)
                .build();

        // Act
        test.test();

        // Assert
        assertEquals(12.43, reference.getTest().in.getHealth(), 0.001);
    }

    @org.junit.Test
    public void testStringAppend() throws Exception {
        // Arrange
        String text = ""
                + "arr = array(4)\n"
                + "arr[0] = \"beh\"+player.in.health\n"
                + "arr[1] = player.in.health+\"beh\"\n"
                + "arr[2] = \"beh\"+1+1\n"
                + "arr[3] = \"beh\"+(1+1)\n"
                + "#MESSAGE arr\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .addScriptVariable("player", new TheTest())
                .overrideSelfReference(new SelfReference() {
                    public Object[] array(int size) {
                        return new Object[size];
                    }
                })
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(new Object[]{"beh0.82", "0.82beh", "beh11", "beh2"}));
    }

    @org.junit.Test(expected = InterpreterException.class)
    public void testLiteralStringTrueOrFalseParse() throws Exception {
        // Arrange
        String text = ""
                + "temp = \"true\"\n"
                + "IF temp == true\n"
                + "  #TEST\n"
                + "ELSE\n"
                + "  error.cause()\n"
                + "ENDIF;";


        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor, times(0)).evaluate(any(), anyMap(), any(), any());
    }

    @org.junit.Test
    public void testGlobalVariable() throws Exception {
        // Arrange
        String text = ""
                + "{text+\".something\"} = 12.54\n"
                + "#MESSAGE {text+\".something\"}\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(12.54));
    }

    @org.junit.Test
    public void testTempGlobalVariable() throws Exception {
        // Arrange
        String text = ""
                + "{?text+\".something\"} = 12.54;"
                + "#MESSAGE {?text+\".something\"};" +
                "{?text+\".something\"} = null;" +
                "#MESSAGE2 {?text+\".something\"};";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .putExecutor("MESSAGE2", mockExecutor2)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(12.54));
        verify(mockExecutor2).evaluate(any(), anyMap(), any(), eq(null));
    }

    @org.junit.Test
    public void testGlobalVariableDeletion() throws Exception {
        // Arrange
        String text = "key = \"temp\";" +
                "{key} = 1;" +
                "#TEST1 {key};" +
                "{key} = null;" +
                "#TEST2 {key};";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor)
                .putExecutor("TEST2", mockExecutor2)
                .build();

        // Act

        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(1));
        verify(mockExecutor2).evaluate(any(), anyMap(), any(), eq(null));
        assertNull(test.getGlobalVar("temp"));
    }

    @org.junit.Test
    public void testArray() throws Exception {
        // Arrange
        String text = ""
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1- -1-1]\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .addScriptVariable("args", new Object[2])
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq("arg1, arg2"));
    }

    @org.junit.Test
    public void testCustomArray() throws Exception {
        // Arrange
        String text = ""
                + "args = array(2)\n"
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1- -1-1]\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .overrideSelfReference(new SelfReference() {
                    public Object[] array(int size) {
                        return new Object[size];
                    }
                })
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq("arg1, arg2"));
    }

    @org.junit.Test
    public void testIteration2() throws Exception {
        // Arrange
        String text = ""
                + "FOR i = 0:10\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        for (int i = 0; i < 10; i++) {
            verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(i));
        }
    }

    @org.junit.Test
    public void testIteration3() throws Exception {
        // Arrange
        String text = ""
                + "start=0;"
                + "stop=10;"
                + "FOR i = start:stop\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        for (int i = 0; i < 10; i++) {
            verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(i));
        }
    }

    @org.junit.Test
    public void testIteration4() throws Exception {
        // Arrange
        String text = ""
                + "start=0;"
                + "stop=10;"
                + "FOR i = start*10-0:stop-10*0\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        for (int i = 0; i < 10; i++) {
            verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(i));
        }
    }

    @org.junit.Test
    public void testIteration5() throws Exception {
        // Arrange
        String text = ""
                + "FOR i = 0:getPlayers().size()\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockExecutor)
                .overrideSelfReference(new SelfReference() {
                    public Collection<String> getPlayers() {
                        List<String> names = new ArrayList<>();
                        for (int i = 0; i < 10; i++)
                            names.add(String.valueOf(i));
                        return names;
                    }
                })
                .build();

        // Act
        test.test();

        // Assert
        for (int i = 0; i < 10; i++) {
            verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(i));
        }
    }

    @org.junit.Test(expected = InterpreterException.class)
    public void testOnlyTry() throws Exception {
        // Arrange

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "ENDTRY;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any());


//        Lexer lexer = new Lexer(text, charset);
//        Parser parser = new Parser(lexer);
//
//        Node root = parser.parse();
//
//        Map<String, Executor> executorMap = new HashMap<>();
//        Executor executor = mock(Executor.class);
//        when(executor.evaluate(any(), anyMap(), any(), any())).thenReturn(null);
//        executorMap.put("TEST", executor);
//
//        Interpreter interpreter = new Interpreter(root);
//        interpreter.setExecutorMap(executorMap);
//        interpreter.setTaskSupervisor(mockTask);
//        interpreter.setSelfReference(new CommonFunctions());
//
//        interpreter.startWithContext(null);
//
//        verify(executor, times(0)).evaluate(any(), anyMap(), any(), any());
        // invoked 0 times? why?
    }

    @org.junit.Test
    public void testTryCatch1() throws Exception {
        // Arrange

        String text = "" +
                "TRY;" +
                "   #CASE 1;" +
                "CATCH e;" +
                "   #CASE 2;" +
                "ENDTRY;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("CASE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(1));
        verify(mockExecutor, never()).evaluate(any(), anyMap(), any(), eq(2));
    }

    @org.junit.Test
    public void testTryCatch2() throws Exception {
        // Arrange

        String text = "" +
                "TRY;" +
                "   #PRE;" +
                "   #ERROR;" +
                "   #POST;" +
                "CATCH e;" +
                "   #CATCH;" +
                "ENDTRY;";

        Executor mockExecutorPreError = mock(Executor.class);
        when(mockExecutorPreError.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutorPostError = mock(Executor.class);
        when(mockExecutorPostError.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutorError = mock(Executor.class);
        when(mockExecutorError.evaluate(any(), any(), any(), any())).thenThrow(new InterpreterException("error"));
        Executor mockExecutorCatch = mock(Executor.class);
        when(mockExecutorCatch.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("PRE", mockExecutorPreError)
                .putExecutor("POST", mockExecutorPostError)
                .putExecutor("ERROR", mockExecutorError)
                .putExecutor("CATCH", mockExecutorCatch)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutorPreError).evaluate(any(), anyMap(), any());
        verify(mockExecutorPostError, never()).evaluate(any(), anyMap(), any());
        verify(mockExecutorError).evaluate(any(), anyMap(), any());
        verify(mockExecutorCatch).evaluate(any(), anyMap(), any());
    }

    @org.junit.Test
    public void testTryFinally1() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #CASE 1;" +
                "FINALLY;" +
                "   #CASE 2;" +
                "ENDTRY;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("CASE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(1));
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(2));
    }

    @org.junit.Test
    public void testTryCatchFinally1() throws Exception {
        // Arrange

        String text = "" +
                "TRY;" +
                "   #CASE 1;" +
                "CATCH e;" +
                "   #CASE 2;" +
                "FINALLY;" +
                "   #CASE 3;" +
                "ENDTRY;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("CASE", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(1));
        verify(mockExecutor, never()).evaluate(any(), anyMap(), any(), eq(2));
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(3));
    }

    @org.junit.Test
    public void testTryCatchFinally2() throws Exception {
        // Arrange

        String text = "" +
                "TRY;" +
                "   #PRE;" +
                "   #ERROR" +
                "   #POST;" +
                "CATCH e;" +
                "   #CATCH;" +
                "FINALLY;" +
                "   #FINALLY;" +
                "ENDTRY;";

        Executor mockExecutorPreError = mock(Executor.class);
        when(mockExecutorPreError.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutorPostError = mock(Executor.class);
        when(mockExecutorPostError.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutorError = mock(Executor.class);
        when(mockExecutorError.evaluate(any(), any(), any(), any())).thenThrow(new InterpreterException("error"));
        Executor mockExecutorCatch = mock(Executor.class);
        when(mockExecutorCatch.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutorFinally = mock(Executor.class);
        when(mockExecutorFinally.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("PRE", mockExecutorPreError)
                .putExecutor("POST", mockExecutorPostError)
                .putExecutor("ERROR", mockExecutorError)
                .putExecutor("CATCH", mockExecutorCatch)
                .putExecutor("FINALLY", mockExecutorFinally)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutorPreError).evaluate(any(), anyMap(), any(), any());
        verify(mockExecutorPostError, never()).evaluate(any(), anyMap(), any(), any());
        verify(mockExecutorError).evaluate(any(), anyMap(), any(), any());
        verify(mockExecutorCatch).evaluate(any(), anyMap(), any(), any());
        verify(mockExecutorFinally).evaluate(any(), anyMap(), any(), any());
    }

    @org.junit.Test
    public void testTryCatchInvokedMethod() throws Exception {
        // Arrange

        String text = "" +
                "import java.io.FileReader;" +
                "" +
                "TRY;" +
                "    FileReader(\"./no-exist-file.data\");" +
                "    #VERIFY false;" +
                "CATCH e;" +
                "    #VERIFY true;" +
                "ENDTRY";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("VERIFY", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any(), eq(true));
        verify(mockExecutor, never()).evaluate(any(), anyMap(), any(), eq(false));
    }

    @org.junit.Test
    public void testNegation() throws Exception {
        // Arrange
        String text = ""
                + "arr = array(6)\n"
                + "arr[0] = true\n"
                + "arr[1] = !true\n"
                + "arr[2] = !true || false\n"
                + "arr[3] = true && !false\n"
                + "arr[4] = true && 1 < 2 && 5 > 4 && 1 != 2 && 2 == 2 && (false || 2*2 > 3)\n"
                + "arr[5] = false || false || (2 < 3 && 6+5*3 > 1*2+3)\n" +
                "#TEST arr";


        Executor mockTask = mock(Executor.class);
        when(mockTask.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockTask)
                .overrideSelfReference(new SelfReference() {
                    public Object[] array(int size) {
                        return new Object[size];
                    }
                })
                .build();

        // Act
        test.test();

        // Assert
        verify(mockTask).evaluate(any(), anyMap(), any(),
                eq(new Object[]{true, false, false, true, true, true}));
    }

    @org.junit.Test
    public void testShortCircuitAnd() throws Exception {
        // Arrange
        String text = ""
                + "IF player != null && player.health == 0.82;"
                + "    #TEST;"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .addScriptVariable("player", new InTest())
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any());
    }

    @org.junit.Test
    public void testShortCircuitAndNull() throws Exception {
        // Arrange
        String text = ""
                + "IF player != null && player.health == 0.82;"
                + "    #TEST;"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor, never()).evaluate(any(), anyMap(), any());
        // and no exception
    }

    @org.junit.Test
    public void testShortCircuitOr() throws Exception {
        // Arrange
        String text = ""
                + "IF player2 == null || player2.health == 0.82;"
                + "    #TEST;"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .addScriptVariable("player2", new InTest())
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any());
    }

    @org.junit.Test
    public void testShortCircuitOrNull() throws Exception {
        // Arrange
        String text = ""
                + "IF player2 == null || player2.health == 0.82;"
                + "    #TEST;"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), anyMap(), any());
        // player2 is null but still executes without NPE
    }

    @org.junit.Test
    public void testWhile() throws Exception {
        // Arrange
        String text = ""
                + "number = 1;"
                + "WHILE number < 3;"
                + "number = number + 1;"
                + "ENDWHILE;";

        Test test = Test.Builder.of(text)
                .overrideTaskSupervisor(mockTask)
                .build();

        // Act
        test.test();

        // Assert
        assertEquals(3, test.getScriptVar("number"));
    }

//    @org.junit.Test
//    public void testEnumParse() throws Exception {
//        // Arrange
//        String text = ""
//                + "result = parseEnum(\"io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter\\$TestEnum\","
//                + " \"IMTEST\");";
//
//        Test test = Test.Builder.of(text)
//                .build();
//
//        // Act
//        test.test();
//
//        // Assert
//        assertEquals(TestEnum.IMTEST, test.getScriptVar("result"));
//    }

    @org.junit.Test
    public void testPlaceholder() throws Exception {
        // Arrange
        String text = "x = 100.0;"
                + "returnvalue = $test:0:x:true:\"hoho\";"
                + "#MESSAGE $playername returnvalue;"
                + "#TESTSTRING $string;"
                + "#TESTINTEGER $integer;"
                + "#TESTDOUBLE $double;"
                + "#TESTBOOLEAN $boolean;";

        Executor mockMessage = mock(Executor.class);
        when(mockMessage.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockTestString = mock(Executor.class);
        when(mockTestString.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockTestInteger = mock(Executor.class);
        when(mockTestInteger.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockTestDouble = mock(Executor.class);
        when(mockTestDouble.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockTestBoolean = mock(Executor.class);
        when(mockTestBoolean.evaluate(any(), any(), any(), any())).thenReturn(null);

        Placeholder mockPlaceholderTest = mock(Placeholder.class);
        when(mockPlaceholderTest.evaluate(any(), any(), any(), any())).thenReturn(100.0);
        Placeholder mockPlaceholderPlayerName = mock(Placeholder.class);
        when(mockPlaceholderPlayerName.evaluate(any(), any(), any(), any())).thenReturn("testplayer");
        Placeholder mockPlaceholderString = mock(Placeholder.class);
        when(mockPlaceholderString.evaluate(any(), any(), any(), any())).thenReturn("hoho");
        Placeholder mockPlaceholderInteger = mock(Placeholder.class);
        when(mockPlaceholderInteger.evaluate(any(), any(), any(), any())).thenReturn(1);
        Placeholder mockPlaceholderDouble = mock(Placeholder.class);
        when(mockPlaceholderDouble.evaluate(any(), any(), any(), any())).thenReturn(1.0);
        Placeholder mockPlaceholderBoolean = mock(Placeholder.class);
        when(mockPlaceholderBoolean.evaluate(any(), any(), any(), any())).thenReturn(true);

        Test test = Test.Builder.of(text)
                .putExecutor("MESSAGE", mockMessage)
                .putExecutor("TESTSTRING", mockTestString)
                .putExecutor("TESTINTEGER", mockTestInteger)
                .putExecutor("TESTDOUBLE", mockTestDouble)
                .putExecutor("TESTBOOLEAN", mockTestBoolean)
                .putPlaceholder("test", mockPlaceholderTest)
                .putPlaceholder("playername", mockPlaceholderPlayerName)
                .putPlaceholder("string", mockPlaceholderString)
                .putPlaceholder("integer", mockPlaceholderInteger)
                .putPlaceholder("double", mockPlaceholderDouble)
                .putPlaceholder("boolean", mockPlaceholderBoolean)
                .build();

        // Act
        test.test();

        // Assert
        //TODO: This seems to be a bug, but we keep it in refactoring stage
        //verify(mockMessage).evaluate(any(), any(), any(), eq("testplayer"), eq(100.0));
        verify(mockTestString).evaluate(any(), any(), any(), eq("hoho"));
        verify(mockTestInteger).evaluate(any(), any(), any(), eq(1));
        //TODO: same as above
        //verify(mockTestDouble).evaluate(any(), any(), any(), eq(1.0));
        verify(mockTestBoolean).evaluate(any(), any(), any(), eq(true));

        verify(mockPlaceholderTest).evaluate(any(), any(), any(),
                eq(0), eq(100.0), eq(true), eq("hoho"));
        verify(mockPlaceholderPlayerName).evaluate(any(), any(), any());
        verify(mockPlaceholderString).evaluate(any(), any(), any());
        verify(mockPlaceholderInteger).evaluate(any(), any(), any());
        verify(mockPlaceholderDouble).evaluate(any(), any(), any());
        verify(mockPlaceholderBoolean).evaluate(any(), any(), any());
    }

    @org.junit.Test
    public void testPlaceholderNull() throws Exception {
        // Arrange
        String text = "a = $merp";

        Placeholder mockPlaceholder = mock(Placeholder.class);
        when(mockPlaceholder.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putPlaceholder("merp", mockPlaceholder)
                .build();

        // Act
        test.test();

        // Assert
        assertNull(test.getScriptVar("a"));
    }

    @org.junit.Test
    public void testUnaryMinus() throws Exception {
        // Arrange
        String text = "x = 4.0;"
                + "#TEST -1+-5;"
                + "#TEST -2.0- -5;"
                + "#TEST -$test3-5;"
                + "#TEST -x-5;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Placeholder mockPlaceholder = mock(Placeholder.class);
        when(mockPlaceholder.evaluate(any(), any(), any(), any())).thenReturn(3.0);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .putPlaceholder("test3", mockPlaceholder)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(-6));
        verify(mockExecutor).evaluate(any(), any(), any(), eq(3.0));
        verify(mockExecutor).evaluate(any(), any(), any(), eq(-8));
        verify(mockExecutor).evaluate(any(), any(), any(), eq(-9.0));
    }

    @org.junit.Test
    public void testIncrementAndDecrement1() throws Exception {
        // Arrange
        String text = "a = 2;"
                + "#TEST1 -a;"
                + "#TEST2 a++;"
                + "#TEST3 a--;"
                + "#TEST4 ++a;"
                + "#TEST5 --a;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor3 = mock(Executor.class);
        when(mockExecutor3.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor4 = mock(Executor.class);
        when(mockExecutor4.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor5 = mock(Executor.class);
        when(mockExecutor5.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .putExecutor("TEST3", mockExecutor3)
                .putExecutor("TEST4", mockExecutor4)
                .putExecutor("TEST5", mockExecutor5)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(-2));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(2));
        verify(mockExecutor3).evaluate(any(), any(), any(), eq(3));
        verify(mockExecutor4).evaluate(any(), any(), any(), eq(3));
        verify(mockExecutor5).evaluate(any(), any(), any(), eq(2));
    }

    @org.junit.Test
    public void testIncrementAndDecrement2() throws Exception {
        // Arrange
        String text = "a = 2.1;"
                + "#TEST1 -a;"
                + "#TEST2 a++;"
                + "#TEST3 a--;"
                + "#TEST4 ++a;"
                + "#TEST5 --a;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor3 = mock(Executor.class);
        when(mockExecutor3.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor4 = mock(Executor.class);
        when(mockExecutor4.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor5 = mock(Executor.class);
        when(mockExecutor5.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .putExecutor("TEST3", mockExecutor3)
                .putExecutor("TEST4", mockExecutor4)
                .putExecutor("TEST5", mockExecutor5)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(-2.1));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(2.1));
        verify(mockExecutor3).evaluate(any(), any(), any(), eq(3.1));
        verify(mockExecutor4).evaluate(any(), any(), any(), eq(3.1));
        verify(mockExecutor5).evaluate(any(), any(), any(), eq(2.1));
    }

    @org.junit.Test
    public void testBitwiseAndBitshift() throws Exception {
        // Arrange
        String text = "x = -129;"
                + "y = true;"
                + "#TEST1 x << 1;"
                + "#TEST2 x >> 1;"
                + "#TEST3 x >>> 1;"
                + "#TEST4 x | 67;"
                + "#TEST5 x & 67;"
                + "#TEST6 x ^ 67;"
                + "#TEST7 ~x;"
                + "#TEST8 y | true;"
                + "#TEST9 y & true;"
                + "#TEST10 y ^ true;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor3 = mock(Executor.class);
        when(mockExecutor3.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor4 = mock(Executor.class);
        when(mockExecutor4.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor5 = mock(Executor.class);
        when(mockExecutor5.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor6 = mock(Executor.class);
        when(mockExecutor6.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor7 = mock(Executor.class);
        when(mockExecutor7.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor8 = mock(Executor.class);
        when(mockExecutor8.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor9 = mock(Executor.class);
        when(mockExecutor9.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor10 = mock(Executor.class);
        when(mockExecutor10.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .putExecutor("TEST3", mockExecutor3)
                .putExecutor("TEST4", mockExecutor4)
                .putExecutor("TEST5", mockExecutor5)
                .putExecutor("TEST6", mockExecutor6)
                .putExecutor("TEST7", mockExecutor7)
                .putExecutor("TEST8", mockExecutor8)
                .putExecutor("TEST9", mockExecutor9)
                .putExecutor("TEST10", mockExecutor10)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(-258));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(-65));
        verify(mockExecutor3).evaluate(any(), any(), any(), eq(2147483583));
        verify(mockExecutor4).evaluate(any(), any(), any(), eq(-129));
        verify(mockExecutor5).evaluate(any(), any(), any(), eq(67));
        verify(mockExecutor6).evaluate(any(), any(), any(), eq(-196));
        verify(mockExecutor7).evaluate(any(), any(), any(), eq(128));
        verify(mockExecutor8).evaluate(any(), any(), any(), eq(true));
        verify(mockExecutor9).evaluate(any(), any(), any(), eq(true));
        verify(mockExecutor10).evaluate(any(), any(), any(), eq(false));
    }

    @org.junit.Test(expected = InterpreterException.class)
    public void testBitwiseException1() throws Exception {
        // Arrange
        String text = "x = 1&true;";

        Test test = Test.Builder.of(text)
                .build();

        // Act
        test.test();

        // Assert
        // Exception
    }

    @org.junit.Test(expected = InterpreterException.class)
    public void testBitwiseException2() throws Exception {
        // Arrange
        String text = "x = false^2.0;";

        Test test = Test.Builder.of(text)
                .build();

        // Act
        test.test();

        // Assert
        // Exception
    }

    @org.junit.Test(expected = InterpreterException.class)
    public void testBitwiseException3() throws Exception {
        // Arrange
        String text = "x = 1|2.1;";

        Test test = Test.Builder.of(text)
                .build();

        // Act
        test.test();

        // Assert
        // Exception
    }

    @org.junit.Test
    public void testSimpleIf() throws Exception {
        // Arrange

        String text = "x = 4.0;"
                + "IF x > 0.0;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq("pass"));
        verify(mockExecutor, never()).evaluate(any(), any(), any(), eq("failed"));
    }

    @org.junit.Test
    public void testSimpleIf2() throws Exception {
        // Arrange
        String text = ""
                + "IF someunknown != 0.0;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .addScriptVariable("someunknown", 0.0)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq("failed"));
        verify(mockExecutor, never()).evaluate(any(), any(), any(), eq("pass"));
    }

    @org.junit.Test
    public void testSimpleIf3() throws Exception {
        // Arrange
        String text = ""
                + "IF 0.0 != someunknown;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .addScriptVariable("someunknown", 0.0)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq("failed"));
        verify(mockExecutor, never()).evaluate(any(), any(), any(), eq("pass"));
    }

    @org.junit.Test
    public void testSimpleIf4() throws Exception {
        // Arrange
        String text = ""
                + "IF someunknown == someunknown;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq("pass"));
        verify(mockExecutor, never()).evaluate(any(), any(), any(), eq("failed"));
    }

    @org.junit.Test
    public void testNestedIf() throws Exception {
        // Arrange
        String text = "x = 4.0;"
                + "IF x < 0.0;"
                + "    #TEST \"case1\";"
                + "ELSEIF x > 0.0;"
                + "    #TEST \"case2\";"
                + "ELSE;"
                + "    #TEST \"case3\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("case2"));
        verify(executor, never()).evaluate(any(), any(), any(), eq("case1"));
        verify(executor, never()).evaluate(any(), any(), any(), eq("case3"));
    }

    @org.junit.Test
    public void testNestedIf2() throws Exception {
        // Arrange
        String text = "x = 4.0;"
                + "IF x < 0.0;"
                + "    #TEST \"case1\";"
                + "ELSEIF x < -5.0;"
                + "    #TEST \"case2\";"
                + "ELSE;"
                + "    #TEST \"case3\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("case3"));
        verify(executor, never()).evaluate(any(), any(), any(), eq("case1"));
        verify(executor, never()).evaluate(any(), any(), any(), eq("case2"));
    }

    @org.junit.Test
    public void testNestedIf3_1() throws Exception {
        // Arrange
        String text = ""
                + "IF x > 999;"
                + "    #TEST \"test1\";"
                + "ELSEIF x > 99;"
                + "    #TEST \"test2\";"
                + "ELSEIF x > 9;"
                + "    IF x < 11;"
                + "        #TEST \"test5\";"
                + "    ENDIF;"
                + "ELSEIF x > 4;"
                + "    #TEST \"test3\";"
                + "ELSE;"
                + "    #TEST \"test4\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .addScriptVariable("x", 1000)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("test1"));
    }

    @org.junit.Test
    public void testNestedIf3_2() throws Exception {
        // Arrange
        String text = ""
                + "IF x > 999;"
                + "    #TEST \"test1\";"
                + "ELSEIF x > 99;"
                + "    #TEST \"test2\";"
                + "ELSEIF x > 9;"
                + "    IF x < 11;"
                + "        #TEST \"test5\";"
                + "    ENDIF;"
                + "ELSEIF x > 4;"
                + "    #TEST \"test3\";"
                + "ELSE;"
                + "    #TEST \"test4\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .addScriptVariable("x", 100)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("test2"));
    }

    @org.junit.Test
    public void testNestedIf3_3() throws Exception {
        // Arrange
        String text = ""
                + "IF x > 999;"
                + "    #TEST \"test1\";"
                + "ELSEIF x > 99;"
                + "    #TEST \"test2\";"
                + "ELSEIF x > 9;"
                + "    IF x < 11;"
                + "        #TEST \"test5\";"
                + "    ENDIF;"
                + "ELSEIF x > 4;"
                + "    #TEST \"test3\";"
                + "ELSE;"
                + "    #TEST \"test4\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .addScriptVariable("x", 10)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("test5"));
    }

    @org.junit.Test
    public void testNestedIf3_4() throws Exception {
        // Arrange
        String text = ""
                + "IF x > 999;"
                + "    #TEST \"test1\";"
                + "ELSEIF x > 99;"
                + "    #TEST \"test2\";"
                + "ELSEIF x > 9;"
                + "    IF x < 11;"
                + "        #TEST \"test5\";"
                + "    ENDIF;"
                + "ELSEIF x > 4;"
                + "    #TEST \"test3\";"
                + "ELSE;"
                + "    #TEST \"test4\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .addScriptVariable("x", 5)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("test3"));
    }

    @org.junit.Test
    public void testNestedIf3_5() throws Exception {
        // Arrange
        String text = ""
                + "IF x > 999;"
                + "    #TEST \"test1\";"
                + "ELSEIF x > 99;"
                + "    #TEST \"test2\";"
                + "ELSEIF x > 9;"
                + "    IF x < 11;"
                + "        #TEST \"test5\";"
                + "    ENDIF;"
                + "ELSEIF x > 4;"
                + "    #TEST \"test3\";"
                + "ELSE;"
                + "    #TEST \"test4\";"
                + "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .addScriptVariable("x", 0)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq("test4"));
    }

    @org.junit.Test
    public void testNestedIf4() throws Exception {
        // Arrange
        String text = "x = 4.0;"
                + "" +
                "IF x > 0.0;" +
                "    IF x == 4.0;" +
                "        #TEST 1;" +
                "    ELSE;" +
                "        #TEST 2;" +
                "    ENDIF;" +
                "ELSE;" +
                "    #TEST 3;" +
                "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(1));
        verify(executor, never()).evaluate(any(), any(), any(), eq(2));
        verify(executor, never()).evaluate(any(), any(), any(), eq(3));
    }

    @org.junit.Test
    public void testNestedIf4_2() throws Exception {
        // Arrange
        String text = "x = 5.0;"
                + "" +
                "IF x > 0.0;" +
                "    IF x == 4.0;" +
                "        #TEST 1;" +
                "    ELSE;" +
                "        #TEST 2;" +
                "    ENDIF;" +
                "ELSE;" +
                "    #TEST 3;" +
                "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(2));
        verify(executor, never()).evaluate(any(), any(), any(), eq(1));
        verify(executor, never()).evaluate(any(), any(), any(), eq(3));
    }

    @org.junit.Test
    public void testNestedIf4_3() throws Exception {
        // Arrange
        String text = "x = -99;"
                + "" +
                "IF x > 0.0;" +
                "    IF x == 4.0;" +
                "        #TEST 1;" +
                "    ELSE;" +
                "        #TEST 2;" +
                "    ENDIF;" +
                "ELSE;" +
                "    #TEST 3;" +
                "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(3));
        verify(executor, never()).evaluate(any(), any(), any(), eq(1));
        verify(executor, never()).evaluate(any(), any(), any(), eq(2));
    }

    @org.junit.Test
    public void testNestedIf5() throws Exception {
        // Arrange
        String text = "x = 4.0;"
                + "" +
                "IF x > 0.0;" +
                "    IF x == 4.0;" +
                "        IF x == 3.0;" +
                "        ELSEIF x == 4.0;" +
                "            #TEST 1;" +
                "        ELSE;" +
                "        ENDIF;" +
                "    ELSE;" +
                "        #TEST 2;" +
                "    ENDIF;" +
                "ELSE;" +
                "    #TEST 3;" +
                "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(1));
        verify(executor, never()).evaluate(any(), any(), any(), eq(2));
        verify(executor, never()).evaluate(any(), any(), any(), eq(3));
    }

    @org.junit.Test
    public void testNestedIfNoElse() throws Exception {
        // Arrange
        String text = "x = 4.0;"
                + "" +
                "IF x > 0.0;" +
                "    IF x == 4.0;" +
                "        IF x == 3.0;" +
                "        ELSEIF x == 2.0;" +
                "            #TEST 1;" +
                "        ELSEIF x == 4.0;" +
                "            #TEST 2;" +
                "        ENDIF;" +
                "    ENDIF;" +
                "ELSE;" +
                "    #TEST 3;" +
                "ENDIF;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(2));
        verify(executor, never()).evaluate(any(), any(), any(), eq(1));
        verify(executor, never()).evaluate(any(), any(), any(), eq(3));
    }

    @org.junit.Test
    public void testImport() throws Exception {
        // Arrange
        String text = "IMPORT io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter$TheTest;"
                + "IMPORT io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter$TestEnum;"
                + "#TEST TheTest;"
                + "#TEST2 TheTest.staticTest();"
                + "#TEST3 TheTest().localTest();"
                + "#TEST4 TheTest.staticField;"
                + "#TEST5 TestEnum.IMTEST;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor3 = mock(Executor.class);
        when(mockExecutor3.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor4 = mock(Executor.class);
        when(mockExecutor4.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor5 = mock(Executor.class);
        when(mockExecutor5.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .putExecutor("TEST3", mockExecutor3)
                .putExecutor("TEST4", mockExecutor4)
                .putExecutor("TEST5", mockExecutor5)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(TheTest.class));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(TheTest.staticTest()));
        verify(mockExecutor3).evaluate(any(), any(), any(), eq(new TheTest().localTest()));
        verify(mockExecutor4).evaluate(any(), any(), any(), eq(TheTest.staticField));
        verify(mockExecutor5).evaluate(any(), any(), any(), eq(TestEnum.IMTEST));
    }

    @org.junit.Test
    public void testImportAs() throws Exception {
        // Arrange
        String text = "IMPORT " + TheTest.class.getName() + " as SomeClass;"
                + "IMPORT " + TestEnum.class.getName() + " as SomeEnum;"
                + "#TEST SomeClass;"
                + "#TEST SomeClass.staticTest();"
                + "#TEST SomeClass().localTest();"
                + "#TEST SomeClass.staticField;"
                + "#TEST SomeEnum.IMTEST;";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(TheTest.class));
        verify(executor).evaluate(any(), any(), any(), eq("static"));
        verify(executor).evaluate(any(), any(), any(), eq("local"));
        verify(executor).evaluate(any(), any(), any(), eq("staticField"));
        verify(executor).evaluate(any(), any(), any(), eq(TestEnum.IMTEST));
    }

    @org.junit.Test
    public void testComparison() throws Exception {
        // Arrange
        String text = "#TEST 1 < 2, 2 < 1;"
                + "#TEST2 5 > 4, 4 > 5;"
                + "#TEST3 1 <= 1, 3 <= 2;"
                + "#TEST4 1 >= 1, 2 >= 3;"
                + "#TEST5 \"tt\" == \"tt\", \"bb\" == \"bt\";"
                + "#TEST6 \"tt\" != \"bb\", \"bb\" != \"bb\";";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor3 = mock(Executor.class);
        when(mockExecutor3.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor4 = mock(Executor.class);
        when(mockExecutor4.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor5 = mock(Executor.class);
        when(mockExecutor5.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor6 = mock(Executor.class);
        when(mockExecutor6.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .putExecutor("TEST3", mockExecutor3)
                .putExecutor("TEST4", mockExecutor4)
                .putExecutor("TEST5", mockExecutor5)
                .putExecutor("TEST6", mockExecutor6)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(true), eq(false));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(true), eq(false));
        verify(mockExecutor3).evaluate(any(), any(), any(), eq(true), eq(false));
        verify(mockExecutor4).evaluate(any(), any(), any(), eq(true), eq(false));
        verify(mockExecutor5).evaluate(any(), any(), any(), eq(true), eq(false));
    }

    @org.junit.Test
    public void testNullComparison() throws Exception {
        // Arrange
        String text = "IF {\"temp\"} == null;"
                + "#TEST;"
                + "ENDIF;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any());
    }

    @org.junit.Test
    public void testLineBreak() throws Exception {
        // Arrange
        String text = "#TEST \"abcd\\nABCD\"";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq("abcd\nABCD"));
    }

    @org.junit.Test
    public void testCarriageReturn() throws Exception {
        // Arrange
        String text = "#TEST \"abcd\\rABCD\"";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq("abcd\rABCD"));
    }

    @org.junit.Test
    public void testISStatement() throws Exception {
        // Arrange
        String text = "IMPORT " + TheTest.class.getName() + ";" +
                "IMPORT " + InTest.class.getName() + ";" +
                "" +
                "#TEST test IS TheTest, test IS InTest;" +
                "#TEST2 test2 IS InTest, test2 IS TheTest;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .addScriptVariable("test", new TheTest())
                .addScriptVariable("test2", new InTest())
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(true), eq(false));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(true), eq(false));
    }

    @org.junit.Test
    public void testBreak() throws Exception {
        // Arrange
        String text = "" +
                "x = 0;" +
                "WHILE x < 5;" +
                "    x = x + 1;" +
                "    IF x > 1;" +
                "        #BREAK;" +
                "    ENDIF;" +
                "ENDWHILE;" +
                "#TEST x;" +
                "" +
                "FOR x = 0:10;" +
                "    IF x == 5;" +
                "        #BREAK;" +
                "    ENDIF;" +
                "ENDFOR;" +
                "#TEST2 x";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .overrideTaskSupervisor(mockTask)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(2));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(5));
    }


    @org.junit.Test
    public void testBreak2() throws Exception {
        // Arrange
        String text = "" +
                "FOR i = 0:5;" +
                "    IF i == 3;" +
                "        #BREAK;" +
                "    ENDIF;" +
                "    #TEST;" +
                "ENDFOR;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor, times(3)).evaluate(any(), any(), any(), any());
    }

    @org.junit.Test
    public void testContinue() throws Exception {
        // Arrange
        String text = "" +
                "x = 0;" +
                "i = 0;" +
                "WHILE i < 5;" +
                "    i = i + 1;" +
                "    IF x > 1;" +
                "        #CONTINUE;" +
                "    ENDIF;" +
                "    x = x + 1;" +
                "ENDWHILE;" +
                "#TEST x, i;" +
                "" +
                "x = 0;" +
                "FOR i = 0:6;" +
                "    IF x > 1;" +
                "        #CONTINUE;" +
                "    ENDIF;" +
                "    x = x + 1;" +
                "ENDFOR;" +
                "#TEST2 x, i;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .overrideTaskSupervisor(mockTask)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(2), eq(5));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(2), eq(5));
    }

    @org.junit.Test
    public void testContinueIterator() throws Exception {
        // Arrange
        String text = "" +
                "sum = 0;" +
                "FOR val = arr;" +
                "    IF val == 1 || val == 5;" +
                "        #CONTINUE;" +
                "    ENDIF;" +
                "    sum = sum + val;" +
                "ENDFOR;" +
                "#TEST sum;" +
                "" +
                "sum = 0;" +
                "FOR val = iter;" +
                "    IF val == 1 || val == 5;" +
                "        #CONTINUE;" +
                "    ENDIF;" +
                "    sum = sum + val;" +
                "ENDFOR;" +
                "#TEST2 sum;";


        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .addScriptVariable("arr", new int[]{1, 2, 3, 4, 5})
                .addScriptVariable("iter", Arrays.asList(1, 2, 3, 4, 5))
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(9));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(9));
    }

    @org.junit.Test
    public void testSyncAsync() throws Exception {
        // Arrange
        String text = ""
                + "SYNC;"
                + "    #TEST1;"
                + "ENDSYNC;"
                + "ASYNC;"
                + "    #TEST2;"
                + "ENDASYNC;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        TaskSupervisor mockTaskSupervisor = mock(TaskSupervisor.class);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .overrideTaskSupervisor(mockTaskSupervisor)
                .build();

        when(mockTaskSupervisor.submitSync(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, Callable.class).call();
            return mock(Future.class);
        });
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(mockTaskSupervisor).submitAsync(any());

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any());
        verify(mockExecutor2).evaluate(any(), any(), any());
        verify(mockTaskSupervisor).submitSync(any());
        verify(mockTaskSupervisor).submitAsync(any());
    }

    @org.junit.Test
    public void testSyncAsync2() throws Exception {
        // Arrange
        String text = ""
                + "SYNC;"
                + "    FOR i = 0:5;"
                + "        #TEST1;"
                + "    ENDFOR;"
                + "ENDSYNC;"
                + "ASYNC;"
                + "    FOR j = 0:5;"
                + "        #TEST2;"
                + "    ENDFOR;"
                + "ENDASYNC;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);
        TaskSupervisor mockTaskSupervisor = mock(TaskSupervisor.class);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .overrideTaskSupervisor(mockTaskSupervisor)
                .build();

        when(mockTaskSupervisor.submitSync(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, Callable.class).call();
            return mock(Future.class);
        });
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(mockTaskSupervisor).submitAsync(any());

        // Act
        test.test();

        // Assert
        verify(mockExecutor1, times(5)).evaluate(any(), any(), any());
        verify(mockExecutor2, times(5)).evaluate(any(), any(), any());
        verify(mockTaskSupervisor).submitSync(any());
        verify(mockTaskSupervisor).submitAsync(any());
    }

    @org.junit.Test
    public void testConstructorNoArg() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest();"
                + "#TEST obj;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(new ConstTest()));
    }

    @org.junit.Test
    public void testConstructorOneArg() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest(1);"
                + "#TEST obj;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(new ConstTest(1)));
    }

    @org.junit.Test
    public void testConstructorThreeArg() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest(2, 5.0, \"hoho\");"
                + "#TEST obj;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(new ConstTest(2, 5.0, "hoho")));
    }

    @org.junit.Test
    public void testConstructorVarArg() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest(1, 2, 3, 4, 5);"
                + "#TEST obj;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(),
                eq(new ConstTest(1, 2, Arrays.toString(new int[]{1, 2, 3, 4, 5}))));
    }

    @org.junit.Test
    public void testConstructorCustom() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT " + Vector.class.getName() + ";"
                + "v = Vector();"
                + "v2 = Vector(4,4,2);"
                + "v3 = Vector(4.2,4.4,2.3);"
                + "v4 = Vector(toFloat(3.2), toFloat(4.3), toFloat(5.4));"
                + "#TEST v, v2, v3, v4;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .overrideSelfReference(new SelfReference() {
                    public float toFloat(Number number) {
                        return number.floatValue();
                    }
                })
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(),
                eq(new Vector()),
                eq(new Vector(4, 4, 2)),
                eq(new Vector(4.2, 4.4, 2.3)),
                eq(new Vector(3.2f, 4.3f, 5.4f)));
    }

    @org.junit.Test
    public void testArrayAndClass() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT " + TestEnum.class.getName() + ";"
                + "enumVal = TestEnum.IMTEST;"
                + "arr = array(1);"
                + "arr[0] = enumVal;"
                + "#TEST arr[0];";

        Executor executor = mock(Executor.class);
        when(executor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", executor)
                .overrideSelfReference(new SelfReference() {
                    public Object array(int size) {
                        return new Object[size];
                    }
                })
                .build();

        // Act
        test.test();

        // Assert
        verify(executor).evaluate(any(), any(), any(), eq(TestEnum.IMTEST));
    }

    @org.junit.Test
    public void testNestedAccessor() throws Exception {
        // Arrange
        String text = ""
                + "IMPORT java.lang.Long;" +
                "id = Long.valueOf(\"123456789123456789\").longValue();" +
                "#TEST id;";

        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST", mockExecutor)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor).evaluate(any(), any(), any(), eq(123456789123456789L));
    }

    @org.junit.Test
    public void testGlobalVariableAsFactor() throws Exception {
        // Arrange
        String text = ""
                + "#TEST1 {\"some.temp.var\"} - 4;"
                + "#TEST2 {?\"some.temp.var\"} - 5;";

        Executor mockExecutor1 = mock(Executor.class);
        when(mockExecutor1.evaluate(any(), any(), any(), any())).thenReturn(null);
        Executor mockExecutor2 = mock(Executor.class);
        when(mockExecutor2.evaluate(any(), any(), any(), any())).thenReturn(null);

        Test test = Test.Builder.of(text)
                .putExecutor("TEST1", mockExecutor1)
                .putExecutor("TEST2", mockExecutor2)
                .addGlobalVariable("some.temp.var", 22)
                .addTemporaryGlobalVariable("some.temp.var", 33)
                .build();

        // Act
        test.test();

        // Assert
        verify(mockExecutor1).evaluate(any(), any(), any(), eq(22 - 4));
        verify(mockExecutor2).evaluate(any(), any(), any(), eq(33 - 5));
    }

    @org.junit.Test
    public void testLambdaFunction() throws Exception {
        // Arrange
        String text = "" +
                "abc = 33\n" +
                "instance.noArg(LAMBDA =>\n" +
                "    abc * 3\n" +
                "ENDLAMBDA)\n" +
                "" +
                "instance.oneArg(LAMBDA str => \n" +
                "    added = str + \" Hi\"\n" +
                "    added\n" +
                "ENDLAMBDA)\n" +
                "" +
                "instance.twoArg(LAMBDA a, b => \n" +
                "    a + b\n" +
                "ENDLAMBDA)\n";

        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;

        doAnswer(invocation -> {
            Supplier run = invocation.getArgument(0);
            return run.get();
        }).when(obj).noArg(any(Supplier.class));
        doAnswer(invocation -> {
            Function run = invocation.getArgument(0);
            return run.apply("Something");
        }).when(obj).oneArg(any(Function.class));
        doAnswer(invocation -> {
            BiFunction run = invocation.getArgument(0);
            return run.apply(456, 78);
        }).when(obj).twoArg(any(BiFunction.class));

        Test test = Test.Builder.of(text)
                .addScriptVariable("instance", instance)
                .build();

        // Act
        test.test();

        // Assert
        assertEquals(99, instance.noArgResult);
        assertEquals("Something Hi", instance.oneArgResult);
        assertEquals(456 + 78, instance.twoArgResult);
    }

    @org.junit.Test
    public void testLambdaFunctionNullReturn() throws Exception {
        // Arrange
        String text = "" +
                "instance.twoArg(LAMBDA a, b => \n" +
                "    a + b\n" +
                "    null\n" +
                "ENDLAMBDA)\n";

        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;
        instance.twoArgResult = "abc";

        doAnswer(invocation -> {
            BiFunction run = invocation.getArgument(0);
            return run.apply(456, 78);
        }).when(obj).twoArg(any(BiFunction.class));

        Test test = Test.Builder.of(text)
                .addScriptVariable("instance", instance)
                .build();

        // Act
        test.test();

        // Assert
        assertNull(instance.twoArgResult);
    }

    @org.junit.Test
    public void testLambdaFunctionComplex() throws Exception {
        // Arrange
        String text = "" +
                "instance.oneArg(LAMBDA x => \n" +
                "    IF x == \"Something\"\n" +
                "        50\n" +
                "    ELSE\n" +
                "        100\n" +
                "    ENDIF\n" +
                "ENDLAMBDA)\n";

        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;

        doAnswer(invocation -> {
            Function run = invocation.getArgument(0);
            return run.apply("Something");
        }).when(obj).oneArg(any(Function.class));

        Test test = Test.Builder.of(text)
                .addScriptVariable("instance", instance)
                .build();

        // Act
        test.test();

        // Assert
        assertEquals(50, instance.oneArgResult);
    }

    @org.junit.Test
    public void testLambdaFunctionComplex2() throws Exception {
        // Arrange
        String text = "" +
                "instance.oneArg(LAMBDA x => \n" +
                "    IF x == \"Something\"\n" +
                "        50\n" +
                "    ELSE\n" +
                "        100\n" +
                "    ENDIF\n" +
                "ENDLAMBDA)\n";

        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;

        doAnswer(invocation -> {
            Function run = invocation.getArgument(0);
            return run.apply("NotSomething");
        }).when(obj).oneArg(any(Function.class));

        Test test = Test.Builder.of(text)
                .addScriptVariable("instance", instance)
                .build();

        // Act
        test.test();

        // Assert
        assertEquals(100, instance.oneArgResult);
    }

    public static class TheTest {
        public static String staticField = "staticField";

        public InTest in = new InTest();

        public TheTest() {

        }

        public InTest getTest() {
            return in;
        }

        public String localTest() {
            return "local";
        }

        public TestEnum testEnumMethod(TestEnum val) {
            return val;
        }

        public static String staticTest() {
            return "static";
        }
    }

    public static class TheTest2 {
        public String testEnumMethod(String val) {
            return val;
        }

        public TestEnum testEnumMethod(TestEnum val) {
            return val;
        }
    }

    public static class InTest {
        public InTest2 in = new InTest2();
        public double health = 0.82;

        public boolean hasPermission(String tt) {
            return tt.equals("tt");
        }

        public InTest2 getTest() {
            return in;
        }
    }

    public static class InTest2 {
        public double health = 5.23;

        public double getHealth() {
            return health;
        }
    }

    public enum TestEnum {
        IMTEST
    }

    public static class ConstTest {
        private int val1 = 0;
        private double val2 = 0.0;
        private String val3 = "";

        public ConstTest(int val1, double val2, String val3) {
            super();
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
        }

        public ConstTest(Object val1, Object val2, Object val3) {
            super();
        }

        public ConstTest(int val1) {
            super();
            this.val1 = val1;
        }

        public ConstTest(Object val1) {
            super();
        }

        public ConstTest() {
            super();
        }

        public ConstTest(int... vararg) {
            val1 = vararg[0];
            val2 = vararg[1];
            val3 = Arrays.toString(vararg);
        }

        public ConstTest(Object... vararg) {

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConstTest constTest = (ConstTest) o;
            return val1 == constTest.val1 && Double.compare(constTest.val2, val2) == 0 && Objects.equals(val3, constTest.val3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(val1, val2, val3);
        }

        @Override
        public String toString() {
            return "ConstTest{" +
                    "val1=" + val1 +
                    ", val2=" + val2 +
                    ", val3='" + val3 + '\'' +
                    '}';
        }
    }

    public static class EmptyFuture<T> implements Future<T> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isCancelled() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isDone() {
            // TODO Auto-generated method stub
            return false;
        }

    }

    public static class Vector {
        final int key;

        public Vector(float x, float y, float z) {
            key = 2;
        }

        public Vector(double x, double y, double z) {
            key = 1;
        }

        public Vector(int x, int y, int z) {
            key = 3;
        }

        public Vector() {
            key = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vector vector = (Vector) o;
            return key == vector.key;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public String toString() {
            return "Vector{" +
                    "key=" + key +
                    '}';
        }
    }

    private interface SomeInterface {
        Object noArg(Supplier<Object> run);

        Object oneArg(Function<Object, Object> run);

        Object twoArg(BiFunction<Object, Object, Object> run);
    }

    private class SomeClass {
        SomeInterface obj;
        Object noArgResult;
        Object oneArgResult;
        Object twoArgResult;

        public void noArg(Supplier<Object> run) {
            noArgResult = obj.noArg(run);
        }

        public void oneArg(Function<Object, Object> run) {
            oneArgResult = obj.oneArg(run);
        }

        public void twoArg(BiFunction<Object, Object, Object> run) {
            twoArgResult = obj.twoArg(run);
        }
    }
}
