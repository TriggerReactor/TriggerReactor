package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

public abstract class AbstractLocationBasedTriggerManager<T extends Trigger> extends AbstractTriggerManager {
    protected Map<SimpleChunkLocation, Map<SimpleLocation, T>> locationTriggers = new ConcurrentHashMap<>();
    private Map<UUID, String> settingLocation = new HashMap<>();

    public AbstractLocationBasedTriggerManager(TriggerReactor plugin, SelfReference ref,
            Map<String, Class<? extends AbstractAPISupport>> vars) {
        super(plugin, ref, vars);
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

    public static class WalkTrigger extends TriggerManager.Trigger{

        public WalkTrigger(String name, String script) throws TriggerInitFailedException {
            super(name, script);

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new WalkTrigger(triggerName, getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class ClickTrigger extends TriggerManager.Trigger{
        private ClickHandler handler;

        public ClickTrigger(String name, String script, ClickHandler handler) throws TriggerInitFailedException {
            super(name, script);
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
                Trigger trigger = new ClickTrigger(triggerName, script, handler);
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