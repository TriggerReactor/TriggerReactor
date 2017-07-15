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
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public abstract class LocationBasedTriggerManager<T extends Trigger> extends AbstractLocationBasedTriggerManager<T> implements Listener{
    public static final Material INSPECTION_TOOL = Material.BONE;
    public static final Material CUT_TOOL = Material.SHEARS;
    public static final Material COPY_TOOL = Material.PAPER;

    private final File folder;
    public LocationBasedTriggerManager(TriggerReactor plugin, String folderName) {
        super(plugin, new CommonFunctions(plugin), APISupport.getSharedVars());

        File dataFolder = plugin.getDataFolder();
        if(!dataFolder.exists())
            dataFolder.mkdirs();

        folder = new File(dataFolder, folderName);
        if(!folder.exists())
            folder.mkdirs();

        reload();

        check();
    }

    protected boolean oldInteractEvent = false;
    private void check() {
        Class<?> clazz = PlayerInteractEvent.class;
        try {
            clazz.getMethod("getHand");
        } catch (NoSuchMethodException e) {
            oldInteractEvent = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void reload(){
        locationTriggers.clear();

        for(File file : folder.listFiles()){
            if(file.isDirectory())
                continue;

            String fileName = file.getName();

            SimpleLocation sloc = null;
            try{
                sloc = stringToSloc(fileName);
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

                triggerMap.put(sloc, trigger);
            }
        }
    }

    @Override
    public void saveAll(){
        for(Entry<SimpleChunkLocation, Map<SimpleLocation, T>> chunkEntry : locationTriggers.entrySet()){
            SimpleChunkLocation cloc = chunkEntry.getKey();
            Map<SimpleLocation, T> slocMap = chunkEntry.getValue();

            Set<SimpleLocation> failed = new HashSet<>();

            for(Entry<SimpleLocation, T> entry : slocMap.entrySet()){
                SimpleLocation sloc = entry.getKey();
                T trigger = entry.getValue();

                String fileName = slocToString(sloc);
                String script = trigger.getScript();

                File file = new File(folder, fileName);
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

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent e){
        if(!oldInteractEvent && e.getHand() != EquipmentSlot.HAND)
            return;

        Player player = e.getPlayer();

        ItemStack IS = player.getInventory().getItemInHand();
        Block clicked = e.getClickedBlock();
        if(clicked == null)
            return;

        T trigger = getTriggerForLocation(clicked.getLocation());

        if(IS != null
                &&!e.isCancelled()
                && player.hasPermission("triggerreactor.admin")){

            if(IS.getType() == INSPECTION_TOOL){
                if(trigger != null && e.getAction() == Action.LEFT_CLICK_BLOCK){
                    removeTriggerForLocation(clicked.getLocation());

                    player.sendMessage(ChatColor.GREEN+"A trigger has deleted.");
                    e.setCancelled(true);
                }else if(trigger != null && e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if(e.getPlayer().isSneaking()){
                        handleScriptEdit(player, trigger);
                        e.setCancelled(true);
                    }else{
                        this.showTriggerInfo(new BukkitPlayer(player), clicked);
                        e.setCancelled(true);
                    }
                }
            }else if(IS.getType() == CUT_TOOL){
                if(e.getAction() == Action.LEFT_CLICK_BLOCK){
                    if(pasteTrigger(player, clicked.getLocation())){
                        player.sendMessage(ChatColor.GREEN+"Successfully pasted the trigger!");
                        this.showTriggerInfo(new BukkitPlayer(player), clicked);
                        e.setCancelled(true);
                    }
                }else if(trigger != null && e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if(cutTrigger(player, clicked.getLocation())){
                        player.sendMessage(ChatColor.GREEN+"Cut Complete!");
                        player.sendMessage(ChatColor.GREEN+"Now you can paste it by left click on any block!");
                        e.setCancelled(true);
                    }
                }
            }else if(IS.getType() == COPY_TOOL){
                if(e.getAction() == Action.LEFT_CLICK_BLOCK){
                    if(pasteTrigger(player, clicked.getLocation())){
                        player.sendMessage(ChatColor.GREEN+"Successfully pasted the trigger!");
                        this.showTriggerInfo(new BukkitPlayer(player), clicked);
                        e.setCancelled(true);
                    }
                }else if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if(trigger != null && copyTrigger(player, clicked.getLocation())){
                        player.sendMessage(ChatColor.GREEN+"Copy Complete!");
                        player.sendMessage(ChatColor.GREEN+"Now you can paste it by left click on any block!");
                        e.setCancelled(true);
                    }
                }
            }
        }

        if(!e.isCancelled() && isLocationSetting(new BukkitPlayer(player))){
            handleLocationSetting(clicked, player);
            e.setCancelled(true);
        }
    }

    private void handleLocationSetting(Block clicked, Player p){
        IPlayer player = new BukkitPlayer(p);

        Location loc = clicked.getLocation();
        T trigger = getTriggerForLocation(loc);
        if(trigger != null){
            player.sendMessage(ChatColor.RED+"Another trigger is set at there!");
            showTriggerInfo(player, clicked);
            return;
        }

        String script = getSettingLocationScript(player);
        if(script == null){
            player.sendMessage(ChatColor.RED+"Could not find script... but how?");
            return;
        }

        try {
            trigger = constructTrigger(LocationUtil.convertToSimpleLocation(loc), script);
        } catch (TriggerInitFailedException e1) {
            player.sendMessage(ChatColor.RED+"Encounterd an error!");
            player.sendMessage(ChatColor.RED+e1.getMessage());
            player.sendMessage(ChatColor.RED+"If you are an administrator, check console to see details.");
            e1.printStackTrace();

            stopLocationSet(player);
            return;
        }

        setTriggerForLocation(loc, trigger);

        showTriggerInfo(player, clicked);

        stopLocationSet(player);

        plugin.saveAsynchronously(this);
    }

    private void handleScriptEdit(Player player, T trigger) {

        plugin.getScriptEditManager().startEdit(new BukkitPlayer(player), trigger.getTriggerName(), trigger.getScript(),
                new SaveHandler() {
                    @Override
                    public void onSave(String script) {
                        try {
                            trigger.setScript(script);
                        } catch (TriggerInitFailedException e) {
                            plugin.handleException(new BukkitPlayer(player), e);
                        }

                        plugin.saveAsynchronously(LocationBasedTriggerManager.this);
                    }

                });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBreak(BlockBreakEvent e){
        if(e.isCancelled())
            return;

        Block block = e.getBlock();
        Block above = block.getRelative(BlockFace.UP);

        if(above.getType() != Material.SIGN && above.getType() != Material.SIGN_POST)
            return;

        BlockBreakEvent bbe = new BlockBreakEvent(above, e.getPlayer());
        onBreak(bbe);
        e.setCancelled(bbe.isCancelled());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Block block = e.getBlock();

        T trigger = getTriggerForLocation(block.getLocation());
        if(trigger == null)
            return;

        Player player = e.getPlayer();

        player.sendMessage(ChatColor.GRAY+"Cannot break trigger block.");
        player.sendMessage(ChatColor.GRAY+"To remove trigger, hold inspection tool "+INSPECTION_TOOL.name());
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent e){
        onItemSwap(new BukkitPlayer(e.getPlayer()));
    }

    protected T getTriggerForLocation(Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return getTriggerForLocation(sloc);
    }

    protected void setTriggerForLocation(Location loc, T trigger) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setTriggerForLocation(sloc, trigger);
    }

    protected T removeTriggerForLocation(Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return removeTriggerForLocation(sloc);
    }

    protected void showTriggerInfo(ICommandSender sender, Block clicked) {
        Trigger trigger = getTriggerForLocation(LocationUtil.convertToSimpleLocation(clicked.getLocation()));
        if(trigger == null){
            return;
        }

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: "+getTriggerTypeName());
        sender.sendMessage("Block Type: " + clicked.getType().name());
        sender.sendMessage("Location: " + clicked.getWorld().getName() + "@" + clicked.getLocation().getBlockX() + ","
                + clicked.getLocation().getBlockY() + "," + clicked.getLocation().getBlockZ());
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    @Override
    protected void showTriggerInfo(ICommandSender sender, SimpleLocation sloc) {
        Trigger trigger = getTriggerForLocation(sloc);
        if(trigger == null){
            return;
        }

        Location loc = LocationUtil.convertToBukkitLocation(sloc);
        Block clicked = loc.getBlock();

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: "+getTriggerTypeName());
        sender.sendMessage("Block Type: " + clicked.getType().name());
        sender.sendMessage("Location: " + clicked.getWorld().getName() + "@" + clicked.getLocation().getBlockX() + ","
                + clicked.getLocation().getBlockY() + "," + clicked.getLocation().getBlockZ());
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    /**
    *
    * @param player
    * @param loc
    * @return true if cut ready; false if no trigger found at the location
    */
    protected boolean cutTrigger(Player player, Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return cutTrigger(new BukkitPlayer(player), sloc);
    }

    /**
    *
    * @param player
    * @param loc
    * @return true if copy ready; false if no trigger found at the location
    */
    protected boolean copyTrigger(Player player, Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return copyTrigger(new BukkitPlayer(player), sloc);
    }

    /**
    *
    * @param player
    * @param loc
    * @return true if pasted; false if nothing in the clipboard
    */
    protected boolean pasteTrigger(Player player, Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return pasteTrigger(new BukkitPlayer(player), sloc);
    }

    protected Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(Chunk chunk) {
        SimpleChunkLocation scloc = LocationUtil.convertToSimpleChunkLocation(chunk);
        return getTriggersInChunk(scloc);
    }



    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(folder, trigger.getTriggerName()));
    }
}
