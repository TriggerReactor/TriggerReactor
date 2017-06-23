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
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryInteractEvent;
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
import io.github.wysohn.triggerreactor.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.faction.FactionsSupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.theguild.TheGuildSupport;
import io.github.wysohn.triggerreactor.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public abstract class TriggerManager extends Manager{
    protected final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private static final CommonFunctions common = new CommonFunctions();

    private static Map<String, APISupport> sharedVars = new HashMap<>();

    public TriggerManager(TriggerReactor plugin) {
        super(plugin);

        addSharedVars("vault", VaultSupport.class);
        addSharedVars("mcmmo", McMmoSupport.class);
        addSharedVars("theguild", TheGuildSupport.class);
        addSharedVars("placeholder", PlaceHolderSupport.class);
        addSharedVars("factions", FactionsSupport.class);
        addSharedVars("coreprotect", CoreprotectSupport.class);
    }

    public void addSharedVars(String varName, Class<? extends APISupport> clazz){
        if(!sharedVars.containsKey(varName)){
            Constructor con = null;
            try {
                con = clazz.getConstructor(TriggerReactor.class);
            } catch (NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }

            boolean initSuccess = true;
            APISupport api = null;
            try {
                api = (APISupport) con.newInstance(plugin);
                api.init();
            } catch(APISupportException e){
                initSuccess = false;
            } catch (Exception e) {
                initSuccess = false;
                e.printStackTrace();
            } finally {
                if(api != null && initSuccess)
                    sharedVars.put(varName, api);
            }
        }
    }

    protected void insertPlayerVariables(Player player, Map<String, Object> varMap) {
        varMap.put("player", player);
/*        varMap.put("name", player.getName());
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
        varMap.put("worldname", player.getWorld().getName());*/
    }

    public abstract class Trigger implements Cloneable{
        protected final String triggerName;
        protected final String script;

        protected Node root;
        protected Map<String, Executor> executorMap;
        protected Map<String, Object> gvarMap;
        protected TriggerConditionManager condition;

        private boolean sync = false;

        /**
         * This constructor <b>does not</b> initialize the fields. It is essential to call init() method
         * in order to make the Trigger work properly. If you want to create a Trigger with customized
         * behavior, it's not necessary to call init() but need to override initInterpreter(),
         * startInterpretation(), or activate() method as your need
         * @param script
         */
        public Trigger(String triggerName, String script)  {
            super();

            this.triggerName = triggerName;
            this.script = script;
        }

        public String getTriggerName() {
            return triggerName;
        }

        /**
         *
         * @throws IOException low level exception from Lexer
         * @throws LexerException throws if lexical analysis failed
         * @throws ParserException throws if parsing failed
         */
        protected void init() throws IOException, LexerException, ParserException{
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

        public boolean isSync() {
            return sync;
        }

        public void setSync(boolean sync) {
            this.sync = sync;
        }

        /**
         * Start this trigger. Variables in scriptVars may be overridden if it has same name as
         *  the name of fields of Event class.
         * @param e the Event associated with this Trigger
         * @param scriptVars the temporary local variables
         * @return true if activated; false if on cooldown
         */
        public boolean activate(Event e, Map<String, Object> scriptVars) {
            if(checkCooldown(e)){
                return false;
            }

            scriptVars.put("event", e);
            scriptVars.putAll(ReflectionUtil.extractVariablesWithEnumAsString(e));

            Interpreter interpreter = initInterpreter(scriptVars);

            startInterpretation(e, scriptVars, interpreter, isSync());
            return true;
        }

        /**
         *
         * @param e
         * @return true if cooldown; false if not cooldown or 'e' is not a compatible type
         */
        protected boolean checkCooldown(Event e) {
            UUID uuid = null;

            if(e instanceof PlayerEvent){
                Player player = ((PlayerEvent) e).getPlayer();
                uuid = player.getUniqueId();
            }else if(e instanceof InventoryInteractEvent){
                uuid = ((InventoryInteractEvent) e).getWhoClicked().getUniqueId();
            }

            if(uuid != null){
                Long end = cooldowns.get(uuid);
                if(end != null && System.currentTimeMillis() < end){
                    return true;
                }

                return false;
            }

            return false;
        }

        protected Interpreter initInterpreter(Map<String, Object> scriptVars) {
            Interpreter interpreter = new Interpreter(root, executorMap, gvarMap, scriptVars, common, condition);
            interpreter.setSync(isSync());

            interpreter.getVars().putAll(scriptVars);
            interpreter.getVars().putAll(sharedVars);

            return interpreter;
        }

        /**
         * Start interpreting the code.
         *
         * @param e
         *            The Event associated with this Trigger
         * @param scriptVars
         *            temporary variables
         * @param interpreter
         *            The Interpreter
         * @param sync
         *            set it true will make this method run in the thread that
         *            has called this method. This is useful when this trigger has to cancel an Event;
         *            set it to false will let it run in separate thread. This is more efficient if you
         *            only need to read data from Event and never interact with it.
         */
        protected void startInterpretation(Event e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            if(sync){
                start(e, scriptVars, interpreter, sync);
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        start(e, scriptVars, interpreter, sync);
                    }
                }).start();
            }
        }

        /**
         * The actual execution part. The Trigger can be sync/async depends on which thread invokes this method.
         * @param e
         * @param scriptVars
         * @param interpreter
         * @param sync
         */
        protected void start(Event e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
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
                                throw new RuntimeException("Need parameter [String]");

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
                        } else if("CANCELEVENT".equals(command)) {
                            if(!sync)
                                throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                            if(context instanceof Cancellable){
                                ((Cancellable) context).setCancelled(true);
                                return true;
                            } else {
                                throw new RuntimeException(context+" is not a Cancellable event!");
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

        @Override
        public abstract Trigger clone();
    }
}
