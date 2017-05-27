/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Test;
import org.mockito.Mockito;

import io.github.wysohn.triggerreactor.core.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.parser.Parser;
import io.github.wysohn.triggerreactor.manager.trigger.share.CommonFunctions;
import junit.framework.Assert;

public class TriggerTest {
    @Test
    public void testRandom() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "rand = common.random(3)\n"
                + "IF rand == 0\n"
                + "#MESSAGE 0\n"
                + "ENDIF\n"
                + "IF rand == 1\n"
                + "#MESSAGE 1\n"
                + "ENDIF\n"
                + "IF rand == 2\n"
                + "#MESSAGE 2\n"
                + "ENDIF\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        Executor mockExecutor = new Executor(){
            @Override
            public Integer execute(Object context, Object... args) {
                String value = String.valueOf(args[0]);
                Assert.assertTrue("0".equals(value) || "1".equals(value)||"2".equals(value));
                return null;
            }};
        executorMap.put("MESSAGE", mockExecutor);

        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<String, Object>(), null);
        interpreter.getVars().put("common", new CommonFunctions());

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
            public Integer execute(Object context, Object... args) {
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<String, Object>(), null);
        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);

        Assert.assertEquals(12.43, reference.getTest().in.getHealth(), 0.001);
    }

    @Test
    public void testStringAppend() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE \"beh\"+player.in.health";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            @Override
            public Integer execute(Object context, Object... args) {
                Assert.assertEquals("beh0.82", (String) args[0]);
                return null;
            }
        });
        TheTest reference = new TheTest();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<String, Object>(), null);
        interpreter.getVars().put("player", reference);
        interpreter.getVars().put("text", "hello");

        interpreter.startWithContext(null);
    }

    boolean takeItem = false;
    @Test
    public void testSimpleTrigger() throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "IF command == \"iron\"\n"
                + "    IF common.takeItem(player, 265, 1)\n"
                + "        #SOUND LEVEL_UP 1.0 2.0 player.getLocation()\n"
                + "        #CMDCON \"econ add \"+player.getName()+\" 10\"\n"
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
            public Integer execute(Object context, Object... args) {
                if(!takeItem){
                    Assert.assertEquals("not enough iron", (String) args[0]);
                }else{
                    Assert.assertEquals("Sold!", (String) args[0]);
                }
                return null;
            }
        });
        Location mockLocation = mock(Location.class);
        executorMap.put("SOUND", new Executor(){
            @Override
            public Integer execute(Object context, Object... args) {
                Assert.assertEquals("LEVEL_UP", args[0]);
                Assert.assertEquals(1.0, args[1]);
                Assert.assertEquals(2.0, args[2]);
                Assert.assertEquals(mockLocation, args[3]);
                return null;
            }
        });
        executorMap.put("CMDCON", new Executor(){
            @Override
            public Integer execute(Object context, Object... args) {
                System.out.println("to console: "+args[0]);
                return null;
            }
        });
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<String, Object>(), null);

        Player mockPlayer = mock(Player.class);
        PlayerInventory mockInven = mock(PlayerInventory.class);

        when(mockPlayer.getInventory()).thenReturn(mockInven);
        when(mockPlayer.getLocation()).thenReturn(mockLocation);
        when(mockPlayer.getName()).thenReturn("Test player");

        interpreter.getVars().put("player", mockPlayer);
        interpreter.getVars().put("text", "hello");
        interpreter.getVars().put("command", "철괴");

        takeItem = false;
        when(mockInven.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt())).thenReturn(takeItem);
        System.out.println("takeItem: "+takeItem+"\n");
        interpreter.startWithContext(null);

        System.out.println();

        takeItem = true;
        when(mockInven.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt())).thenReturn(takeItem);
        System.out.println("takeItem: "+takeItem+"\n");
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
            public Integer execute(Object context, Object... args) {
                Assert.assertEquals(12.54, args[0]);
                return null;
            }
        });
        Map<String, Object> map = new HashMap<String, Object>();
        Interpreter interpreter = new Interpreter(root, executorMap, map, null);

        interpreter.getVars().put("text", "someplayername");
        interpreter.startWithContext(null);

        Assert.assertTrue(map.containsKey("someplayername.something"));
        Assert.assertEquals(12.54, map.get("someplayername.something"));
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

}
