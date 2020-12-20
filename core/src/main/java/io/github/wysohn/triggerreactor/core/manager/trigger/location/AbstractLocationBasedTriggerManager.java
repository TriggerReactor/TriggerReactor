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
package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLocationBasedTriggerManager<T extends Trigger> extends AbstractTaggedTriggerManager<T> {
    protected final Map<SimpleChunkLocation, Map<SimpleLocation, T>> chunkMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> settingLocation = new HashMap<>();

    private final Map<UUID, ClipBoard> clipboard = new HashMap<>();

    public AbstractLocationBasedTriggerManager(TriggerReactorCore plugin, File folder, ITriggerLoader<T> loader) {
        super(plugin, folder, loader);
    }

    @Override
    public void reload() {
        super.reload();

        chunkMap.clear();

        for (T trigger : getAllTriggers()) {
            SimpleLocation sloc = null;
            try {
                sloc = SimpleLocation.valueOf(trigger.getInfo().getTriggerName());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

            Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
            if (!chunkMap.containsKey(scloc)) {
                locationMap = new ConcurrentHashMap<>();
                chunkMap.put(scloc, locationMap);
            }

            if (locationMap.containsKey(sloc)) {
                Trigger previous = locationMap.get(sloc);
                plugin.getLogger().warning("Found a duplicating " + trigger.getClass().getSimpleName());
                plugin.getLogger().warning("Existing: " + previous.getInfo().getSourceCodeFile().getAbsolutePath());
                plugin.getLogger().warning("Skipped: " + trigger.getInfo().getSourceCodeFile().getAbsolutePath());
            } else {
                locationMap.put(sloc, trigger);
            }
        }
    }

    protected abstract String getTriggerTypeName();

    protected T getTriggerForLocation(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        if (!chunkMap.containsKey(scloc))
            return null;

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!locationMap.containsKey(sloc))
            return null;

        return locationMap.get(sloc);
    }

    protected void setLocationCache(SimpleLocation sloc, T trigger) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!chunkMap.containsKey(scloc)) {
            locationMap = new ConcurrentHashMap<>();
            chunkMap.put(scloc, locationMap);
        }

        locationMap.put(sloc, trigger);
        put(scloc.toString(), trigger);

        plugin.saveAsynchronously(this);
    }

    protected T removeLocationCache(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!chunkMap.containsKey(scloc)) {
            return null;
        }

        T result = locationMap.remove(sloc);
        remove(sloc.toString());

        plugin.saveAsynchronously(this);
        return result;
    }

    protected abstract T newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException;

    protected abstract void showTriggerInfo(ICommandSender sender, SimpleLocation sloc);

    protected boolean isLocationSetting(IPlayer player) {
        return settingLocation.containsKey(player.getUniqueId());
    }

    public boolean startLocationSet(IPlayer player, String script) {
        if (settingLocation.containsKey(player.getUniqueId()))
            return false;

        settingLocation.put(player.getUniqueId(), script);

        return true;
    }

    protected boolean stopLocationSet(IPlayer player) {
        if (!settingLocation.containsKey(player.getUniqueId()))
            return false;

        settingLocation.remove(player.getUniqueId());

        return true;
    }

    protected String getSettingLocationScript(IPlayer player) {
        return settingLocation.get(player.getUniqueId());
    }

    protected void onItemSwap(IPlayer player) {
        if (player.getUniqueId() == null)
            return;

        clipboard.remove(player.getUniqueId());
    }

    /**
     * @param player
     * @param sloc
     * @return true if cut ready; false if no trigger found at the location
     */
    protected boolean cutTrigger(IPlayer player, SimpleLocation sloc) {
        T trigger = getTriggerForLocation(sloc);
        if (trigger == null) {
            return false;
        }

        clipboard.put(player.getUniqueId(), new ClipBoard(ClipBoard.BoardType.CUT, sloc));
        return true;
    }


    /**
     * @param player
     * @param sloc
     * @return true if copy ready; false if no trigger found at the location
     */
    protected boolean copyTrigger(IPlayer player, SimpleLocation sloc) {
        T trigger = getTriggerForLocation(sloc);
        if (trigger == null) {
            return false;
        }

        clipboard.put(player.getUniqueId(), new ClipBoard(ClipBoard.BoardType.COPY, sloc));
        return true;
    }


    /**
     * @param player
     * @param sloc
     * @return true if pasted; false if nothing in the clipboard
     */
    protected boolean pasteTrigger(IPlayer player, SimpleLocation sloc) {
        ClipBoard board = clipboard.get(player.getUniqueId());
        if (board == null)
            return false;

        SimpleLocation from = board.location;
        if (from == null) {
            return false;
        }

        T trigger = getTriggerForLocation(from);
        if (trigger == null) {
            return false;
        }

        try {
            if (board.type == ClipBoard.BoardType.CUT)
                trigger = removeLocationCache(board.location);

            T copy = (T) trigger.clone();

            setLocationCache(sloc, copy);
        } catch (Exception e) {
            e.printStackTrace();
            //put it back if failed
            if (board.type == ClipBoard.BoardType.CUT && trigger != null) {
                setLocationCache(board.location, (T) trigger.clone());
            }
        }

        return true;
    }

    public Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(SimpleChunkLocation scloc) {
        Set<Map.Entry<SimpleLocation, Trigger>> triggers = new HashSet<>();
        if (!chunkMap.containsKey(scloc))
            return triggers;

        for (Entry<SimpleChunkLocation, Map<SimpleLocation, T>> entry : chunkMap.entrySet()) {
            for (Entry<SimpleLocation, T> entryIn : entry.getValue().entrySet()) {
                triggers.add(new SimpleEntry<SimpleLocation, Trigger>(entryIn.getKey(), entryIn.getValue()));
            }
        }

        return triggers;
    }

    private static class ClipBoard {
        final BoardType type;
        final SimpleLocation location;

        public ClipBoard(BoardType type, SimpleLocation location) {
            this.type = type;
            this.location = location;
        }

        enum BoardType {
            CUT, COPY
        }
    }

    public static class WalkTrigger extends Trigger {
        public WalkTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
            super(info, script);

            init();

        }

        @Override
        public Trigger clone() {
            try {
                return new WalkTrigger(info, script);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class ClickTrigger extends Trigger {
        private final ClickHandler handler;

        public ClickTrigger(TriggerInfo info, String script, ClickHandler handler) throws TriggerInitFailedException {
            super(info, script);
            this.handler = handler;

            init();
        }

        @Override
        public boolean activate(Object e, Map<String, Object> scriptVars) {
            if (!handler.allow(e))
                return true;

            return super.activate(e, scriptVars);
        }

        @Override
        public Trigger clone() {
            try {
                //TODO: using same handler will be safe?
                return new ClickTrigger(info, script, handler);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface ClickHandler {
        /**
         * Check if click is allowed for this context. If it were Bukkit API, it will be PlayerInteractEvent.
         *
         * @param context the context
         * @return true if allowed; false if not (the click will be ignored in this case)
         */
        boolean allow(Object context);
    }
}