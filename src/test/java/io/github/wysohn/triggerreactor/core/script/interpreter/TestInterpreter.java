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

import static org.mockito.Mockito.*;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Test;
import org.mockito.Mockito;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import junit.framework.Assert;

public class TestInterpreter {
    @Test
    public void testMethod() throws Exception{
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
        Executor mockExecutor = new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                String value = String.valueOf(args[0]);
                Assert.assertTrue("0".equals(value) || "1".equals(value)||"2".equals(value));
                return null;
            }};
        executorMap.put("MESSAGE", mockExecutor);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.getVars().put("common", new CommonFunctions(null));

        interpreter.startWithContext(null);
    }
    
    @Test
    public void testMethodReturnValue() throws Exception{
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
        interpreter.setSelfReference(new CommonFunctions(null));

        interpreter.startWithContext(null);

        Assert.assertTrue(gvars.get("temp1") instanceof Integer);
        Assert.assertTrue(gvars.get("temp2") instanceof Double);
        Assert.assertTrue(gvars.get("temp3") instanceof Double);
    }
    
    @Test
    public void testMethodWithEnumParameter() throws Exception{
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
        interpreter.setSelfReference(new CommonFunctions(null));

        interpreter.startWithContext(null);

        Assert.assertEquals(TestEnum.IMTEST, gvars.get("temp1"));
        Assert.assertEquals("Something", gvars.get("temp2"));
    }

    @Test
    public void testReference() throws Exception{
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
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
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
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Object[] arr = (Object[]) args[0];
                Assert.assertEquals("beh0.82",  arr[0]);
                Assert.assertEquals("0.82beh",  arr[1]);
                Assert.assertEquals("beh11",  arr[2]);
                Assert.assertEquals("beh2",  arr[3]);
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions(null));
        
        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);
    }

    boolean takeItem = false;
    @Test
    public void testSimpleTrigger() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        int ecoValue = 10;
        String text = ""
                + "IF command == \"iron\"\n"
                + "    IF takeItem(player, \"IRON_INGOT\", 1)\n"
                + "        #SOUND \"LEVEL_UP\",1.0,-2.0,player.getLocation()\n"
                + "        #CMDCON \"econ add \"+player.getName()+\" "+ecoValue+"\"\n"
                + "        #MESSAGE \"Sold!\"\n"
                + "    ELSE\n"
                + "        #MESSAGE \"not enough iron.\"\n"
                + "    ENDIF\n"
                + "ENDIF\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                if(!takeItem){
                    Assert.assertEquals("not enough iron.", (String) args[0]);
                }else{
                    Assert.assertEquals("Sold!", (String) args[0]);
                }
                return null;
            }
        });
        Location mockLocation = mock(Location.class);
        executorMap.put("SOUND", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals("LEVEL_UP", args[0]);
                Assert.assertEquals(1.0, args[1]);
                Assert.assertEquals(-2.0, args[2]);
                Assert.assertEquals(mockLocation, args[3]);
                return null;
            }
        });
        String playerName = "TestPlayer";
        executorMap.put("CMDCON", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                if(takeItem)
                    Assert.assertEquals("econ add "+playerName+" "+ecoValue, args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions(null));

        Player mockPlayer = mock(Player.class);
        PlayerInventory mockInven = mock(PlayerInventory.class);

        when(mockPlayer.getInventory()).thenReturn(mockInven);
        when(mockPlayer.getLocation()).thenReturn(mockLocation);
        when(mockPlayer.getName()).thenReturn(playerName);

        interpreter.getVars().put("player", mockPlayer);
        interpreter.getVars().put("text", "hello");
        interpreter.getVars().put("command", "iron");

        takeItem = false;
        when(mockInven.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt())).thenReturn(takeItem);
        interpreter.startWithContext(null);

        System.out.println();

        takeItem = true;
        when(mockInven.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt())).thenReturn(takeItem);
        interpreter.startWithContext(null);
    }

    @Test
    public void testGlobalVariable() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "{text+\".something\"} = 12.54\n"
                + "#MESSAGE {text+\".something\"}\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
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
    public void testTempGlobalVariable() throws Exception{
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
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals(12.54, args[0]);
                return null;
            }
        });
        executorMap.put("MESSAGE2", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertNull(args[0]);
                return null;
            }
        });
        TriggerReactor triggerReactor = Mockito.mock(TriggerReactor.class);
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
    public void testGlobalVariableDeletion() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(1, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
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
    public void testArray() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "args[0] = \"arg1\"\n"
                + "args[1] = \"arg2\"\n"
                + "#MESSAGE args[0]+\", \"+args[1*-1*-1+1-1--1-1]\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
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
    public void testCustomArray() throws Exception{
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
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions(null));

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration() throws Exception{
        CommonFunctions mockFunctions = mock(CommonFunctions.class);
        Player[] mockPlayer = new Player[2];
        mockPlayer[0] = mock(Player.class);
        mockPlayer[1] = mock(Player.class);
        when(mockPlayer[0].getName()).thenReturn("Player0");
        when(mockPlayer[1].getName()).thenReturn("Player1");

        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "FOR player = getPlayers()\n"
                + "    #MESSAGE player.getName()\n"
                + "ENDFOR\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            int index = 0;
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals(mockPlayer[index].getName(), args[0]);
                index++;
                return null;
            }
        });

        Collection players = new ArrayList<Player>(){{add(mockPlayer[0]); add(mockPlayer[1]);}};
        when(mockFunctions.getPlayers()).thenReturn(players);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(mockFunctions);

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration2() throws Exception{
        CommonFunctions mockFunctions = mock(CommonFunctions.class);
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "FOR i = 0:10\n"
                + "    #MESSAGE i\n"
                + "ENDFOR\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            int index = 0;
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Collection players = new ArrayList<Player>(){{add(mockPlayer1); add(mockPlayer2);}};
        when(mockFunctions.getPlayers()).thenReturn(players);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions(null));

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration3() throws Exception{
        CommonFunctions mockFunctions = mock(CommonFunctions.class);
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

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
        executorMap.put("MESSAGE", new Executor(){
            int index = 0;
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Collection players = new ArrayList<Player>(){{add(mockPlayer1); add(mockPlayer2);}};
        when(mockFunctions.getPlayers()).thenReturn(players);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testIteration4() throws Exception{
        CommonFunctions mockFunctions = mock(CommonFunctions.class);
        Player mockPlayer1 = mock(Player.class);
        Player mockPlayer2 = mock(Player.class);

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
        executorMap.put("MESSAGE", new Executor(){
            int index = 0;
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Collection players = new ArrayList<Player>(){{add(mockPlayer1); add(mockPlayer2);}};
        when(mockFunctions.getPlayers()).thenReturn(players);

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void currentAreaTest() throws Exception{
        Player mockPlayer = mock(Player.class);
        CommonFunctions mockFunctions = mock(CommonFunctions.class);

        Charset charset = Charset.forName("UTF-8");
        String text = "areaName = currentArea(player)\n"
                + "IF areaName == \"tutorialArea\"\n"
                + "#MESSAGE \"Not valid in here.\"\n"
                + "ELSE\n"
                + "#GUI \"menu\"\n"
                + "ENDIF\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals("Not valid in here.", String.valueOf(args[0]));
                return null;
            }
        });
        executorMap.put("GUI", new Executor(){
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                Assert.assertEquals("menu", String.valueOf(args[0]));
                return null;
            }
        });

        Interpreter interpreter;

        when(mockFunctions.currentArea(Mockito.any(Player.class))).thenReturn("tutorialArea");
        interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(mockFunctions);
        
        interpreter.getVars().put("player", mockPlayer);
        interpreter.startWithContext(null);
    }

    @Test
    public void testNegation() throws Exception{
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
        interpreter.setSelfReference(new CommonFunctions(null));

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
    public void testShortCircuit() throws Exception{
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
        Map<String, Executor> executorMap = new HashMap<String, Executor>() {{
            put("TEST1", new Executor() {

                @Override
                protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                    Assert.assertEquals("work", args[0]);
                    return null;
                }

            });
            put("TEST2", new Executor() {

                @Override
                protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
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
    public void testWhile() throws Exception{
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
    public void testEnumParse() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "result = parseEnum(\"io.github.wysohn.triggerreactor.core.script.interpreter.TestInterpreter$TestEnum\","
                + " \"IMTEST\");";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setSelfReference(new CommonFunctions(null));

        interpreter.startWithContext(null);

        Assert.assertEquals(TestEnum.IMTEST, interpreter.getVars().get("result"));
    }

    @Test
    public void testPlaceholder() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("testplayer", args[0]);
                Assert.assertEquals("testwithargs", args[1]);
                return null;
            }

        });

        executorMap.put("TESTSTRING", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertTrue(args[0] instanceof String);
                return null;
            }

        });

        executorMap.put("TESTINTEGER", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertTrue(args[0] instanceof Integer);
                return null;
            }

        });

        executorMap.put("TESTDOUBLE", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertTrue(args[0] instanceof Double);
                return null;
            }

        });

        executorMap.put("TESTBOOLEAN", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertTrue(args[0] instanceof Boolean);
                return null;
            }

        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        placeholderMap.put("playername", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                return "testplayer";
            }

        });
        placeholderMap.put("test", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                Assert.assertEquals(0, args[0]);
                Assert.assertEquals(100.0, args[1]);
                Assert.assertEquals(true, args[2]);
                Assert.assertEquals("hoho", args[3]);
                return "testwithargs";
            }

        });

        placeholderMap.put("string", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                return "testplayer";
            }

        });

        placeholderMap.put("integer", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                return 1;
            }

        });

        placeholderMap.put("double", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                return 1.5;
            }

        });

        placeholderMap.put("boolean", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                return false;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setSelfReference(new CommonFunctions(null));
        
        interpreter.startWithContext(null);

        Assert.assertEquals("testwithargs", interpreter.getVars().get("returnvalue"));
    }

    @Test
    public void testUnaryMinus() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(-6, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(3.0, args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(-8, args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(-9.0, args[0]);
                return null;
            }

        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        placeholderMap.put("test3", new Placeholder() {

            @Override
            public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
                return 3;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.setSelfReference(new CommonFunctions(null));

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = "x = 4.0;"
                + "IF x > 0.0;"
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf2() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf3() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        
        interpreter.startWithContext(null);
    }

    @Test
    public void testSimpleIf4() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf2() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf3() throws Exception{
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

        for(Entry<Integer, String> entry : testMap.entrySet()) {
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
    public void testNestedIf4() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIf5() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(1, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testNestedIfNoElse() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = "x = 4.0;"
                + "IF x < 0.0;"
                + "    #TEST \"no\";"
                + "ELSEIF x > 0.0;"
                + "    #TEST \"pass\";"
                + "ENDIF;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("pass", args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testImport() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(TheTest.class, args[0]);
                return null;
            }

        });
        executorMap.put("TEST2", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("static", args[0]);
                return null;
            }

        });
        executorMap.put("TEST3", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("local", args[0]);
                return null;
            }

        });
        executorMap.put("TEST4", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals("staticField", args[0]);
                return null;
            }

        });
        executorMap.put("TEST5", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(TestEnum.IMTEST, args[0]);
                return null;
            }

        });

        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);

        interpreter.startWithContext(null);
    }

    @Test
    public void testComparison() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
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
    public void testNullComparison() throws Exception{
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
    public void testLineBreak() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = "#TEST \"abcd\\nABCD\"";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
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
			protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args)
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
    public void testISStatement() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = "IMPORT "+TheTest.class.getName()+";" +
                "IMPORT "+InTest.class.getName()+";" +
                "" +
                "#TEST test IS TheTest, test IS InTest;" +
                "#TEST test2 IS InTest, test2 IS TheTest;";
        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);
        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
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
    public void testBreak() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(2, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(2, args[0]);
                return null;
            }
        });
        
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        
        interpreter.startWithContext(null);
    }

    @Test
    public void testContinue() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(2, args[0]);
                Assert.assertEquals(5, args[1]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
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
    public void testContinueIterator() throws Exception{
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
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(9, args[0]);
                return null;
            }
        });
        executorMap.put("TEST2", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                Assert.assertEquals(9, args[0]);
                return null;
            }
        });
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("arr", new int[]{1,2,3,4,5});
        vars.put("iter", Lists.newArrayList(1,2,3,4,5));
        Interpreter interpreter = new Interpreter(root);
        interpreter.setExecutorMap(executorMap);
        interpreter.setVars(vars);
        
        interpreter.startWithContext(null);
    }
    
    @Test
    public void testSyncAsync() throws Exception{
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
			protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args)
					throws Exception {
				set.add("test1");
				return null;
			}
        	
        });
        executorMap.put("TEST2", new Executor() {

			@Override
			protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args)
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
			public <T> Future<T> submitAsync(Callable<T> call) {
				try {
					call.call();
					set.add("async");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return new EmptyFuture<T>();
			}
        	
        });
        
        interpreter.startWithContext(null);
        
        Assert.assertTrue(set.contains("test1"));
        Assert.assertTrue(set.contains("test2"));
        Assert.assertTrue(set.contains("sync"));
        Assert.assertTrue(set.contains("async"));
    }

    public static class TheTest{
        public static String staticField = "staticField";

        public InTest in = new InTest();
        public TheTest() {

        }
        public InTest getTest(){
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

    public static class InTest{
        public InTest2 in = new InTest2();
        public double health = 0.82;
        public boolean hasPermission(String tt){
            return tt.equals("tt");
        }
        public InTest2 getTest(){
            return in;
        }
    }

    public static class InTest2{
        public double health = 5.23;
        public double getHealth(){
            return health;
        }
    }

    public enum TestEnum{
        IMTEST;
    }
    
    public static class EmptyFuture<T> implements Future<T>{

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
