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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;

import io.github.wysohn.triggerreactor.core.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.parser.Parser;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;

public abstract class TriggerManager extends Manager implements Listener{
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TriggerManager(TriggerReactor plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    protected void insertPlayerVariables(Player player, Map<String, Object> varMap) {
        varMap.put("player", player);
        varMap.put("name", player.getName());
        varMap.put("canfly", player.getAllowFlight());
        varMap.put("bedlocation", player.getBedSpawnLocation());
        varMap.put("canpickup", player.getCanPickupItems());
        varMap.put("compasstarget", player.getCompassTarget());
        varMap.put("displayname", player.getDisplayName());
        varMap.put("exp", player.getExp());
        varMap.put("eyeheight", player.getEyeHeight());
        varMap.put("eyeheightignoresneak", player.getEyeHeight(true));
        varMap.put("eyelocation", player.getEyeLocation());
        varMap.put("firetick", player.getFireTicks());
        varMap.put("worldname", player.getWorld().getName());
        varMap.put("gamemode", player.getGameMode());
    }

    public class Trigger implements Cloneable{
        protected final String script;

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
            if(e instanceof PlayerEvent){
                Player player = ((PlayerEvent) e).getPlayer();
                UUID uuid = player.getUniqueId();

                Long end = cooldowns.get(uuid);
                if(end != null && System.currentTimeMillis() < end){
                    player.sendMessage(ChatColor.GRAY+"Cooldown: "+(end/1000L)+" secs left.");
                    return;
                }
            }

            Interpreter interpreter = new Interpreter(root, executorMap, gvarMap, condition);
            interpreter.getVars().putAll(scriptVars);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        interpreter.startWithContextAndInterrupter(e, new ProcessInterrupter(){
                            @Override
                            public boolean onNodeProcess(Node node) {
                                if(interpreter.isCooldown() && e instanceof PlayerEvent){
                                    Player player = ((PlayerEvent) e).getPlayer();
                                    UUID uuid = player.getUniqueId();
                                    cooldowns.put(uuid, interpreter.getCooldownEnd());
                                }
                                return false;
                            }

                            @Override
                            public boolean onCommand(Object context, String command, Object[] args) {
                                if("CALL".equals(command)){
                                    if(args.length < 1)
                                        throw new RuntimeException("Need parameter [String] or [Location]");

                                    if(args[0] instanceof String){
                                        Trigger trigger = plugin.getNamedTriggerManager().getTriggerForName((String) args[0]);
                                        if(trigger == null)
                                            throw new RuntimeException("No trigger found for Named Trigger "+args[0]);

                                        trigger.activate(e, scriptVars);
                                        return true;
                                    } else {
                                        throw new RuntimeException("Parameter type not match; it should be a String."
                                                + " Make sure to put double quotes, if you provided String literal.");
                                    }
                                }
                                return false;
                            }

                        });
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

        @Override
        public Trigger clone(){
            try {
                Trigger trigger = new Trigger(script);
                return trigger;
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
