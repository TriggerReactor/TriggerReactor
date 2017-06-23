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
package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class RepeatingTriggerManager extends TriggerManager {
    private static final String TRIGGER = "trigger";

    private final Map<String, RepeatingTrigger> repeatTriggers = new ConcurrentHashMap<>();

    private final Map<String, Thread> runningThreads = new ConcurrentHashMap<>();

    private File folder;
    public RepeatingTriggerManager(TriggerReactor plugin) {
        super(plugin);

        this.folder = plugin.getDataFolder();
        if(!this.folder.exists()){
            this.folder.mkdirs();
        }
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    /**
     * Get Repeating Trigger with specified name.
     * @param triggerName name of trigger
     * @return Repeating Trigger if found; null if not found.
     */
    public RepeatingTrigger getTrigger(String triggerName){
        return repeatTriggers.get(triggerName);
    }

    /**
     *
     * Create trigger.
     * @param triggerName name of the trigger.
     * @param script the code.
     * @param interval interval in milliseconds.
     * @return true on success; false if already exists.
     * @throws IOException See {@link Trigger#init()}
     * @throws LexerException See {@link Trigger#init()}
     * @throws ParserException See {@link Trigger#init()}
     */
    public boolean createTrigger(String triggerName, String script, long interval) throws IOException, LexerException, ParserException{
        if(getTrigger(triggerName) != null){
            return false;
        }

        RepeatingTrigger trigger = new RepeatingTrigger(triggerName, script, interval);
        repeatTriggers.put(triggerName, trigger);

        Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
        yaml.set("Interval", interval);
        yaml.set("AutoStart", false);
        yaml.save(new File(folder, triggerName+".yml"));

        FileUtil.writeToFile(new File(folder, triggerName), script);

        return true;
    }

    /**
     * Create trigger. Interval is 1000L by default.
     * @param triggerName name of the trigger.
     * @param script the code.
     * @return true on success; false if already exists.
     * @throws IOException See {@link Trigger#init()}
     * @throws LexerException See {@link Trigger#init()}
     * @throws ParserException See {@link Trigger#init()}
     */
    public boolean createTrigger(String triggerName, String script) throws IOException, LexerException, ParserException{
        return createTrigger(triggerName, script, 1000L);
    }

    /**
     * Completely clean up the Repeating Trigger. This also stops the thread if
     *  one was running already.
     * @param triggerName name of the trigger
     * @return true on success; false if trigger with the name not found.
     */
    public boolean deleteTrigger(String triggerName){
        RepeatingTrigger trigger = repeatTriggers.remove(triggerName);
        if(trigger == null){
            return false;
        }

        //stop the thread if it's running
        if(runningThreads.containsKey(triggerName)){
            this.stopTrigger(triggerName);
        }

        FileUtil.delete(new File(folder, triggerName+".yml"));
        FileUtil.delete(new File(folder, triggerName));

        return true;
    }

    /**
     * Checks whether the specified trigger is running. However, this also can
     *  return false even if the trigger with name 'triggerName' does not exists.
     * @param triggerName
     * @return
     */
    public boolean isRunning(String triggerName){
        return runningThreads.containsKey(triggerName);
    }

    /**
     * Attempts to start the trigger with provided trigger name. Return false if no such trigger
     *  with that name, yet return value of true does not necessarily start the thread; it checks if
     *  the trigger thread is already running, and if it is running, it will skip. So return value
     *  true actually just guarantees that the repeating trigger is running. If you want to check
     *  whether the repeating trigger is running, use {@link #isRunning(String)} instead.
     * @param triggerName name of the repeating trigger.
     * @return true on success; false if trigger not found.
     */
    public boolean startTrigger(String triggerName){
        RepeatingTrigger trigger = repeatTriggers.get(triggerName);
        if(trigger == null){
            return false;
        }

        if(!isRunning(triggerName)){
            Map<String, Object> vars = new HashMap<>();
            vars.put(TRIGGER, "init");
            trigger.activate(EMPTY, vars);

            Thread thread = new Thread(trigger);
            thread.setName("TRG Repeating Trigger -- "+triggerName);
            thread.setPriority(Thread.MIN_PRIORITY + 1);
            thread.start();

            runningThreads.put(triggerName, thread);
        }

        return true;
    }

    /**
     * Check whether the Repeating Trigger is currently paused or not. However, it
     *  can return false when there is no such trigger named 'triggerName.' So it's
     *  a better practice to check it with {@link #getTrigger(String)} to see if
     *  the trigger actually exists.
     * @param triggerName name of the trigger
     * @return true if it's paused; false if not paused (or no trigger)
     */
    public boolean isPaused(String triggerName){
        RepeatingTrigger trigger = repeatTriggers.get(triggerName);
        if(trigger == null){
            return false;
        }

        return trigger.isPaused();
    }

    /**
     * Set trigger to paused/resume state.
     * @param triggerName name of the trigger
     * @param pause state to set
     * @return true if set; false if no trigger with name 'triggerName' found.
     */
    public boolean setPaused(String triggerName, boolean pause){
        RepeatingTrigger trigger = repeatTriggers.get(triggerName);
        if(trigger == null){
            return false;
        }

        trigger.setPaused(pause);
        return true;
    }

    /**
     * Attempts to stop the trigger.
     * @param triggerName name of the repeating trigger.
     * @return true on success; false if no such trigger found with name.
     */
    public boolean stopTrigger(String triggerName){
        Thread thread = runningThreads.remove(triggerName);
        if(thread == null){
            return false;
        }

        thread.interrupt();
        return true;
    }

    public class RepeatingTrigger extends Trigger implements Runnable{
        private final ThrowableHandler throwableHandler = new ThrowableHandler(){
            @Override
            public void onFail(Throwable throwable) {
                throwable.printStackTrace();
                plugin.getLogger().warning("Repeating Trigger ["+triggerName+"] encountered an error!");
                plugin.getLogger().warning(throwable.getMessage());
                plugin.getLogger().warning("If you are an administrator, see console for more details.");
            }
        };

        private long interval = 1000L;
        private Map<String, Object> vars;

        public RepeatingTrigger(String name, String script) throws IOException, LexerException, ParserException {
            super(name, script);

            init();
        }

        public RepeatingTrigger(String name, String script, long interval) throws IOException, LexerException, ParserException {
            this(name, script);

            this.interval = interval;
        }

        /**
         * This should be called at least once on start up so variables can be initialized.
         */
        @Override
        public boolean activate(Event e, Map<String, Object> scriptVars) {
            vars = scriptVars;

            return super.activate(e, scriptVars);
        }

        /**
         * We don't use cooldown for this trigger. Just return false always
         */
        @Override
        protected boolean checkCooldown(Event e) {
            return false;
        }

        @Override
        public Trigger clone() {
            try {
                return new RepeatingTrigger(this.triggerName, this.getScript(), interval);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }

            return null;
        }

        //////////////////////////////////////////////////////////////////////////////////////
        private boolean paused;

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;

            if(!paused){
                synchronized(this){
                    this.notify();
                }
            }
        }

        @Override
        public void run() {
            try{
                vars.put(TRIGGER, "start");
                activate(EMPTY, vars);
            }catch(Exception e){
                throwableHandler.onFail(e);
            }

            try{
                vars.put(TRIGGER, "repeat");
                while(!Thread.interrupted()){
                    synchronized (this) {
                        while (paused && !Thread.interrupted()) {
                            this.wait();
                        }
                    }

                    //we re-use the variables over and over.
                    activate(EMPTY, vars);

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }catch(Exception e){
                throwableHandler.onFail(e);
            }

            try{
                vars.put(TRIGGER, "stop");
                activate(EMPTY, vars);
            }catch(Exception e){
                throwableHandler.onFail(e);
            }
        }

    }

    private interface ThrowableHandler{
        void onFail(Throwable throwable);
    }

    private static class EmptyEvent extends Event{

        @Override
        public HandlerList getHandlers() {
            return null;
        }

    }

    private static final Event EMPTY = new EmptyEvent();
}
