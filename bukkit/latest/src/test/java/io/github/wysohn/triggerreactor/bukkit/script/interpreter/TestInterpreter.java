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
package io.github.wysohn.triggerreactor.bukkit.script.interpreter;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInterpreter {
    boolean takeItem = false;

    @Test
    public void testSimpleTrigger() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        int ecoValue = 10;
        String text = ""
                + "IF command == \"iron\"\n"
                + "    IF takeItem(player, \"IRON_INGOT\", 1)\n"
                + "        #SOUND \"LEVEL_UP\",1.0,-2.0,player.getLocation()\n"
                + "        #CMDCON \"econ add \"+player.getName()+\" " + ecoValue + "\"\n"
                + "        #MESSAGE \"Sold!\"\n"
                + "    ELSE\n"
                + "        #MESSAGE \"not enough iron.\"\n"
                + "    ENDIF\n"
                + "ENDIF\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor() {
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                if (!takeItem) {
                    Assert.assertEquals("not enough iron.", (String) args[0]);
                } else {
                    Assert.assertEquals("Sold!", (String) args[0]);
                }
                return null;
            }
        });
        Location mockLocation = mock(Location.class);
        executorMap.put("SOUND", new Executor() {
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
        executorMap.put("CMDCON", new Executor() {
            @Override
            public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
                if (takeItem)
                    Assert.assertEquals("econ add " + playerName + " " + ecoValue, args[0]);
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
}
