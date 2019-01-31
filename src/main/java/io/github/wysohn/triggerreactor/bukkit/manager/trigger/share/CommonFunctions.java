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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.*;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.SkullUtil;

public class CommonFunctions extends io.github.wysohn.triggerreactor.core.manager.trigger.share.CommonFunctions
        implements SelfReference {
    private final TriggerReactor plugin;

    public CommonFunctions(TriggerReactor plugin) {
        super();
        this.plugin = plugin;
    }

    /**
     * Simply try to get plugin object directly. *
     * <p>
     * Example) #MESSAGE "spawn region info:
     * "+plugin("WorldGuard").getRegionManager(player.getWorld()).getRegion("spawn")
     * </p>
     *
     * @param name
     *            name of the plugin.
     * @return Plugin object on success; null if plugin not found or loaded.
     */
    public Plugin plugin(String name){
        return Bukkit.getPluginManager().getPlugin(name);
    }

    /**
     * Deprecated since 1.13
     * @param player
     * @param id
     * @param amount
     * @return
     * @deprecated use {@link #takeItem(Player, String, int)} instead
     */
    @Deprecated
    public boolean takeItem(Player player, int id, int amount) {
        throw new RuntimeException("Cannot use numeric value for id since 1.13. Use appropriate Material value.");
    }

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, 1, 1); #MESSAGE "Removed one stone."; ELSE; #MESSAGE "You don't have a stone"; ENDIF;
     * </p>
     * @param player
     *            target player
     * @param id
     *            item id
     * @param amount
     *            amount
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, String id, int amount) {
        ItemStack IS = new ItemStack(Material.valueOf(id), amount);
        return takeItem(player, IS, amount);
    }

    /**
     * Deprecated since 1.13
     * @param player
     * @param id
     * @param amount
     * @param data
     * @return
     * @deprecated use {@link #takeItem(Player, String, int, int)} instead
     */
    @Deprecated
    public boolean takeItem(Player player, int id, int amount, int data) {
        throw new RuntimeException("Cannot use numeric value for id since 1.13. Use appropriate Material value.");
    }

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, 1, 1, 1); #MESSAGE "Removed one granite."; ELSE; #MESSAGE "You don't have a granite"; ENDIF;
     * </p>
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
    public boolean takeItem(Player player, String id, int amount, int data) {
        ItemStack IS = new ItemStack(Material.valueOf(id), amount, (short) data);
        if (!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, 1, 1, 1); #MESSAGE "Removed one granite."; ELSE; #MESSAGE "You don't have a granite"; ENDIF;
     * </p>
     * @param player
     *            target player
     * @param IS
     * 		      exact item type
     * @param amount
     *            amount
     * @param data
     *            data of item
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, ItemStack IS, int amount) {
        if (!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }
    
    public Location location(String world, double x, double y, double z){
        World w = Bukkit.getWorld(world);
        if(world == null)
            throw new RuntimeException("world "+world+" does not exists!");
        return new Location(w, x, y, z);
    }

    public Location location(String world, double x, double y, double z, double yaw, double pitch){
        World w = Bukkit.getWorld(world);
        if(world == null)
            throw new RuntimeException("world "+world+" does not exists!");
        return new Location(w, x, y, z, toFloat(yaw), toFloat(pitch));
    }

    public Block block(String world, int x, int y, int z){
        return location(world, x, y, z).getBlock();
    }

    /**
     * check if two location are equal not considering their decimal points
     * <p>
     * Example) /trg run IF locationEqual(player.getLocation(), {"otherLocation"}); #MESSAGE "match"; ENDIF;
     * </p>
     * @param loc1
     * @param loc2
     * @return true if equal; false if not
     */
    public boolean locationEqual(Location loc1, Location loc2) {
        return loc1.getWorld() == loc2.getWorld() && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * create a PotionEffect for use in entity.addPotionEffect();
     * <p>
     * Example) /trg run player.addPotionEffect( makePotionEffect("SPEED", 1000000, 5, false, true, bukkitColor(21,2,24) ))
     * </p>
     * @param EffectType the name of the PotionEffectType to use
     * @param duration how long the potioneffect should last when applied to an enitity
     * @param amplifier how strong the effect should be
     * @param ambient if true particle effects will be more transparent
     * @param particles if false potion particle effects will not be shown
     * @param color color is no longer available since 1.13
     * @return returns a PotionEffect object or null if specified PotionEffectType was not found.
     */
    public PotionEffect makePotionEffect(String EffectType, int duration, int amplifier, boolean ambient,
            boolean particles, Color color) {
        PotionEffectType type = null;
        type = PotionEffectType.getByName(EffectType);

        if (type != null) {
            return new PotionEffect(type, duration, amplifier, ambient, particles);
        } else {
            return null;
        }

    }

    /**
     * try to get a player from name. Mostly online player.
     * <p>
     * Example) player("wysohn").performCommand("spawn")
     * </p>
     * @param name name of player
     * @return the Player object; it can be null if no player is found with the name
     */
    public Player player(String name){
        return Bukkit.getPlayer(name);
    }

    /**
     * try to get offline player from name.
     * <p>
     * Example) /trg run #MESSAGE "UUID is: "+oplayer("wysohn").getUniqueId()
     * </p>
     * @param name name of player
     * @return the OfflinePlayer object; it never returns null but always return an offline player even if the player
     * doesn't exist.
     */
    public OfflinePlayer oplayer(String name){
        return Bukkit.getOfflinePlayer(name);
    }

    /**
     * get list of online players online
     * <p>
     * Example) /trg run FOR p = getPlayers(); p.performCommand("spawn"); ENDFOR;
     * </p>
     * @return player iterator
     */
    public Collection<? extends Player> getPlayers() {
        return BukkitUtil.getOnlinePlayers();
    }

/*    public static void main(String[] ar) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalArgumentException, InvocationTargetException {
        CommonFunctions com = new CommonFunctions(null);
        com.newInstance(Test.class.getName(), new Child());
    }

    public static class Parent{

    }

    public static class Child extends Parent{

    }

    public static class Test{
        public Test(Parent parent) {

        }
    }*/

    /**
     * Get the name of area where entity is currently standing on.
     * <p>
     * Example) /trg run #MESSAGE "You are in the AreaTrigger named: "+currentArea(player)
     * </p>
     * @param entity
     *            any entity(including player)
     * @return name of area; null if player is not on any area.
     */
    public String currentArea(Entity entity) {
        return currentAreaAt(entity.getLocation());
    }

    /**
     * Get the name of area trigger at the target location.
     * Since 2.1.8, as Area Triggers can overlap each other, there can be multiple result.
     * This method is left as is, but it will return only the first area found.
     * To get the full list of Area Triggers, use {@link #currentAreasAt(Location)}
     * @param location the location to check
     * @return name of area; null if there is no area trigger at location
     * @deprecated this only return one AreaTrigger's name, yet there could be more
     */
    public String currentAreaAt(Location location) {
        String[] areaNames = currentAreasAt(location);
        return areaNames.length > 0 ? areaNames[0] : null;
    }

    /**
     * Get the name of area triggers containing the given location.
     * @param location the location to check
     * @return array of AreaTrigger names. The array can be empty but never null.
     */
    public String[] currentAreasAt(Location location){
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        String[] names = areaManager.getAreas(LocationUtil.convertToSimpleLocation(location)).stream()
                .map(Map.Entry::getValue)
                .map(AbstractTriggerManager.Trigger::getTriggerName)
                .toArray(String[]::new);
        return names;
    }

    /**
     * Get list of entities tracked by this AreaTrigger.
     * @param areaTriggerName name of AreaTrigger to get entities from
     * @return List of entities. null if the AreaTrigger with specified name doesn't exist.
     */
    public List<Entity> getEntitiesInArea(String areaTriggerName){
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        AreaTriggerManager.AreaTrigger trigger = areaManager.getArea(areaTriggerName);
        if (trigger == null)
            return null;

        List<Entity> entities = new ArrayList<>();
        for(IEntity ie : trigger.getEntities())
            entities.add(ie.get());

        return entities;
    }

    /**
     * Translate & into minecraft color code
     * <p>
     * Example) /trg run player.sendMessage(color("&aGREEN, &cRED"))
     * </p>
     * @param str unprocessed string
     * @return string with minecraft color codes
     */
    public String color(String str){
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    /**
     * creates and returns a bukkit color object using
     * the RGB values given. the max value for the int arguments is 255
     * exceeding it may cause errors.
     * <p>
     * Example) /trg run partColor = bukkitColor(255,255,255)
     * </p>
     * @param red red the value of red in RGB
     * @param green green the value of green in RGB
     * @param blue blue the value of blue in RGB
     * @return returns a Color object from org.bukkit.Color
     */
    public Color bukkitColor(int red, int green, int blue){
        Color color = null;
	color = color.fromRGB(red,green,blue);
	return color;
    }

    /**
     * Deprecated since 1.13
     * @param type
     * @param amount
     * @param data
     * @return
     * @deprecated use {@link #item(String, int, int)} instead
     */
    @Deprecated
    public ItemStack item(int type, int amount, int data){
        throw new RuntimeException("Cannot use numeric value for type since 1.13. Use appropriate Material value.");
    }

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item(1, 64, 0)
     * </p>
     * @param type typeId
     * @param amount amount of item
     * @param data data
     * @return the ItemStack
     */
    public ItemStack item(String type, int amount, int data){
        return new ItemStack(Material.valueOf(type), amount, (short) data);
    }

    /**
     * Deprecated since 1.13
     * @param type
     * @param amount
     * @return
     * @deprecated use {@link #item(String, int)} instead
     */
    @Deprecated
    public ItemStack item(int type, int amount){
        throw new RuntimeException("Cannot use numeric value for type since 1.13. Use appropriate Material value.");
    }

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item(1, 32)
     * </p>
     * @param type typeId
     * @param amount amount of item
     * @return the ItemStack
     */
    public ItemStack item(String type, int amount){
        return item(type, amount, 0);
    }

    /**
     * Get title of the specified ItemStack. Empty String if not exist.
     * <p>
     * Example) /trg run #MESSAGE "item name is: "+getItemTitle(player.getItemInHand())
     * </p>
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
     * <p>
     * Example) /trg run myitem = item(1, 1); setItemTitle(myitem, "I'm stone"); #GIVE myitem;
     * </p>
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
        if(lores == null)
            return false;

        return lores.contains(lore);
    }

    /**
     * get Lore at the specified index
     * @param IS
     * @param index
     * @return String of lore; null if not found
     */
    public String getLore(ItemStack IS, int index){
        ItemMeta IM = IS.getItemMeta();
        if(IM == null)
            return null;

        List<String> lores = IM.getLore();
        if(lores == null)
            return null;

        return lores.get(index);
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

    /**
     * Translate money into specified country's currency format. You need to provide exact locale provided
     *  in http://www.oracle.com/technetwork/java/javase/java8locales-2095355.html
     * @param money
     * @param locale1 language code (Ex. en)
     * @param locale2 country code (Ex. US)
     * @return formatted currecy
     */
    @Override
    public String formatCurrency(double money, String locale1, String locale2){
        Locale locale = new Locale(locale1, locale2);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        return currencyFormatter.format(money);
    }

    /**
     * Translate money into specified country's currency format. US currency will be used.
     * @param money
     * @return formatted currecy
     */
    @Override
    public String formatCurrency(double money){
        return formatCurrency(money, "en", "US");
    }

    /**
     * Get the block which player is looking at
     * <p>
     * Example) /trg run #MESSAGE "looking at the block: "+getTargetBlock(player, 30);
     * </p>
     * @param player
     * @param maxDistance
     * @return block
     */
    public Block getTargetBlock(Player player, int maxDistance){
        return player.getTargetBlock((HashSet<Material>)null, maxDistance);
    }

    /**
     * Create a player head with given name.
     *
     * <p>
     * Example) /trg run #GIVE headForName("wysohn")
     * </p>
     *
     * @param targetName
     *            name of the owner of head
     * @return the ItemStack head
     */
    public ItemStack headForName(String targetName) {
        ItemStack head = BukkitUtil.getPlayerHeadItem();
        ItemMeta IM = head.getItemMeta();
        SkullMeta SM = (SkullMeta) IM;
        SM.setOwner(targetName);
        head.setItemMeta(SM);
        return head;
    }

	public ItemStack headForName(String targetName, int amount) {
        ItemStack head = BukkitUtil.getPlayerHeadItem();
        head.setAmount(amount);
        ItemMeta IM = head.getItemMeta();
        SkullMeta SM = (SkullMeta) IM;
        SM.setOwner(targetName);
        head.setItemMeta(SM);
        return head;
    }

    /**
     * Create a player head with given textureValue(base64 encoded).
     *
     * <p>
     * Example) /trg run #GIVE
     * headForValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlc
     * y5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RjZDdkZjUyNjQ1YzY4Y2RhZDg1NDhlNjFiM2Y3
     * NjU5NjQwNzcyMjZiYTg3MmI5ZDJiZDQ1YzQz<b>OWQifX19\\</b>")
     * </p>
     * <p>
     * <b> Notice that there is an extra \ sign as \ is an escape sequence. The final
     * String will be ...OWQifX19\ instead of ...OWQifX19\\" </b>
     * </p>
     *
     * @param textureValue
     *            base64 encoded texture value
     * @return the ItemStack head
     */
    public ItemStack headForValue(String textureValue) {
        ItemStack head = BukkitUtil.getPlayerHeadItem();
        ItemMeta IM = head.getItemMeta();
        SkullMeta SM = (SkullMeta) IM;
        try {
            SkullUtil.setTextureValue(SM, textureValue);
        } catch (Exception e) {
            //e.printStackTrace();
            return head;
        }
        head.setItemMeta(SM);
        return head;
    }

    //Eventually, this has to be created either as Executor or Placeholder
//    public BossBar makeBossBar(String title, String color, String style) {
//        BarColor colorEnum = BarColor.valueOf(color.toUpperCase());
//	    BarStyle styleEnum = BarStyle.valueOf(style.toUpperCase());
//
//        BossBar BarObj = null;
//        try {
//            BarObj = (BossBar) ReflectionUtil.invokeMethod(Bukkit.class, (Object) null, "createBossBar", title, colorEnum, styleEnum);
//        } catch (NoSuchMethodException e) {
//            return null;
//        } catch (InvocationTargetException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        return BarObj;
//    }
}
