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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.wysohn.triggerreactor.bukkit.main.TriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.manager.TriggerManager;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.TimeUtil;

public class RepeatingTriggerManager extends TriggerManager {
    private static final String TRIGGER = "trigger";

    private final Map<String, RepeatingTrigger> repeatTriggers = new ConcurrentHashMap<>();

    private final Map<String, Thread> runningThreads = new ConcurrentHashMap<>();

    private File folder;
    public RepeatingTriggerManager(TriggerReactor plugin) {
        super(plugin);

        this.folder = new File(plugin.getDataFolder(), "RepeatTrigger");
        if(!this.folder.exists()){
            this.folder.mkdirs();
        }

        reload();
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        repeatTriggers.clear();
        for(Entry<String, Thread> entry : runningThreads.entrySet()){
            entry.getValue().interrupt();
        }
        runningThreads.clear();

        for(File file : folder.listFiles(filter)){
            String fileName = file.getName();
            String triggerName = fileName.substring(0, fileName.indexOf('.'));

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            boolean autoStart = yaml.getBoolean("AutoStart", false);
            long interval = yaml.getLong("Interval", 1000L);

            String script = null;
            try {
                script = FileUtil.readFromFile(new File(folder, triggerName));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            RepeatingTrigger trigger = null;
            try {
                trigger = new RepeatingTrigger(triggerName, script, interval);
                //let repeating thread to handle the work, not the newly created thread.
                trigger.setSync(true);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            trigger.setAutoStart(autoStart);
            trigger.setInterval(interval);

            repeatTriggers.put(triggerName, trigger);

            final RepeatingTrigger triggerCopy = trigger;
            //start 1 tick later so other managers can be initialized.
            new BukkitRunnable(){
                @Override
                public void run() {
                    if(triggerCopy.isAutoStart()){
                        startTrigger(triggerName);
                    }
                }}.runTask(plugin);
        }
    }

    @Override
    public void saveAll() {
        for(Entry<String, RepeatingTrigger> entry : repeatTriggers.entrySet()){
            String triggerName = entry.getKey();
            RepeatingTrigger trigger = entry.getValue();

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            yaml.set("AutoStart", trigger.isAutoStart());
            yaml.set("Interval", trigger.getInterval());
            try {
                yaml.save(new File(folder, triggerName+".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileUtil.writeToFile(new File(folder, triggerName), trigger.getScript());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showTriggerInfo(CommandSender sender, RepeatingTrigger trigger) {
        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: "+trigger.getTriggerName());
        sender.sendMessage("Auto Start: " + trigger.isAutoStart());
        sender.sendMessage("Interval: " + TimeUtil.milliSecondsToString(trigger.interval));
        sender.sendMessage("");
        sender.sendMessage("Paused: " + trigger.isPaused());
        sender.sendMessage("Running: "+isRunning(trigger.getTriggerName()));
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
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
        yaml.set("AutoStart", false);
        yaml.set("Interval", interval);
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
        private boolean autoStart = false;
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

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public boolean isAutoStart() {
            return autoStart;
        }

        public void setAutoStart(boolean autoStart) {
            this.autoStart = autoStart;
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
                while(!Thread.interrupted()){
                    synchronized (this) {
                        while (paused && !Thread.interrupted()) {
                            this.wait();
                        }
                    }

                    vars.put(TRIGGER, "repeat");

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
