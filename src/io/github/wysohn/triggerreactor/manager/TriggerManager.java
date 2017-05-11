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
package io.github.wysohn.triggerreactor.manager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;

import io.github.wysohn.triggerreactor.core.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.parser.Parser;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;

public abstract class TriggerManager extends Manager implements Listener{
    public TriggerManager(TriggerReactor plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public class Trigger{
        private final String script;

        private final Node root;
        private final Map<String, Executor> executorMap;
        private final Map<String, Object> gvarMap;
        private final TriggerConditionManager condition;

        public Trigger(String script) throws IOException, LexerException, ParserException {
            super();
            this.script = script;

            Charset charset = Charset.forName("UTF-8");

            Lexer lexer = new Lexer(script, charset);
            Parser parser = new Parser(lexer);

            root = parser.parse();
            executorMap = plugin.getExecutorManager();
            gvarMap = plugin.getVariableManager().getGlobalVariableAdapter();
            condition = plugin.getConditionManager();
        }

        public String getScript() {
            return script;
        }

        public void activate(Event e, Map<String, Object> scriptVars) {
            Interpreter interpreter = new Interpreter(root, executorMap, gvarMap, condition);
            interpreter.getVars().putAll(scriptVars);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        interpreter.startWithContext(e);
                        if(interpreter.isCooldown()){
                            Player player = ((PlayerEvent) e).getPlayer();
                            player.sendMessage(ChatColor.RED+"You have to wait for awhile before use this again.");
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                        if(e instanceof PlayerEvent){
                            Player player = ((PlayerEvent) e).getPlayer();
                            player.sendMessage(ChatColor.RED+"Could not execute this trigger.");
                            player.sendMessage(ChatColor.RED+ex.getMessage());
                            player.sendMessage(ChatColor.RED+"If you are administrator, see console for details.");
                        }
                    }
                }
            }).start();
        }
    }

}
