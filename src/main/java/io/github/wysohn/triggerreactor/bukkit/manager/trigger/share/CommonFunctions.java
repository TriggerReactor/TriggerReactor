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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import io.github.wysohn.triggerreactor.tools.SkullUtil;

public class CommonFunctions implements SelfReference {
    private static final Random rand = new Random();

    private final TriggerReactor plugin;
    public CommonFunctions(TriggerReactor plugin) {
        super();
        this.plugin = plugin;
    }

    /**
     * get a random integer value between 0 to end
     *
     *<p>
     *Example) #MESSAGE "You rolled the dice: "+(random(6) + 1)
     *</p>
     *
     * @param end
     *            exclusive
     * @return
     */
    public int random(int end) {
        return rand.nextInt(end);
    }
    
    public float random(float end) {
        return rand.nextFloat(end);
    }
    
    public double random(double end) {
        return rand.nextDouble(end);
    }
    
    public long random(long end) {
        return rand.nextLong(end);
    }

    /**
     * get a random integer value between start to end
     *     *<p>
     *Example) #MESSAGE "You rolled the dice: "+random(1, 7)
     *</p>
     * @param start
     *            inclusive
     * @param end
     *            exclusive
     * @return
     */
    public int random(int start, int end) {
        return start + rand.nextInt(end - start);
    }
    
    public float random(float start, float end) {
        return start + rand.nextFloat(end - start);
    }
    
    public double random(double start, double end) {
        return start + rand.nextDouble(end - start);
    }
    
    public long random(long start, long end) {
        return start + rand.nextLong(end - start);
    }

    /**
     * Simply try to get plugin object directly.
     *      *<p>
     *Example) #MESSAGE "spawn region info: "+plugin("WorldGuard").getRegionManager(player.getWorld()).getRegion("spawn")
     *</p>
     * @param name name of the plugin.
     * @return Plugin object on success; null if plugin not found or loaded.
     */
    public Plugin plugin(String name){
        return Bukkit.getPluginManager().getPlugin(name);
    }

    /**
     * Try to get static value for specified field from specified class.
     * @param className the full class name
     * @param fieldName the name of static field
     * @return the value in the static field
     * @throws ClassNotFoundException error if the specified 'className' does not exist
     * @throws NoSuchFieldException error if the specified 'fieldName' field does not exist in the class.
     */
    public Object staticGetFieldValue(String className, String fieldName) throws ClassNotFoundException, NoSuchFieldException{
        Class<?> clazz = Class.forName(className);

        Field field = clazz.getField(fieldName);

        try {
            return field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            //Unexpected error
        }

        return null;
    }

    /**
     * Try to set static value for specified field from specified class.
     *  This can lead any plugin to catastrpohic failuer if you don't what exactly you are doing.
     *  Use it with your own risk.
     * @param className full name of the class
     * @param fieldName name of the static field
     * @param value the value to save into the field
     * @throws ClassNotFoundException error if the specified 'className' does not exist
     * @throws NoSuchFieldException error if the specified 'fieldName' field does not exist in the class.
     * @throws IllegalArgumentException if the 'value' is incompatible with the field type.
     */
    public void staticSetFieldValue(String className, String fieldName, Object value) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException{
        Class<?> clazz = Class.forName(className);

        Field field = clazz.getField(fieldName);

        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            //Unexpected error
        }
    }

    /**
     * Invoke the static method of provided class.
     * @param className the full name of the class.
     * @param methodName the name of static method.
     * @param args array of arguments. This can be empty if the method doesn't have any arguments.
     *  (Ex. staticMethod("my.class", "PewPew")
     * @return some value depends on the method; it can be null if the method returns nothing.
     * @throws ClassNotFoundException error if the 'className' does not exist.
     * @throws NoSuchMethodException error if the 'methodName' does not exist in the class.
     * @throws IllegalArgumentException error if invalid 'args' are passed to the method.
     */
    public Object staticMethod(String className, String methodName, Object... args) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException{
        Class<?> clazz = Class.forName(className);

        try {
            return ReflectionUtil.invokeMethod(clazz, null, methodName, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
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
    public boolean takeItem(Player player, int id, int amount) {
        ItemStack IS = new ItemStack(id, amount);
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
     * @param id
     *            item id
     * @param amount
     *            amount
     * @param data
     *            data of item
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, int id, int amount, int data) {
        ItemStack IS = new ItemStack(id, amount, (short) data);
        if (!player.getInventory().containsAtLeast(IS, amount))
            return false;

        player.getInventory().removeItem(IS);
        return true;
    }

    public Location location(String world, int x, int y, int z){
        World w = Bukkit.getWorld(world);
        if(world == null)
            throw new RuntimeException("world "+world+" does not exists!");
        return new Location(w, x, y, z);
    }

    public SimpleLocation slocation(String world, int x, int y, int z){
        return new SimpleLocation(world, x, y, z);
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
     * matches the str by using regular expression regex
     * <p>
     * Example) /trg run IF matches("PewPew", "[a-zA-Z0-9 ]+"); #MESSAGE "Seems to be English"; ENDIF;
     * </p>
     * @param str
     * @param regex
     * @return true if str matches with regex; false if not
     */
    public boolean matches(String str, String regex) {
        return str.matches(regex);
    }

    /**
     * parse the string that contains integer into real integer value.
     * <p>
     * Example) /trg run #MESSAGE parseInt("300") + 50
     * </p>
     * @param str the string that contains integer
     * @return the real integer
     */
    public int parseInt(String str){
        return Integer.parseInt(str);
    }

    /**
     * parse the string that contains decimal into real decimal value.
     * <p>
     * Example) /trg run #MESSAGE parseDouble("15.30") + 2.12
     * </p>
     * @param str the string that contains decimal
     * @return the real decimal
     */
    public double parseDouble(String str){
        return Double.parseDouble(str);
    }

    /**
     * Get enum value manually. Usually, you can simply pass String value to the
     * Enum parameter, yet in some cases, you need to manually create enum
     * value. For example, TriggerReactor cannot detect Generic argument, so you
     * have to manually create enum value.
     * <p>
     * Example) /trg run #MESSAGE "value is "+parseEnum("org.bukkit.GameMode",
     * "CREATIVE")
     * </p>
     * <p>
     * Example) /trg run player.setGameMode(parseEnum("org.bukkit.GameMode",
     * "CREATIVE"))
     * </p>
     * <p>
     * Example) /trg run player.setGameMode("CREATIVE") //This also works
     * </p>
     *
     * @param enumClassName
     *            the full class name of enum.
     * @param valueName
     *            the enum value to parse.
     * @return the actual enum value.
     * @throws ClassNotFoundException
     *             if the provided class name doesn't exist or not enum class.
     */
    @SuppressWarnings("unchecked")
    public Object parseEnum(String enumClassName, String valueName) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(enumClassName);
        if(!clazz.isEnum())
            throw new ClassNotFoundException("Found the class ["+clazz.getName()+"], but it wasn't Enum");

        return Enum.valueOf((Class<? extends Enum>) clazz, valueName);
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

    /**
     * create an empty array
     * <p>
     * Example) /trg run temp = array(5); temp[3] = "hi"; #MESSAGE temp[3];
     * </p>
     * @param size
     *            size of array
     * @return
     */
    public Object[] array(int size) {
        return new Object[size];
    }

    public List list(){
        return new ArrayList();
    }

    public Map map(){
        return new HashMap();
    }

    public Set set(){
        return new HashSet();
    }

    public Object newInstance(String className, Object... args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalArgumentException, InvocationTargetException{
        Class<?> clazz = Class.forName(className);

        Class<?>[] types = new Class[args.length];
        for(int i = 0; i < types.length; i++){
            types[i] = args[i].getClass();
        }

        Constructor con = null;
        Constructor[] cons = clazz.getConstructors();
        outer:for(Constructor check : cons) {
            Class<?>[] params = check.getParameterTypes();
            if(params.length == types.length) {
                for(int i = 0; i < types.length; i++) {
                    if(!params[i].isAssignableFrom(types[i])) {
                        break;
                    }

                    //we found the constructor
                    con = check;
                    break outer;
                }
            }
        }

        if(con != null) {
            con.setAccessible(true);

            try {
                return con.newInstance(args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return null;
        }else {
            StringBuilder builder = new StringBuilder("Could not found counstuctor with matching parameters.");
            builder.append(" -- ");
            builder.append(className+"<init>");
            builder.append("(");
            for(int i = 0; i < types.length - 1; i++)
                builder.append(types[i].getSimpleName()+", ");
            builder.append(types[types.length - 1].getSimpleName());
            builder.append(")");

            throw new NoSuchMethodException(builder.toString());
        }
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
     * @param location the location to check
     * @return name of area; null if there is no area trigger at location
     */
    public String currentAreaAt(Location location) {
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        AreaTriggerManager.AreaTrigger trigger = areaManager.getArea(LocationUtil.convertToSimpleLocation(location));
        if (trigger == null)
            return null;

        return trigger.getTriggerName();
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
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
     * @param argument
     *            array to merge
     * @param indexFrom
     *            inclusive
     * @return
     */
    public String mergeArguments(String[] args, int indexFrom) {
        return mergeArguments(args, indexFrom, args.length - 1);
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     * @param argument
     *            array to merge
     * @param indexFrom
     *            inclusive
     * @param indexTo
     *            inclusive
     * @return
     */
    public String mergeArguments(String[] args, int indexFrom, int indexTo) {
        StringBuilder builder = new StringBuilder(args[indexFrom]);
        for(int i = indexFrom + 1; i <= indexTo; i++){
            builder.append(" "+args[i]);
        }
        return builder.toString();
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
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item(1, 64, 0)
     * </p>
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
     * <p>
     * Example) /trg run #GIVE item(1, 32)
     * </p>
     * @param type typeId
     * @param amount amount of item
     * @return the ItemStack
     */
    public ItemStack item(int type, int amount){
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
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
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
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
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
}
