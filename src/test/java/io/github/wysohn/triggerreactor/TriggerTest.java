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
package io.github.wysohn.triggerreactor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Test;
import org.mockito.Mockito;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import junit.framework.Assert;

public class TriggerTest {
    @Test
    public void testRandom() throws Exception{
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
            public Integer execute(boolean sync, Object context, Object... args) {
                String value = String.valueOf(args[0]);
                Assert.assertTrue("0".equals(value) || "1".equals(value)||"2".equals(value));
                return null;
            }};
        executorMap.put("MESSAGE", mockExecutor);

        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));

        interpreter.getVars().put("common", new CommonFunctions(null));

        interpreter.startWithContext(null);
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
            public Integer execute(boolean sync, Object context, Object... args) {
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(),new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));
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
            public Integer execute(boolean sync, Object context, Object... args) {
                Object[] arr = (Object[]) args[0];
                Assert.assertEquals("beh0.82",  arr[0]);
                Assert.assertEquals("0.82beh",  arr[1]);
                Assert.assertEquals("beh11",  arr[2]);
                Assert.assertEquals("beh2",  arr[3]);
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));
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
                + "    IF takeItem(player, 265, 1)\n"
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
            public Integer execute(boolean sync, Object context, Object... args) {
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
            public Integer execute(boolean sync, Object context, Object... args) {
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
            public Integer execute(boolean sync, Object context, Object... args) {
                if(takeItem)
                    Assert.assertEquals("econ add "+playerName+" "+ecoValue, args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));

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
            public Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals(12.54, args[0]);
                return null;
            }
        });
        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), map, new HashMap<>(), new CommonFunctions(null));

        interpreter.getVars().put("text", "someplayername");
        interpreter.startWithContext(null);

        Assert.assertTrue(map.containsKey("someplayername.something"));
        Assert.assertEquals(12.54, map.get("someplayername.something"));
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
            public Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), map, new HashMap<>(), new CommonFunctions(null));

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
            public Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals("arg1, arg2", args[0]);
                return null;
            }
        });
        CommonFunctions mockFunctions = mock(CommonFunctions.class);
        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), map, new HashMap<>(), new CommonFunctions(null));

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
            public Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals(mockPlayer[index].getName(), args[0]);
                index++;
                return null;
            }
        });

        Collection players = new ArrayList<Player>(){{add(mockPlayer[0]); add(mockPlayer[1]);}};
        when(mockFunctions.getPlayers()).thenReturn(players);

        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), map, new HashMap<>(), mockFunctions);

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
            public Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals(index++, args[0]);
                return null;
            }
        });

        Collection players = new ArrayList<Player>(){{add(mockPlayer1); add(mockPlayer2);}};
        when(mockFunctions.getPlayers()).thenReturn(players);

        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), map, new HashMap<>(), mockFunctions);

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
            public Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals("Not valid in here.", String.valueOf(args[0]));
                return null;
            }
        });
        executorMap.put("GUI", new Executor(){
            @Override
            protected Integer execute(boolean sync, Object context, Object... args) {
                Assert.assertEquals("menu", String.valueOf(args[0]));
                return null;
            }
        });

        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter;

        when(mockFunctions.currentArea(Mockito.any(Player.class))).thenReturn("tutorialArea");
        interpreter = new Interpreter(root, executorMap, new HashMap<>(), map, new HashMap<>(), mockFunctions);
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

        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));

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
                protected Integer execute(boolean sync, Object context, Object... args) throws Exception {
                    Assert.assertEquals("work", args[0]);
                    return null;
                }

            });
            put("TEST2", new Executor() {

                @Override
                protected Integer execute(boolean sync, Object context, Object... args) throws Exception {
                    Assert.assertEquals("work2", args[0]);
                    return null;
                }

            });
        }};

        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));
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

        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));

        interpreter.startWithContext(null);

        Assert.assertEquals(3, interpreter.getVars().get("number"));
    }

    @Test
    public void testEnumParse() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "result = parseEnum(\"io.github.wysohn.triggerreactor.TriggerTest$TestEnum\", value);";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();

        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<>(), new HashMap<String, Object>(), new HashMap<String, Object>() {{
            put("value", "IMTEST");
        }}, new CommonFunctions(null));

        interpreter.startWithContext(null);

        Assert.assertEquals(TestEnum.IMTEST, interpreter.getVars().get("result"));
    }

    @Test
    public void testPlaceholder() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = "x = 100;"
                + "returnvalue = $test:0:x:5<3:true&&false:\"hoho\";"
                + "#MESSAGE $playername returnvalue;";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {

            @Override
            protected Integer execute(boolean sync, Object context, Object... args) throws Exception {
                Assert.assertEquals("testplayer", args[0]);
                Assert.assertEquals("testwithargs", args[1]);
                return null;
            }

        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        placeholderMap.put("playername", new Placeholder() {

            @Override
            public Object parse(Object context, Object... args) throws Exception {
                return "testplayer";
            }

        });
        placeholderMap.put("test", new Placeholder() {

            @Override
            public Object parse(Object context, Object... args) throws Exception {
                Assert.assertEquals(0, args[0]);
                Assert.assertEquals(100, args[1]);
                Assert.assertEquals(false, args[2]);
                Assert.assertEquals(false, args[3]);
                Assert.assertEquals("hoho", args[4]);
                return "testwithargs";
            }

        });

        Interpreter interpreter = new Interpreter(root, executorMap, placeholderMap, new HashMap<String, Object>(), new HashMap<>(), new CommonFunctions(null));

        interpreter.startWithContext(null);

        Assert.assertEquals("testwithargs", interpreter.getVars().get("returnvalue"));
    }

    private static class TheTest{
        public InTest in = new InTest();
        public InTest getTest(){
            return in;
        }
    }

    private static class InTest{
        public InTest2 in = new InTest2();
        public double health = 0.82;
        public boolean hasPermission(String tt){
            return tt.equals("tt");
        }
        public InTest2 getTest(){
            return in;
        }
    }

    private static class InTest2{
        public double health = 5.23;
        public double getHealth(){
            return health;
        }
    }

    public enum TestEnum{
        IMTEST;
    }
}
