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
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.script.ScriptEditor;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class AbstractLocationBasedTriggerManager<T extends Trigger> extends AbstractTaggedTriggerManager<T> {
    @Inject
    @Named("Permission")
    String permission;
    @Inject
    Logger logger;
    @Inject
    ConfigSourceFactories configSourceFactories;
    @Inject
    ScriptEditManager scriptEditManager;
    @Inject
    IThrowableHandler throwableHandler;
    @Inject
    IWrapper wrapper;
    protected final Map<SimpleChunkLocation, Map<SimpleLocation, T>> chunkMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> settingLocation = new HashMap<>();
    private final Map<UUID, ClipBoard> clipboard = new HashMap<>();

    public AbstractLocationBasedTriggerManager(String folderName) {
        super(folderName);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() {
        super.onReload();

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
                logger.warning("Found a duplicating " + trigger.getClass().getSimpleName());
                logger.warning("Existing: " + previous.getInfo().getSourceCodeFile().getAbsolutePath());
                logger.warning("Skipped: " + trigger.getInfo().getSourceCodeFile().getAbsolutePath());
            } else {
                locationMap.put(sloc, trigger);
            }
        }
    }

    protected abstract String getTriggerTypeName();

    protected abstract T newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException;

    private void handleScriptEdit(IPlayer player, T trigger) {
        scriptEditManager.startEdit(player, trigger.getInfo().getTriggerName(), trigger.getScript(),
                (ScriptEditor.SaveHandler) script -> {
                    try {
                        trigger.setScript(script);
                    } catch (AbstractTriggerManager.TriggerInitFailedException e) {
                        throwableHandler.handleException(player, e);
                    }
                }, false);
    }

    protected void showTriggerInfo(ICommandSender sender, SimpleLocation sloc, Trigger trigger) {
        ValidationUtil.notNull(sender);
        ValidationUtil.notNull(sloc);
        ValidationUtil.notNull(trigger);

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("Trigger: " + getTriggerTypeName());
        sender.sendMessage("Location: " + sloc);
        sender.sendMessage("");
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    protected void showTriggerInfo(ICommandSender sender, SimpleLocation sloc) {
        Trigger trigger = getTriggerForLocation(sloc);
        if (trigger == null) {
            return;
        }

        showTriggerInfo(sender, sloc, trigger);
    }

    private void handleLocationSetting(SimpleLocation clickedLoc, IPlayer player) {
        T trigger = getTriggerForLocation(clickedLoc);
        if (trigger != null) {
            player.sendMessage("&cAnother trigger is set at there!");
            showTriggerInfo(player, clickedLoc);
            return;
        }

        String script = getSettingLocationScript(player);
        if (script == null) {
            player.sendMessage("&cCould not find script... but how?");
            return;
        }

        File file = getTriggerFile(folder, clickedLoc.toString(), true);
        try {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = configSourceFactories.create(folder, name);
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = newTrigger(info, script);
        } catch (TriggerInitFailedException e1) {
            player.sendMessage("&cEncounterd an error!");
            player.sendMessage("&c" + e1.getMessage());
            player.sendMessage("&cIf you are an administrator, check console to see details.");
            e1.printStackTrace();

            stopLocationSet(player);
            return;
        }

        setLocationCache(clickedLoc, trigger);
        showTriggerInfo(player, clickedLoc);
        stopLocationSet(player);
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

    protected String getSettingLocationScript(IPlayer player) {
        return settingLocation.get(player.getUniqueId());
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

    protected boolean isLocationSetting(IPlayer player) {
        return settingLocation.containsKey(player.getUniqueId());
    }

    public void onItemSwap(IPlayer player) {
        if (player.getUniqueId() == null)
            return;

        clipboard.remove(player.getUniqueId());
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

            T copy = (T) trigger.copy();

            setLocationCache(sloc, copy);
        } catch (Exception e) {
            e.printStackTrace();
            //put it back if failed
            if (board.type == ClipBoard.BoardType.CUT && trigger != null) {
                setLocationCache(board.location, (T) trigger.copy());
            }
        }

        return true;
    }

    protected T removeLocationCache(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!chunkMap.containsKey(scloc)) {
            return null;
        }

        T result = locationMap.remove(sloc);
        remove(sloc.toString());

        return result;
    }

    protected void setLocationCache(SimpleLocation sloc, T trigger) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!chunkMap.containsKey(scloc)) {
            locationMap = new ConcurrentHashMap<>();
            chunkMap.put(scloc, locationMap);
        }

        locationMap.put(sloc, trigger);
        put(sloc.toString(), trigger);
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

    /**
     * @param clicked
     * @param player
     * @param type
     * @return true to allow the click event; false to cancel
     */
    public boolean onClick(SimpleLocation clicked, IPlayer player, ClickType type) {
        T trigger = getTriggerForLocation(clicked);
        IItemStack itemInHand = player.getItemInMainHand();

        // no business here unless something in the hand and has permission
        if (itemInHand == null || !player.hasPermission(permission))
            return true;

        // finish the trigger setting
        if (isLocationSetting(player)) {
            handleLocationSetting(clicked, player);
            return true;
        }

        // transition into setting mode if condition is matched
        if (itemInHand.isInspectionTool()) {
            if (trigger != null && type == ClickType.LEFT_CLICK) {
                removeLocationCache(clicked);

                player.sendMessage("&aA trigger has deleted.");
                return false;
            } else if (trigger != null && type == ClickType.RIGHT_CLICK) {
                if (player.isSneaking()) {
                    handleScriptEdit(player, trigger);
                } else {
                    this.showTriggerInfo(player, clicked);
                }
                return false;
            }
        } else if (itemInHand.isCutTool()) {
            if (type == ClickType.LEFT_CLICK) {
                if (pasteTrigger(player, clicked)) {
                    player.sendMessage("&aSuccessfully pasted the trigger!");
                    this.showTriggerInfo(player, clicked);
                    return false;
                }
            } else if (trigger != null && type == ClickType.RIGHT_CLICK) {
                if (cutTrigger(player, clicked)) {
                    player.sendMessage("&aCut Complete!");
                    player.sendMessage("&aNow you can paste it by left click on any block!");
                    return false;
                }
            }
        } else if (itemInHand.isCopyTool()) {
            if (type == ClickType.LEFT_CLICK) {
                if (pasteTrigger(player, clicked)) {
                    player.sendMessage("&aSuccessfully pasted the trigger!");
                    this.showTriggerInfo(player, clicked);
                    return false;
                }
            } else if (type == ClickType.RIGHT_CLICK) {
                if (trigger != null && copyTrigger(player, clicked)) {
                    player.sendMessage("&aCopy Complete!");
                    player.sendMessage("&aNow you can paste it by left click on any block!");
                    return false;
                }
            }
        }

        // otherwise, just normal interaction
        return true;
    }

    public boolean isTrigger(SimpleLocation sloc) {
        return getTriggerForLocation(sloc) != null;
    }

    protected T getTriggerForLocation(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        if (!chunkMap.containsKey(scloc))
            return null;

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!locationMap.containsKey(sloc))
            return null;

        return locationMap.get(sloc);
    }

    public enum ClickType {
        LEFT_CLICK("left"),
        LEFT_CLICK_AIR("left_air"),
        RIGHT_CLICK("right"),
        RIGHT_CLICK_AIR("right_air"),
        UNKNOWN("unknown"),
        ;

        private final String variable;

        ClickType(String variable) {
            this.variable = variable;
        }

        public String getVariable() {
            return variable;
        }
    }

    /**
     * Stateless logic to check if clicking the trigger block (in-game) is allowed.
     */
    public interface ClickHandler {
        /**
         * Check if click is allowed for this event. If it were Bukkit API, it will be PlayerInteractEvent.
         *
         * @param event the event
         * @return true if allowed; false if not (the click will be ignored in this case)
         */
        boolean allow(Object event);
    }

    private static class ClipBoard {
        final BoardType type;
        final SimpleLocation location;

        public ClipBoard(BoardType type, SimpleLocation location) {
            this.type = type;
            this.location = location;
        }

        enum BoardType {
            CUT,
            COPY
        }
    }
}