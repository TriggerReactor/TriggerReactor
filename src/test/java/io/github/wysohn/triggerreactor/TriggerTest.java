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
                Assert.assertEquals("beh0.82", args[0]);
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
                + "        #SOUND LEVEL_UP 1.0 1.0 player.getLocation()\n"
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
                System.out.println(args[0]);
                return null;
            }
        });
        executorMap.put("SOUND", new Executor(){
            @Override
            public Integer execute(Object context, Object... args) {
                System.out.println(args[0]+","+args[1]+","+args[2]+","+args[3]);
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
        Location mockLocation = mock(Location.class);

        when(mockPlayer.getInventory()).thenReturn(mockInven);
        when(mockPlayer.getLocation()).thenReturn(mockLocation);
        when(mockPlayer.getName()).thenReturn("Test player");

        interpreter.getVars().put("player", mockPlayer);
        interpreter.getVars().put("text", "hello");
        interpreter.getVars().put("command", "철괴");

        when(mockInven.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt())).thenReturn(false);
        System.out.println("takeItem: "+false+"\n");
        interpreter.startWithContext(null);

        System.out.println();

        when(mockInven.containsAtLeast(Mockito.any(ItemStack.class), Mockito.anyInt())).thenReturn(true);
        System.out.println("takeItem: "+true+"\n");
        interpreter.startWithContext(null);
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
