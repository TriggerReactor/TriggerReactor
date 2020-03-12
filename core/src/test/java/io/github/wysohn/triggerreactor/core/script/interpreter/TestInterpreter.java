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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class TestInterpreter {
    @Test
    public void testMethod() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                String value = String.valueOf(args[0]);
                Assert.assertTrue("0".equals(value) || "1".equals(value) || "2".equals(value));
                return null;
            }
        };
        executorMap.put("MESSAGE", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.getVars().put("common", new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testMethodReturnValue() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
        Charset charset = Charset.forName("UTF-8");
        String text = "{\"temp1\"} = temp.testEnumMethod(\"IMTEST\");"
                + "{\"temp2\"} = temp.testEnumMethod(\"Something\");"
                + "{\"temp3\"} = random(0, 10.0);";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<String, Object> vars = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();
        vars.put("temp", new TheTest());

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setVars(vars);
        interpreter.setGvars(gvars);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        Assert.assertEquals(TestEnum.IMTEST, gvars.get("temp1"));
        Assert.assertEquals("Something", gvars.get("temp2"));
    }

    @Test
    public void testReference() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);

        Assert.assertEquals(12.43, reference.getTest().in.getHealth(), 0.001);
    }

    @Test
    public void testStringAppend() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Object[] arr = (Object[]) args[0];
                Assert.assertEquals("beh0.82", arr[0]);
                Assert.assertEquals("0.82beh", arr[1]);
                Assert.assertEquals("beh11", arr[2]);
                Assert.assertEquals("beh2", arr[3]);
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);
    }

    @Test
    public void testGlobalVariable() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "{text+\".something\"} = 12.54\n"
                + "#MESSAGE {text+\".something\"}\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals(12.54, args[0]);
                return null;
            }
        });
        Map<Object, Object> map = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setGvars(map);

        interpreter.getVars().put("text", "someplayername");
        interpreter.startWithContext(null);

        Assert.assertTrue(map.containsKey("someplayername.something"));
        Assert.assertEquals(12.54, map.get("someplayername.something"));
    }

    @Test
    public void testTempGlobalVariable() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals(12.54, args[0]);
                return null;
            }
        });
        executorMap.put("MESSAGE2", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertNull(args[0]);
                return null;
            }
        });
        TriggerReactorCore triggerReactor = Mockito.mock(TriggerReactorCore.class);
        AbstractVariableManager avm = new AbstractVariableManager(triggerReactor) {
            @Override
            public void remove(String key) {
                Assert.fail("remove() of actual gvar was called");
            }

            @Override
            public boolean has(String key) {
                Assert.fail("has() of actual gvar was called");
                return false;
            }

            @Override
            public void put(String key, Object value) throws Exception {
                Assert.fail("put() of actual gvar was called");
            }

            @Override
            public Object get(String key) {
                Assert.fail("get() of actual gvar was called");
                return null;
            }

            @Override
            public void reload() {

            }

            @Override
            public void saveAll() {

            }
        };
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setGvars(avm.getGlobalVariableAdapter());

        interpreter.getVars().put("text", "someplayername");
        interpreter.startWithContext(null);
    }

    @Test
    public void testGlobalVariableDeletion() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
 
			 
 
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(1, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
 
			 
 
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertNull(args[0]);
                return null;
            }
        });
        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setGvars(gvars);

        interpreter.startWithContext(null);

        Assert.assertNull(gvars.get("temp"));
    }

    @Test
    public void testArray() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1--1-1]\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        String[] args = new String[]{"item1", "item2"};
        interpreter.getVars().put("args", args);
        interpreter.startWithContext(null);
    }

    @Test
    public void testCustomArray() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "args = array(2)\n"
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1--1-1]\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration2() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration3() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration4() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                   Object... args) {
 
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNegation() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
        Charset charset = Charset.forName("UTF-8");
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
                protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                          Object... args) throws Exception {
 
                    Assert.assertEquals("work", args[0]);
                    return null;
                }

            });
            put("TEST2", new Executor() {

                @Override
                protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                          Object... args) throws Exception {
 
                    Assert.assertEquals("work2", args[0]);
                    return null;
                }

            });
        }};

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.getVars().put("player", new InTest());
        interpreter.getVars().put("player2", new InTest());

        interpreter.startWithContext(null);

        interpreter.getVars().remove("player");
        interpreter.getVars().remove("player2");
        interpreter.startWithContext(null);
    }

    @Test
    public void testWhile() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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

        interpreter.startWithContext(null);

        Assert.assertEquals(3, interpreter.getVars().get("number"));
    }

    @Test
    public void testEnumParse() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "result = parseEnum(\"io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter\\$TestEnum\","
                + " \"IMTEST\");";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        Assert.assertEquals(TestEnum.IMTEST, interpreter.getVars().get("result"));
    }

    @Test
    public void testPlaceholder() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("testplayer", args[0]);
                Assert.assertEquals("testwithargs", args[1]);
                return null;
            }

        });

        executorMap.put("TESTSTRING", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertTrue(args[0] instanceof String);
                return null;
            }

        });

        executorMap.put("TESTINTEGER", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertTrue(args[0] instanceof Integer);
                return null;
            }

        });

        executorMap.put("TESTDOUBLE", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertTrue(args[0] instanceof Double);
                return null;
            }

        });

        executorMap.put("TESTBOOLEAN", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
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
                Assert.assertEquals(0, args[0]);
                Assert.assertEquals(100.0, args[1]);
                Assert.assertEquals(true, args[2]);
                Assert.assertEquals("hoho", args[3]);
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
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);

        Assert.assertEquals("testwithargs", interpreter.getVars().get("returnvalue"));
    }
    
    @Test
    public void testPlaceholderNull() throws IOException, LexerException, ParserException, InterpreterException {
    	Charset charset = Charset.forName("UTF-8");
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
        interpreter.startWithContext(null);
        Assert.assertEquals(null, interpreter.getVars().get("a"));
    }

    @Test
    public void testUnaryMinus() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "x = 4.0;"
                + "#TEST1 -1+-5;"
                + "#TEST2 -2.0--5;"
                + "#TEST3 -$test3-5;"
                + "#TEST4 -x-5;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST1", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(-6, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(3.0, args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(-8, args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(-9.0, args[0]);
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
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setSelfReference(new CommonFunctions());

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                Assert.assertEquals("pass", args[0]);
                set.add("true");
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                Assert.assertEquals("fail", args[0]);
                set.add("false");
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("true"));
        Assert.assertFalse(set.contains("false"));
    }

    @Test
    public void testSimpleIf2() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf3() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf4() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf2() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf3() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            interpreter.setVars(localVars);

            interpreter.startWithContext(null);

            Assert.assertEquals(testMap.get(x), localVars.get("result"));
        }
    }

    @Test
    public void testNestedIf4() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf5() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIfNoElse() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(2, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testImport() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(TheTest.class, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("static", args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("local", args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("staticField", args[0]);
                return null;
            }

        });
        executorMap.put("TEST5", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(TestEnum.IMTEST, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testComparison() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(true, args[0]);
                Assert.assertEquals(false, args[1]);
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

        interpreter.startWithContext(null);
    }

    @Test
    public void testNullComparison() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
        interpreter.setGvars(gvars);

        interpreter.startWithContext(null);

        Assert.assertEquals(true, gvars.get("temp"));
    }

    @Test
    public void testLineBreak() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#TEST \"abcd\\nABCD\"";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals("abcd\nABCD", args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testCarriageReturn() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#TEST \"abcd\\rABCD\"";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                Assert.assertEquals("abcd\rABCD", args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testISStatement() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
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
        interpreter.setVars(vars);

        interpreter.startWithContext(null);
    }

    @Test
    public void testBreak() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(2, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(2, args[0]);
                return null;
            }
        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testContinue() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(2, args[0]);
                Assert.assertEquals(5, args[1]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(2, args[0]);
                Assert.assertEquals(5, args[1]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testContinueIterator() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(9, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args) throws Exception {
 
                Assert.assertEquals(9, args[0]);
                return null;
            }
        });
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("arr", new int[]{1, 2, 3, 4, 5});
        vars.put("iter", Arrays.asList(1, 2, 3, 4, 5));
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setVars(vars);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSyncAsync() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test1");
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
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

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test1");
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
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

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test");

                Object obj = args[0];
                Assert.assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                Assert.assertEquals(0, test.val1);
                Assert.assertEquals(0.0, test.val2, 0.000001);
                Assert.assertEquals("", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorOneArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test");

                Object obj = args[0];
                Assert.assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                Assert.assertEquals(1, test.val1);
                Assert.assertEquals(0.0, test.val2, 0.000001);
                Assert.assertEquals("", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorThreeArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test");

                Object obj = args[0];
                Assert.assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                Assert.assertEquals(2, test.val1);
                Assert.assertEquals(5.0, test.val2, 0.000001);
                Assert.assertEquals("hoho", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testConstructorVarArg() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test");

                Object obj = args[0];
                Assert.assertEquals(ConstTest.class, obj.getClass());

                ConstTest test = (ConstTest) obj;
                Assert.assertEquals(1, test.val1);
                Assert.assertEquals(2.0, test.val2, 0.000001);
                Assert.assertEquals("[1, 2, 3, 4, 5]", test.val3);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
    }

    @Test
    public void testArrayAndClass() throws Exception {
        Set<String> set = new HashSet<>();

        Charset charset = Charset.forName("UTF-8");
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
            protected Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> vars, Object context,
                                      Object... args)
 
                    throws Exception {
                set.add("test");

                Assert.assertEquals(TestEnum.IMTEST, args[0]);

                return null;
            }

        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new SelfReference() {
            @SuppressWarnings("unused")
			public Object array(int size) {
                return new Object[size];
            }
        });

        interpreter.startWithContext(null);

        Assert.assertTrue(set.contains("test"));
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

        public String testEnumMethod(String val) {
            return val;
        }

        public static String staticTest() {
            return "static";
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
        IMTEST;
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
}
