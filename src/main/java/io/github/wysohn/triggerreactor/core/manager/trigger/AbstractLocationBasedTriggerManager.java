/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public abstract class AbstractLocationBasedTriggerManager<T extends Trigger> extends AbstractTriggerManager {
    protected Map<SimpleChunkLocation, Map<SimpleLocation, T>> locationTriggers = new ConcurrentHashMap<>();
    private Map<UUID, String> settingLocation = new HashMap<>();

    public AbstractLocationBasedTriggerManager(TriggerReactor plugin, SelfReference ref, File tirggerFolder) {
        super(plugin, ref, tirggerFolder);
    }

    @Override
    public void reload(){
        locationTriggers.clear();

        loadTriggers(folder.listFiles());
    }

    private void loadTriggers(File[] target) {
        for(File file : target){
            if(file.isDirectory()) {
                loadTriggers(file.listFiles());
            }else {
                if(!isTriggerFile(file))
                    continue;

                String triggerName = extractName(file);

                SimpleLocation sloc = null;
                try{
                    sloc = stringToSloc(triggerName);
                }catch(Exception e){
                    e.printStackTrace();
                    continue;
                }

                String script = null;
                try {
                    script = FileUtil.readFromFile(file);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    continue;
                }

                T trigger = null;
                try {
                    trigger = constructTrigger(sloc.toString(), script);
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                    continue;
                }

                if(sloc != null && trigger != null){
                    SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

                    Map<SimpleLocation, T> triggerMap = locationTriggers.get(scloc);
                    if(!locationTriggers.containsKey(scloc)){
                        triggerMap = new ConcurrentHashMap<>();
                        locationTriggers.put(scloc, triggerMap);
                    }

                    if (triggerMap.containsKey(sloc)) {
                        Trigger previous = triggerMap.get(sloc);
                        plugin.getLogger().warning("Found a duplicating "+trigger.getClass().getSimpleName());
                        plugin.getLogger().warning("Existing: "+previous.file.getAbsolutePath());
                        plugin.getLogger().warning("Skipped: "+trigger.file.getAbsolutePath());
                    } else {
                        triggerMap.put(sloc, trigger);
                    }
                }
            }
        }
    }

    @Override
    public void saveAll(){
        for(Entry<SimpleChunkLocation, Map<SimpleLocation, T>> chunkEntry : locationTriggers.entrySet()){
            SimpleChunkLocation scloc = chunkEntry.getKey();
            Map<SimpleLocation, T> slocMap = chunkEntry.getValue();

            Set<SimpleLocation> failed = new HashSet<>();

            for(Entry<SimpleLocation, T> entry : slocMap.entrySet()){
                SimpleLocation sloc = entry.getKey();
                T trigger = entry.getValue();

                String fileName = slocToString(sloc);
                String script = trigger.getScript();

                File file = getTriggerFile(folder, fileName, true);
                try{
                    FileUtil.writeToFile(file, script);
                }catch(Exception e){
                    e.printStackTrace();
                    plugin.getLogger().severe("Could not save a trigger at "+sloc);
                    failed.add(sloc);
                }
            }

            for(SimpleLocation sloc : failed){
                slocMap.remove(sloc);
            }
        }
    }

    @Override
    protected Collection<? extends Trigger> getAllTriggers() {
        // Think about this later
        return Collections.emptySet();
    }

    protected String slocToString(SimpleLocation sloc) {
        return sloc.getWorld()+"@"+sloc.getX()+","+sloc.getY()+","+sloc.getZ();
    }

    protected SimpleLocation stringToSloc(String str) {
        String[] wsplit = str.split("@");
        String world = wsplit[0];
        String[] lsplit = wsplit[1].split(",");
        int x = Integer.parseInt(lsplit[0]);
        int y = Integer.parseInt(lsplit[1]);
        int z = Integer.parseInt(lsplit[2]);
        return new SimpleLocation(world, x, y, z);
    }

    protected abstract T constructTrigger(String slocString, String script) throws TriggerInitFailedException;

    protected T constructTrigger(SimpleLocation sloc, String script) throws TriggerInitFailedException{
        return constructTrigger(sloc.toString(), script);
    }

    protected abstract String getTriggerTypeName();

    protected T getTriggerForLocation(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        if(!locationTriggers.containsKey(scloc))
            return null;

        Map<SimpleLocation, T> triggerMap = locationTriggers.get(scloc);
        if(!triggerMap.containsKey(sloc))
            return null;

        T trigger = triggerMap.get(sloc);
        return trigger;
    }

    protected void setTriggerForLocation(SimpleLocation sloc, T trigger) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> triggerMap = locationTriggers.get(scloc);
        if(!locationTriggers.containsKey(scloc)){
            triggerMap = new ConcurrentHashMap<>();
            locationTriggers.put(scloc, triggerMap);
        }

        triggerMap.put(sloc, trigger);

        plugin.saveAsynchronously(this);
    }

    protected T removeTriggerForLocation(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> triggerMap = locationTriggers.get(scloc);
        if(!locationTriggers.containsKey(scloc)){
            return null;
        }

        T result = triggerMap.remove(sloc);

        deleteInfo(result);

        plugin.saveAsynchronously(this);
        return result;
    }

    protected abstract void showTriggerInfo(ICommandSender sender, SimpleLocation sloc);

    private final Map<UUID, ClipBoard> clipboard = new HashMap<>();

    protected boolean isLocationSetting(IPlayer player) {
        return settingLocation.containsKey(player.getUniqueId());
    }

    public boolean startLocationSet(IPlayer player, String script) {
        if(settingLocation.containsKey(player.getUniqueId()))
            return false;

        settingLocation.put(player.getUniqueId(), script);

        return true;
    }

    protected boolean stopLocationSet(IPlayer player) {
        if(!settingLocation.containsKey(player.getUniqueId()))
            return false;

        settingLocation.remove(player.getUniqueId());

        return true;
    }

    protected String getSettingLocationScript(IPlayer player) {
        return settingLocation.get(player.getUniqueId());
    }

    private static class ClipBoard{
            final BoardType type;
            final SimpleLocation location;
            public ClipBoard(BoardType type, SimpleLocation location) {
                this.type = type;
                this.location = location;
            }

            enum BoardType{
                CUT, COPY;
            }
        }

    protected void onItemSwap(IPlayer player) {
        if(player.getUniqueId() == null)
            return;

        clipboard.remove(player.getUniqueId());
    }

    /**
     *
     * @param player
     * @param sloc
     * @return true if cut ready; false if no trigger found at the location
     */
    protected boolean cutTrigger(IPlayer player, SimpleLocation sloc) {
        T trigger = getTriggerForLocation(sloc);
        if(trigger == null){
            return false;
        }

        clipboard.put(player.getUniqueId(), new ClipBoard(ClipBoard.BoardType.CUT, sloc));
        return true;
    }



    /**
     *
     * @param player
     * @param sloc
     * @return true if copy ready; false if no trigger found at the location
     */
    protected boolean copyTrigger(IPlayer player, SimpleLocation sloc) {
        T trigger = getTriggerForLocation(sloc);
        if(trigger == null){
            return false;
        }

        clipboard.put(player.getUniqueId(), new ClipBoard(ClipBoard.BoardType.COPY, sloc));
        return true;
    }



    /**
     *
     * @param player
     * @param sloc
     * @return true if pasted; false if nothing in the clipboard
     */
    protected boolean pasteTrigger(IPlayer player, SimpleLocation sloc) {
        ClipBoard board = clipboard.get(player.getUniqueId());
        if(board == null)
            return false;

        SimpleLocation from = board.location;
        if(from == null){
            return false;
        }

        T trigger = getTriggerForLocation(from);
        if(trigger == null){
            return false;
        }

        try{
            if(board.type == ClipBoard.BoardType.CUT)
                trigger = removeTriggerForLocation(board.location);

            T copy = (T) trigger.clone();
            copy.setTriggerName(sloc.toString());

            setTriggerForLocation(sloc, copy);
        }catch(Exception e){
            e.printStackTrace();
            //put it back if failed
            if(board.type == ClipBoard.BoardType.CUT && trigger != null){
                setTriggerForLocation(board.location, (T) trigger.clone());
            }
        }

        return true;
    }

    public Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(SimpleChunkLocation scloc) {
        Set<Map.Entry<SimpleLocation, Trigger>> triggers = new HashSet<>();
        if(!locationTriggers.containsKey(scloc))
            return triggers;

        for(Entry<SimpleChunkLocation, Map<SimpleLocation, T>> entry : locationTriggers.entrySet()){
            for(Entry<SimpleLocation, T> entryIn : entry.getValue().entrySet()){
                triggers.add(new SimpleEntry<SimpleLocation, Trigger>(entryIn.getKey(), entryIn.getValue()));
            }
        }

        return triggers;
    }

    public static class WalkTrigger extends Trigger{

        public WalkTrigger(String name, File file, String script) throws TriggerInitFailedException {
            super(name, file, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new WalkTrigger(triggerName, file, getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class ClickTrigger extends Trigger{
        private ClickHandler handler;

        public ClickTrigger(String name, File file, String script, ClickHandler handler) throws TriggerInitFailedException {
            super(name, file, script);
            this.handler = handler;

            init();
        }

        @Override
        public boolean activate(Object e, Map<String, Object> scriptVars) {
            if(!handler.allow(e))
                return true;

            return super.activate(e, scriptVars);
        }

        @Override
        public Trigger clone(){
            try {
                //TODO: using same handler will be safe?
                Trigger trigger = new ClickTrigger(triggerName, file, script, handler);
                return trigger;
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface ClickHandler{
        /**
         * Check if click is allowed for this context. If it were Bukkit API, it will be PlayerInteractEvent.
         * @param context the context
         * @return true if allowed; false if not (the click will be ignored in this case)
         */
        boolean allow(Object context);
    }
}