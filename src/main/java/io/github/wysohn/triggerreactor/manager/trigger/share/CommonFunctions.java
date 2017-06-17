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
package io.github.wysohn.triggerreactor.manager.trigger.share;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.wysohn.triggerreactor.core.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.trigger.AreaTriggerManager;

public class CommonFunctions implements SelfReference {
    private static final Random rand = new Random();

    /**
     * get a random integer value between 0 to end
     *
     * @param end
     *            exclusive
     * @return
     */
    public int random(Integer end) {
        return rand.nextInt(end);
    }

    /**
     * get a random integer value between start to end
     *
     * @param start
     *            inclusive
     * @param end
     *            exclusive
     * @return
     */
    public int random(Integer start, Integer end) {
        return start + rand.nextInt(end - start);
    }

    /**
     * take item from player.
     *
     * @param player
     *            target player
     * @param id
     *            item id
     * @param amount
     *            amount
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, Integer id, Integer amount) {
        ItemStack IS = new ItemStack(id, amount);
        if (!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }

    /**
     * take item from player.
     *
     * @param player
     *            target player
     * @param id
     *            item id
     * @param amount
     *            amount
     * @param data
     *            data of item
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, Integer id, Integer amount, short data) {
        ItemStack IS = new ItemStack(id, amount, data);
        if (!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }

    /**
     * check if two location are equal not considering their decimal points
     *
     * @param loc1
     * @param loc2
     * @return true if equal; false if not
     */
    public boolean locationEqual(Location loc1, Location loc2) {
        return loc1.getWorld() == loc2.getWorld() && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * matches the str by using regular expression regex
     *
     * @param str
     * @param regex
     * @return true if str matches with regex; false if not
     */
    public boolean matches(String str, String regex) {
        return str.matches(regex);
    }

    /**
     * parse the string that contains integer into real integer value.
     * @param str the string that contains integer
     * @return the real integer
     */
    public int parseInt(String str){
        return Integer.parseInt(str);
    }

    /**
     * parse the string that contains decimal into real decimal value.
     * @param str the string that contains decimal
     * @return the real decimal
     */
    public double parseDouble(String str){
        return Double.parseDouble(str);
    }

    /**
     * get list of online players online
     *
     * @return player iterator
     */
    public Collection<? extends Player> getPlayers() {
        return Bukkit.getOnlinePlayers();
    }

    /**
     * create an empty array
     *
     * @param size
     *            size of array
     * @return
     */
    public Object[] array(Integer size) {
        return new Object[size];
    }

    /**
     * Get the name of area where player is currently standing on.
     *
     * @param player
     *            player
     * @return name of area; null if player is not on any area.
     */
    public String currentArea(Player player) {
        AreaTriggerManager areaManager = TriggerReactor.getInstance().getAreaManager();
        AreaTriggerManager.AreaTrigger trigger = areaManager.getArea(player.getLocation());
        if (trigger == null)
            return null;

        return trigger.getName();
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param argument
     *            array to merge
     * @return
     */
    public String mergeArguments(String[] args) {
        return mergeArguments(args, 0, args.length - 1);
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param argument
     *            array to merge
     * @param indexFrom
     *            inclusive
     * @return
     */
    public String mergeArguments(String[] args, Integer indexFrom) {
        return mergeArguments(args, indexFrom, args.length - 1);
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param argument
     *            array to merge
     * @param indexFrom
     *            inclusive
     * @param indexTo
     *            inclusive
     * @return
     */
    public String mergeArguments(String[] args, Integer indexFrom, Integer indexTo) {
        StringBuilder builder = new StringBuilder(args[indexFrom]);
        for(int i = indexFrom + 1; i <= indexTo; i++){
            builder.append(" "+args[i]);
        }
        return builder.toString();
    }

    /**
     * Translate & into minecraft color code
     * @param str unprocessed string
     * @return string with minecraft color codes
     */
    public String color(String str){
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    /**
     * Create a new ItemStack
     * @param type typeId
     * @param amount amount of item
     * @param data data
     * @return the ItemStack
     */
    public ItemStack item(int type, int amount, int data){
        return new ItemStack(type, amount, (short) data);
    }

    /**
     * Create a new ItemStack
     * @param type typeId
     * @param amount amount of item
     * @return the ItemStack
     */
    public ItemStack item(int type, int amount){
        return item(type, amount, 0);
    }

    /**
     * Get title of the specified ItemStack. Empty String if not exist.
     * @param IS
     * @return
     */
    public String getItemTitle(ItemStack IS){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            return "";

        String dispName = IM.getDisplayName();
        return dispName == null ? "" : dispName;
    }

    /**
     * Set title of the specified ItemStack
     * @param IS
     * @param title
     */
    public void setItemTitle(ItemStack IS, String title){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if(IM == null)
            return;
        IM.setDisplayName(color(title));
        IS.setItemMeta(IM);
    }

    /**
     * Check if the specified ItemStack contains the 'lore.' At least one is contained
     *  in the lore will return true.
     * @param IS
     * @param lore
     * @return true if 'lore' is in IS; false if not
     */
    public boolean hasLore(ItemStack IS, String lore){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            return false;

        List<String> lores = IM.getLore();
        if(lore == null)
            return false;

        return lores.contains(lore);
    }

    /**
     * Append a lore to the specified ItemStack
     * @param IS
     * @param lore
     */
    public void addLore(ItemStack IS, String lore){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if(IM == null)
            return;
        List<String> lores = IM.getLore();
        if(lores == null)
            lores = new ArrayList<>();
        lores.add(color(lore));
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    /**
     * Replace a lore at 'index' for the specified ItemStack
     * @param IS
     * @param index
     * @param lore
     */
    public void setLore(ItemStack IS, int index, String lore){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if(IM == null)
            return;
        List<String> lores = IM.getLore();
        if(lores == null)
            lores = new ArrayList<>();
        lores.set(index, color(lore));
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    /**
     * Remove lore at the 'index' of the specified ItemStack
     * @param IS
     * @param index
     */
    public void removeLore(ItemStack IS, int index){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if(IM == null)
            return;
        List<String> lores = IM.getLore();
        if(lores == null)
            lores = new ArrayList<>();
        lores.remove(index);
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    /**
     * Clear all lores from item.
     * @param IS
     */
    public void clearLore(ItemStack IS){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            return;
        IM.setLore(new ArrayList<>());
        IS.setItemMeta(IM);
    }

    /**
     * get size of the lores for the specified ItemStack
     * @param IS
     * @return
     */
    public int loreSize(ItemStack IS){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            return 0;
        List<String> lores = IM.getLore();
        if(lores == null)
            return 0;
        return lores.size();
    }
}
