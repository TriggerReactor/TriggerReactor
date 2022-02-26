/*******************************************************************************
 *     Copyright (C) 2017, 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
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
    public void init(){
        mockTask = mock(TaskSupervisor.class);
    }

    @Test
    public void testMethod() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "rand = common.random(3);"
                + "IF rand == 0\n"
                + "#MESSAGE 0\n"
                + "ENDIF;"
                + "IF rand == 1;\n"
                + "#MESSAGE 1;\n"
                + "ENDIF\n"
                + "IF rand == 2;"
                + "#MESSAGE 2\n"
                + "ENDIF\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Executor mockExecutor = new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                String value = String.valueOf(args[0]);
                Assert.assertTrue("0".equals(value) || "1".equals(value) || "2".equals(value));
                return null;
            }
        };
        executorMap.put("MESSAGE", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.getVars().put("common", new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testMethodReturnValue() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "{\"temp1\"} = random(0, 10);"
                + "{\"temp2\"} = random(0.0, 10.0);"
                + "{\"temp3\"} = random(0, 10.0);";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setGvars(gvars);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        Assert.assertTrue(gvars.get("temp1") instanceof Integer);
        Assert.assertTrue(gvars.get("temp2") instanceof Double);
        Assert.assertTrue(gvars.get("temp3") instanceof Double);
    }

    @Test
    public void testMethodWithEnumParameter() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "{\"temp1\"} = temp.testEnumMethod(\"IMTEST\");"
                + "{\"temp2\"} = temp2.testEnumMethod(\"Something\");"
                + "{\"temp3\"} = random(0, 10.0);";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<String, Object> vars = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();
        vars.put("temp", new TheTest());
        vars.put("temp2", new TheTest2());

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setVars(vars);
        interpreter.setGvars(gvars);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        // the only method matching is the one with enum parameter. Expect it to be converted
        assertEquals(TestEnum.IMTEST, gvars.get("temp1"));
        // there is an overloaded method which takes in String in the place of enum. Overloaded method has higher priority.
        assertEquals("Something", gvars.get("temp2"));
    }

    @Test
    public void testReference() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
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

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);

        assertEquals(12.43, reference.getTest().in.getHealth(), 0.001);
    }

    @Test
    public void testStringAppend() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "arr = array(4)\n"
                + "arr[0] = \"beh\"+player.in.health\n"
                + "arr[1] = player.in.health+\"beh\"\n"
                + "arr[2] = \"beh\"+1+1\n"
                + "arr[3] = \"beh\"+(1+1)\n"
                + "#MESSAGE arr\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                Object[] arr = (Object[]) args[0];
                assertEquals("beh0.82", arr[0]);
                assertEquals("0.82beh", arr[1]);
                assertEquals("beh11", arr[2]);
                assertEquals("beh2", arr[3]);
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);
    }

    @Test(expected = InterpreterException.class)
    public void testLiteralStringTrueOrFalseParse() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
            + "temp = \"true\"\n"
            + "IF temp == true\n"
            + "  #TEST\n"
            + "ELSE\n"
            + "  error.cause()\n"
            + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        verify(mockExecutor, times(0)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testGlobalVariable() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "{text+\".something\"} = 12.54\n"
                + "#MESSAGE {text+\".something\"}\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals(12.54, args[0]);
                return null;
            }
        });
        Map<Object, Object> map = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setGvars(map);

        interpreter.getVars().put("text", "someplayername");
        interpreter.startWithContext(null);

        Assert.assertTrue(map.containsKey("someplayername.something"));
        assertEquals(12.54, map.get("someplayername.something"));
    }

    @Test
    public void testTempGlobalVariable() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "{?text+\".something\"} = 12.54;"
                + "#MESSAGE {?text+\".something\"};" +
                "{?text+\".something\"} = null;" +
                "#MESSAGE2 {?text+\".something\"};";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals(12.54, args[0]);
                return null;
            }
        });
        executorMap.put("MESSAGE2", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                Assert.assertNull(args[0]);
                return null;
            }
        });
        TriggerReactorCore triggerReactor = mock(TriggerReactorCore.class);
        GlobalVariableManager avm = new GlobalVariableManager(triggerReactor, mock(IConfigSource.class));
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setGvars(avm.getGlobalVariableAdapter());

        interpreter.getVars().put("text", "someplayername");
        interpreter.startWithContext(null);
    }

    @Test
    public void testGlobalVariableDeletion() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "key = \"temp\";" +
                "{key} = 1;" +
                "#TEST1 {key};" +
                "{key} = null;" +
                "#TEST2 {key};";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {
            @Override


            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(1, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override


            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                Assert.assertNull(args[0]);
                return null;
            }
        });
        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setGvars(gvars);

        interpreter.startWithContext(null);

        Assert.assertNull(gvars.get("temp"));
    }

    @Test
    public void testArray() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1- -1-1]\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        String[] args = new String[]{"item1", "item2"};
        interpreter.getVars().put("args", args);
        interpreter.startWithContext(null);
    }

    @Test
    public void testCustomArray() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "args = array(2)\n"
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1- -1-1]\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "FOR i = 0:10\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            int index = 0;

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals(index++, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration3() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "start=0;"
                + "stop=10;"
                + "FOR i = start:stop\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            int index = 0;

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals(index++, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration4() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "start=0;"
                + "stop=10;"
                + "FOR i = start*10-0:stop-10*0\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            int index = 0;

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) {

                assertEquals(index++, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration5() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "FOR i = 0:getPlayers().size()\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), anyInt())).thenReturn(null);
        executorMap.put("MESSAGE", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new SelfReference() {
            public Collection<String> getPlayers(){
                List<String> names = new ArrayList<>();
                for(int i = 0; i < 10; i++)
                    names.add(String.valueOf(i));
                return names;
            }
        });

        interpreter.startWithContext(null);

        verify(executor, times(10)).execute(any(), anyMap(), any(), anyInt());
    }

    @Test(expected = InterpreterException.class)
    public void testOnlyTry() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "ENDTRY;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        verify(executor, times(0)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testTryCatch1() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "   #TEST;" +
                "   #TEST;" +
                "CATCH e;" +
                "   #TEST;" +
                "ENDTRY;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        verify(executor, times(3)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testTryCatch2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "   error.cause();" +
                "   #TEST;" +
                "   #TEST;" +
                "CATCH e;" +
                "   #TEST;" +
                "   #TEST;" +
                "   #TEST;" +
                "ENDTRY;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        verify(executor, times(4)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testTryFinally1() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "FINALLY;" +
                "   #TEST;" +
                "ENDTRY;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        verify(executor, times(2)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testTryCatchFinally1() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "CATCH e;" +
                "   #TEST;" +
                "FINALLY;" +
                "   #TEST;" +
                "ENDTRY;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        verify(executor, times(2)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testTryCatchFinally2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        String text = "" +
                "TRY;" +
                "   #TEST;" +
                "   error.cause();" +
                "   #TEST;" +
                "CATCH e;" +
                "   #TEST;" +
                "FINALLY;" +
                "   #TEST;" +
                "ENDTRY;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        verify(executor, times(3)).execute(any(), anyMap(), any(), any());
    }

    @Test
    public void testTryCatchInvokedMethod() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "" +
                "import java.io.FileReader;" +
                "" +
                "TRY;" +
                "    FileReader(\"./no-exist-file.data\");" +
                "    #VERIFY false;" +
                "CATCH e;" +
                "    #VERIFY true;" +
                "ENDTRY";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();

        Executor executor = mock(Executor.class);
        when(executor.execute(any(), anyMap(), any(), anyBoolean())).thenReturn(null);
        executorMap.put("VERIFY", executor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        verify(executor, times(1)).execute(any(), anyMap(), any(), eq(true));
    }

    @Test
    public void testNegation() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "arr = array(6)\n"
                + "arr[0] = true\n"
                + "arr[1] = !true\n"
                + "arr[2] = !true || false\n"
                + "arr[3] = true && !false\n"
                + "arr[4] = true && 1 < 2 && 5 > 4 && 1 != 2 && 2 == 2 && (false || 2*2 > 3)\n"
                + "arr[5] = false || false || (2 < 3 && 6+5*3 > 1*2+3)";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        Object arr = interpreter.getVars().get("arr");
        Assert.assertTrue((boolean) Array.get(arr, 0));
        Assert.assertFalse((boolean) Array.get(arr, 1));
        Assert.assertFalse((boolean) Array.get(arr, 2));
        Assert.assertTrue((boolean) Array.get(arr, 3));
        Assert.assertTrue((boolean) Array.get(arr, 4));
        Assert.assertTrue((boolean) Array.get(arr, 5));
    }

    @Test
    public void testShortCircuit() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IF player != null && player.health == 0.82;"
                + "    #TEST1 \"work\";"
                + "ENDIF;"
                + "IF player2 == null || player2.health == 0.82;"
                + "    #TEST2 \"work2\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        @SuppressWarnings("serial")
        Map<String, Executor> executorMap = new HashMap<String, Executor>() {
            {
                put("TEST1", new Executor() {

                    @Override
                    public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                           Object... args) throws Exception {
                        assertEquals("work", args[0]);
                        return null;
                    }

                });
                put("TEST2", new Executor() {

                    @Override
                    public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                           Object... args) throws Exception {
                        assertEquals("work2", args[0]);
                        return null;
                    }

                });
            }
        };

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.getVars().put("player", new InTest());
        interpreter.getVars().put("player2", new InTest());

        interpreter.startWithContext(null);

        interpreter.getVars().remove("player");
        interpreter.getVars().remove("player2");
        interpreter.startWithContext(null);
    }

    @Test
    public void testWhile() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "number = 1;"
                + "WHILE number < 3;"
                + "number = number + 1;"
                + "ENDWHILE;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        assertEquals(3, interpreter.getVars().get("number"));
    }

    @Test
    public void testEnumParse() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "result = parseEnum(\"io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter\\$TestEnum\","
                + " \"IMTEST\");";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        assertEquals(TestEnum.IMTEST, interpreter.getVars().get("result"));
    }

    @Test
    public void testPlaceholder() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 100.0;"
                + "returnvalue = $test:0:x:true:\"hoho\";"
                + "#MESSAGE $playername returnvalue;"
                + "#TESTSTRING $string;"
                + "#TESTINTEGER $integer;"
                + "#TESTDOUBLE $double;"
                + "#TESTBOOLEAN $boolean;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("testplayer", args[0]);
                assertEquals("testwithargs", args[1]);
                return null;
            }

        });

        executorMap.put("TESTSTRING", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                Assert.assertTrue(args[0] instanceof String);
                return null;
            }

        });

        executorMap.put("TESTINTEGER", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                Assert.assertTrue(args[0] instanceof Integer);
                return null;
            }

        });

        executorMap.put("TESTDOUBLE", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                Assert.assertTrue(args[0] instanceof Double);
                return null;
            }

        });

        executorMap.put("TESTBOOLEAN", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                Assert.assertTrue(args[0] instanceof Boolean);
                return null;
            }

        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        placeholderMap.put("playername", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                return "testplayer";
            }

        });
        placeholderMap.put("test", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                assertEquals(0, args[0]);
                assertEquals(100.0, args[1]);
                assertEquals(true, args[2]);
                assertEquals("hoho", args[3]);
                return "testwithargs";
            }

        });

        placeholderMap.put("string", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                return "testplayer";
            }

        });

        placeholderMap.put("integer", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                return 1;
            }

        });

        placeholderMap.put("double", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                return 1.5;
            }

        });

        placeholderMap.put("boolean", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                return false;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        assertEquals("testwithargs", interpreter.getVars().get("returnvalue"));
    }

    @Test
    public void testPlaceholderNull() throws IOException, LexerException, ParserException, InterpreterException {
        Charset charset = StandardCharsets.UTF_8;
        String text = "a = $merp";
        Lexer lexer = new Lexer(text, charset);
        Parser parser;
        parser = new Parser(lexer);
        Node root = parser.parse();

        Map<String, Placeholder> placeholderMap = new HashMap<>();

        placeholderMap.put("merp", new Placeholder() {
            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars, Object... args) throws Exception {
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.startWithContext(null);
        assertEquals(null, interpreter.getVars().get("a"));
    }

    @Test
    public void testUnaryMinus() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 4.0;"
                + "#TEST1 -1+-5;"
                + "#TEST2 -2.0- -5;"
                + "#TEST3 -$test3-5;"
                + "#TEST4 -x-5;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-6, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(3.0, args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-8, args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-9.0, args[0]);
                return null;
            }

        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        placeholderMap.put("test3", new Placeholder() {

            @Override
            public Object parse(Timings.Timing timing, Object context, Map<String, Object> vars,
                                Object... args) throws Exception {
                return 3;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testIncrementAndDecrement1() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "a = 2;"
                + "#TEST1 -a;"
                + "#TEST2 a++;"
                + "#TEST3 a--;"
                + "#TEST4 ++a;"
                + "#TEST5 --a;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-2, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(3, args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(3, args[0]);
                return null;
            }

        });
        executorMap.put("TEST5", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testIncrementAndDecrement2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "a = 2.1;"
                + "#TEST1 -a;"
                + "#TEST2 a++;"
                + "#TEST3 a--;"
                + "#TEST4 ++a;"
                + "#TEST5 --a;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-2.1, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2.1, args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(3.1, args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(3.1, args[0]);
                return null;
            }

        });
        executorMap.put("TEST5", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2.1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testBitwiseAndBitshift() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
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

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-258, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-65, args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2147483583, args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-129, args[0]);
                return null;
            }

        });
        executorMap.put("TEST5", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(67, args[0]);
                return null;
            }

        });
        executorMap.put("TEST6", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(-196, args[0]);
                return null;
            }

        });
        executorMap.put("TEST7", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(128, args[0]);
                return null;
            }

        });
        executorMap.put("TEST8", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(true, args[0]);
                return null;
            }

        });
        executorMap.put("TEST9", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(true, args[0]);
                return null;
            }

        });
        executorMap.put("TEST10", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(false, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test(expected = InterpreterException.class)
    public void testBitwiseException1() throws Exception {
        {
            Charset charset = StandardCharsets.UTF_8;
            String text = "x = 1&true;";

            Lexer lexer = new Lexer(text, charset);
            Parser parser = new Parser(lexer);

            Node root = parser.parse();
            Interpreter interpreter = new Interpreter(root);
            interpreter.setTaskSupervisor(mockTask);
            interpreter.startWithContext(null);
        }
    }

    @Test(expected = InterpreterException.class)
    public void testBitwiseException2() throws Exception {
        {
            Charset charset = StandardCharsets.UTF_8;
            String text = "x = false^2.0;";

            Lexer lexer = new Lexer(text, charset);
            Parser parser = new Parser(lexer);

            Node root = parser.parse();
            Interpreter interpreter = new Interpreter(root);
            interpreter.setTaskSupervisor(mockTask);
            interpreter.startWithContext(null);
        }
    }

    @Test(expected = InterpreterException.class)
    public void testBitwiseException3() throws Exception {
        {
            Charset charset = StandardCharsets.UTF_8;
            String text = "x = 1|2.1;";

            Lexer lexer = new Lexer(text, charset);
            Parser parser = new Parser(lexer);

            Node root = parser.parse();
            Interpreter interpreter = new Interpreter(root);
            interpreter.setTaskSupervisor(mockTask);
            interpreter.startWithContext(null);
        }
    }

    @Test
    public void testSimpleIf() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 4.0;"
                + "IF x > 0.0;"
                + "    #TEST1 \"pass\";"
                + "ELSE;"
                + "    #TEST2 \"failed\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                assertEquals("pass", args[0]);
                set.add("true");
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                assertEquals("fail", args[0]);
                set.add("false");
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("true"));
        Assert.assertFalse(set.contains("false"));
    }

    @Test
    public void testSimpleIf2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IF someunknown != 0.0;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf3() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IF 0.0 != someunknown;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf4() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IF someunknown == someunknown;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"failed\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 4.0;"
                + "IF x < 0.0;"
                + "    #TEST \"no\";"
                + "ELSEIF x > 0.0;"
                + "    #TEST \"pass\";"
                + "ELSE;"
                + "    #TEST \"no\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 4.0;"
                + "IF x < 0.0;"
                + "    #TEST \"no\";"
                + "ELSEIF x < -5.0;"
                + "    #TEST \"no\";"
                + "ELSE;"
                + "    #TEST \"pass\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf3() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IF x > 999;"
                + "    result = \"test1\";"
                + "ELSEIF x > 99;"
                + "    result = \"test2\";"
                + "ELSEIF x > 9;"
                + "    IF x < 11;"
                + "        result = \"test5\";"
                + "    ENDIF;"
                + "ELSEIF x > 4;"
                + "    result = \"test3\";"
                + "ELSE;"
                + "    result = \"test4\";"
                + "ENDIF;";

        Map<Integer, String> testMap = new HashMap<>();
        testMap.put(1000, "test1");
        testMap.put(100, "test2");
        testMap.put(10, "test5");
        testMap.put(5, "test3");
        testMap.put(0, "test4");

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();

        int x = 0;
        Map<String, Executor> executorMap = new HashMap<>();

        for (Entry<Integer, String> entry : testMap.entrySet()) {
            x = entry.getKey();

            Map<String, Object> localVars = new HashMap<>();
            localVars.put("x", x);

            Interpreter interpreter = new Interpreter(root);
            interpreter.setExecutorMap(executorMap);
            interpreter.setTaskSupervisor(mockTask);
            interpreter.setVars(localVars);

            interpreter.startWithContext(null);

            assertEquals(testMap.get(x), localVars.get("result"));
        }
    }

    @Test
    public void testNestedIf4() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
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

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf5() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
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

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIfNoElse() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
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

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testImport() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "IMPORT io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter$TheTest;"
                + "IMPORT io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter$TestEnum;"
                + "#TEST TheTest;"
                + "#TEST2 TheTest.staticTest();"
                + "#TEST3 TheTest().localTest();"
                + "#TEST4 TheTest.staticField;"
                + "#TEST5 TestEnum.IMTEST;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(TheTest.class, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("static", args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("local", args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("staticField", args[0]);
                return null;
            }

        });
        executorMap.put("TEST5", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(TestEnum.IMTEST, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testImportAs() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "IMPORT " + TheTest.class.getName() + " as SomeClass;"
            + "IMPORT " + TestEnum.class.getName() + " as SomeEnum;"
            + "#TEST SomeClass;"
            + "#TEST SomeClass.staticTest();"
            + "#TEST SomeClass().localTest();"
            + "#TEST SomeClass.staticField;"
            + "#TEST SomeEnum.IMTEST;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Executor mockExecutor = mock(Executor.class);
        when(mockExecutor.execute(any(), anyMap(), any(), any())).thenReturn(null);
        executorMap.put("TEST", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        InOrder inOrder = inOrder(mockExecutor);
        inOrder.verify(mockExecutor).execute(any(), anyMap(), any(), eq(TheTest.class));
        inOrder.verify(mockExecutor).execute(any(), anyMap(), any(), eq("static"));
        inOrder.verify(mockExecutor).execute(any(), anyMap(), any(), eq("local"));
        inOrder.verify(mockExecutor).execute(any(), anyMap(), any(), eq("staticField"));
        inOrder.verify(mockExecutor).execute(any(), anyMap(), any(), eq(TestEnum.IMTEST));
    }

    @Test
    public void testComparison() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "#TEST 1 < 2, 2 < 1;"
                + "#TEST2 5 > 4, 4 > 5;"
                + "#TEST3 1 <= 1, 3 <= 2;"
                + "#TEST4 1 >= 1, 2 >= 3;"
                + "#TEST5 \"tt\" == \"tt\", \"bb\" == \"bt\";"
                + "#TEST6 \"tt\" != \"bb\", \"bb\" != \"bb\";";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Executor exec = new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(true, args[0]);
                assertEquals(false, args[1]);
                return null;
            }

        };
        executorMap.put("TEST", exec);
        executorMap.put("TEST2", exec);
        executorMap.put("TEST3", exec);
        executorMap.put("TEST4", exec);
        executorMap.put("TEST5", exec);
        executorMap.put("TEST6", exec);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNullComparison() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "IF {\"temp\"} == null;"
                + "{\"temp\"} = true;"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setGvars(gvars);

        interpreter.startWithContext(null);

        assertEquals(true, gvars.get("temp"));
    }

    @Test
    public void testLineBreak() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "#TEST \"abcd\\nABCD\"";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals("abcd\nABCD", args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testCarriageReturn() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "#TEST \"abcd\\rABCD\"";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                assertEquals("abcd\rABCD", args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testISStatement() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "IMPORT " + TheTest.class.getName() + ";" +
                "IMPORT " + InTest.class.getName() + ";" +
                "" +
                "#TEST test IS TheTest, test IS InTest;" +
                "#TEST test2 IS InTest, test2 IS TheTest;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                Assert.assertTrue((boolean) args[0]);
                Assert.assertFalse((boolean) args[1]);
                return null;
            }
        });

        HashMap<String, Object> vars = new HashMap<>();
        vars.put("test", new TheTest());
        vars.put("test2", new InTest());
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setVars(vars);

        interpreter.startWithContext(null);
    }

    @Test
    public void testBreak() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 0;" +
                "WHILE x < 5;" +
                "x = x + 1;" +
                "IF x > 1;" +
                "#BREAK;" +
                "ENDIF;" +
                "ENDWHILE;" +
                "#TEST x;" +
                "" +
                "FOR x = 0:10;" +
                "IF x == 2;" +
                "#BREAK;" +
                "ENDIF;" +
                "ENDFOR;" +
                "#TEST2 x";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }


    @Test
    public void testBreak2() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "FOR i = 0:5;" +
                "IF i == 3;" +
                "#BREAK;" +
                "ENDIF;" +
                "#TEST;" +
                "ENDFOR;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor mockExecutor = mock(Executor.class);
        Mockito.when(mockExecutor.execute(Mockito.any(), Mockito.anyMap(),
                Mockito.any(), ArgumentMatchers.any())).thenReturn(null);
        executorMap.put("TEST", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        Mockito.verify(mockExecutor, Mockito.times(3)).execute(Mockito.any(), Mockito.anyMap(),
                Mockito.any(), ArgumentMatchers.any());
    }

    @Test
    public void testContinue() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = 0;" +
                "i = 0;" +
                "WHILE i < 5;" +
                "i = i + 1;" +
                "IF x > 1;" +
                "#CONTINUE;" +
                "ENDIF;" +
                "x = x + 1;" +
                "ENDWHILE;" +
                "#TEST x, i;" +
                "" +
                "x = 0;" +
                "FOR i = 0:6;" +
                "IF x > 1;" +
                "#CONTINUE;" +
                "ENDIF;" +
                "x = x + 1;" +
                "ENDFOR;" +
                "#TEST2 x, i;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                assertEquals(5, args[1]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(2, args[0]);
                assertEquals(5, args[1]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
    }

    @Test
    public void testContinueIterator() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "sum = 0;" +
                "FOR val = arr;" +
                "IF val == 1 || val == 5;" +
                "#CONTINUE;" +
                "ENDIF;" +
                "sum = sum + val;" +
                "ENDFOR;" +
                "#TEST sum;" +
                "" +
                "sum = 0;" +
                "FOR val = iter;" +
                "IF val == 1 || val == 5;" +
                "#CONTINUE;" +
                "ENDIF;" +
                "sum = sum + val;" +
                "ENDFOR;" +
                "#TEST2 sum;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(9, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args) throws Exception {

                assertEquals(9, args[0]);
                return null;
            }
        });
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("arr", new int[]{1, 2, 3, 4, 5});
        vars.put("iter", Arrays.asList(1, 2, 3, 4, 5));
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setVars(vars);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSyncAsync() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "SYNC;"
                + "#TEST1;"
                + "ENDSYNC;"
                + "ASYNC;"
                + "#TEST2;"
                + "ENDASYNC;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test1");
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test2");
                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(new TaskSupervisor() {

            /* (non-Javadoc)
             * @see io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor#submitSync(java.util.concurrent.Callable)
             */
            @Override
            public <T> Future<T> submitSync(Callable<T> call) {
                try {
                    call.call();
                    set.add("sync");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new EmptyFuture<T>();
            }

            @Override
            public void submitAsync(Runnable run) {
                try {
                    run.run();
                    set.add("async");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean isServerThread() {
                return true;
            }

        });

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test1"));
        Assert.assertTrue(set.contains("test2"));
        Assert.assertTrue(set.contains("sync"));
        Assert.assertTrue(set.contains("async"));
    }

    @Test
    public void testSyncAsync2() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "SYNC;"
                + "FOR i = 0:1;"
                + "#TEST1;"
                + "ENDFOR;"
                + "ENDSYNC;"
                + "ASYNC;"
                + "FOR j = 0:1;"
                + "#TEST2;"
                + "ENDFOR;"
                + "ENDASYNC;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test1");
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test2");
                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(new TaskSupervisor() {

            /* (non-Javadoc)
             * @see io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor#submitSync(java.util.concurrent.Callable)
             */
            @Override
            public <T> Future<T> submitSync(Callable<T> call) {
                try {
                    call.call();
                    set.add("sync");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new EmptyFuture<T>();
            }

            @Override
            public void submitAsync(Runnable run) {
                try {
                    run.run();
                    set.add("async");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean isServerThread() {
                return true;
            }

        });

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test1"));
        Assert.assertTrue(set.contains("test2"));
        Assert.assertTrue(set.contains("sync"));
        Assert.assertTrue(set.contains("async"));
    }

    @Test
    public void testConstructorNoArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest();"
                + "#TEST obj;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test");

                Object obj = args[0];
                assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                assertEquals(0, test.val1);
                assertEquals(0.0, test.val2, 0.000001);
                assertEquals("", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorOneArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest(1);"
                + "#TEST obj;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test");

                Object obj = args[0];
                assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                assertEquals(1, test.val1);
                assertEquals(0.0, test.val2, 0.000001);
                assertEquals("", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorThreeArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest(2, 5.0, \"hoho\");"
                + "#TEST obj;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test");

                Object obj = args[0];
                assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                assertEquals(2, test.val1);
                assertEquals(5.0, test.val2, 0.000001);
                assertEquals("hoho", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorVarArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT " + ConstTest.class.getName() + ";"
                + "obj = ConstTest(1, 2, 3, 4, 5);"
                + "#TEST obj;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test");

                Object obj = args[0];
                assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                assertEquals(1, test.val1);
                assertEquals(2.0, test.val2, 0.000001);
                assertEquals("[1, 2, 3, 4, 5]", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorCustom() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT " + Vector.class.getName() + ";"
                + "v = Vector();"
                + "v2 = Vector(4,4,2);"
                + "v3 = Vector(4.2,4.4,2.3);"
                + "v4 = Vector(toFloat(3.2), toFloat(4.3), toFloat(5.4));";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();

        Map<String, Executor> executorMap = new HashMap<>();
        Executor mockExecutor = mock(Executor.class);
        executorMap.put("TEST", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setSelfReference(new SelfReference() {
            public float toFloat(Number number) {
                return number.floatValue();
            }
        });

        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.startWithContext(null);

        Map<String, Object> vars = interpreter.getVars();

        assertEquals(new Vector(), vars.get("v"));
        assertEquals(new Vector(4, 4, 2), vars.get("v2"));
        assertEquals(new Vector(4.2, 4.4, 2.3), vars.get("v3"));
        assertEquals(new Vector(3.2f, 4.3f, 5.4f), vars.get("v4"));
    }

    @Test
    public void testArrayAndClass() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT " + TestEnum.class.getName() + ";"
                + "enumVal = TestEnum.IMTEST;"
                + "arr = array(1);"
                + "arr[0] = enumVal;"
                + "#TEST arr[0];";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object context,
                                   Object... args)

                    throws Exception {
                set.add("test");

                assertEquals(TestEnum.IMTEST, args[0]);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.setSelfReference(new SelfReference() {
            @SuppressWarnings("unused")
            public Object array(int size) {
                return new Object[size];
            }
        });

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testNestedAccessor() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "IMPORT java.lang.Long;" +
                "id = Long.valueOf(\"123456789123456789\").longValue()";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new SelfReference() {
            @SuppressWarnings("unused")
            public Object array(int size) {
                return new Object[size];
            }
        });
        interpreter.setTaskSupervisor(mockTask);

        interpreter.startWithContext(null);
        assertEquals(123456789123456789L, interpreter.getVars().get("id"));
    }

    @Test
    public void testGlobalVariableAsFactor() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = ""
                + "result = {\"some.temp.var\"} - 4;"
                + "result2 = {?\"some.temp.var\"} - 5;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);

        Map<Object, Object> globalVar = new HashMap<>();
        globalVar.put("some.temp.var", 22);
        globalVar.put(new TemporaryGlobalVariableKey("some.temp.var"), 22);
        interpreter.setGvars(globalVar);

        interpreter.startWithContext(null);
        assertEquals(18, interpreter.getVars().get("result"));
        assertEquals(17, interpreter.getVars().get("result2"));
    }

    @Test
    public void testLambdaFunction() throws Exception {
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

        Charset charset = StandardCharsets.UTF_8;
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
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.getVars().put("instance", instance);

        interpreter.start();
        assertEquals(33, interpreter.getVars().get("abc"));
        assertNull(interpreter.getVars().get("str"));
        assertNull(interpreter.getVars().get("added"));
        assertNull(interpreter.getVars().get("a"));
        assertNull(interpreter.getVars().get("b"));

        assertEquals(99, instance.noArgResult);
        assertEquals("Something Hi", instance.oneArgResult);
        assertEquals(456 + 78, instance.twoArgResult);
    }

    @Test
    public void testLambdaFunctionNullReturn() throws Exception {
        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;
        instance.twoArgResult = "abc";

        doAnswer(invocation -> {
            BiFunction run = invocation.getArgument(0);
            return run.apply(456, 78);
        }).when(obj).twoArg(any(BiFunction.class));

        Charset charset = StandardCharsets.UTF_8;
        String text = "" +
                "instance.twoArg(LAMBDA a, b => \n" +
                "    a + b\n" +
                "    null\n" +
                "ENDLAMBDA)\n";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.getVars().put("instance", instance);

        interpreter.start();
        assertNull(interpreter.getVars().get("a"));
        assertNull(interpreter.getVars().get("b"));

        assertNull(instance.twoArgResult);
    }

    @Test
    public void testLambdaFunctionComplex() throws Exception {
        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;

        doAnswer(invocation -> {
            Function run = invocation.getArgument(0);
            return run.apply("Something");
        }).when(obj).oneArg(any(Function.class));

        Charset charset = StandardCharsets.UTF_8;
        String text = "" +
                "instance.oneArg(LAMBDA x => \n" +
                "    IF x == \"Something\"\n" +
                "        50\n" +
                "    ELSE\n" +
                "        100\n" +
                "    ENDIF\n" +
                "ENDLAMBDA)\n";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.getVars().put("instance", instance);

        interpreter.start();
        assertNull(interpreter.getVars().get("a"));
        assertNull(interpreter.getVars().get("b"));

        assertEquals(50, instance.oneArgResult);
    }

    @Test
    public void testLambdaFunctionComplex2() throws Exception {
        SomeInterface obj = mock(SomeInterface.class);
        SomeClass instance = new SomeClass();

        instance.obj = obj;

        doAnswer(invocation -> {
            Function run = invocation.getArgument(0);
            return run.apply("NotSomething");
        }).when(obj).oneArg(any(Function.class));

        Charset charset = StandardCharsets.UTF_8;
        String text = "" +
                "instance.oneArg(LAMBDA x => \n" +
                "    IF x == \"Something\"\n" +
                "        50\n" +
                "    ELSE\n" +
                "        100\n" +
                "    ENDIF\n" +
                "ENDLAMBDA)\n";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setTaskSupervisor(mockTask);
        interpreter.getVars().put("instance", instance);

        interpreter.start();
        assertNull(interpreter.getVars().get("a"));
        assertNull(interpreter.getVars().get("b"));

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

    private interface SomeInterface{
        Object noArg(Supplier<Object> run);

        Object oneArg(Function<Object, Object> run);

        Object twoArg(BiFunction<Object, Object, Object> run);
    }

    private class SomeClass{
        SomeInterface obj;
        Object noArgResult;
        Object oneArgResult;
        Object twoArgResult;

        public void noArg(Supplier<Object> run){
            noArgResult = obj.noArg(run);
        }

        public void oneArg(Function<Object, Object> run){
            oneArgResult = obj.oneArg(run);
        }

        public void twoArg(BiFunction<Object, Object, Object> run){
            twoArgResult = obj.twoArg(run);
        }
    }
}
