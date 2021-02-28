package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.text.NumberFormat;
import java.util.*;

public abstract class AbstractCommonFunctions extends io.github.wysohn.triggerreactor.core.manager.trigger.share.CommonFunctions implements SelfReference {
    protected final TriggerReactorCore plugin;

    public AbstractCommonFunctions(TriggerReactorCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Simply try to get plugin object directly. *
     * <p>
     * Example) #MESSAGE "spawn region info:
     * "+plugin("WorldGuard").getRegionManager(player.getWorld()).getRegion("spawn")
     * </p>
     *
     * @param name name of the plugin.
     * @return Plugin object on success; null if plugin not found or loaded.
     */
    public Plugin plugin(String name) {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    /**
     * Deprecated since 1.13. Even though you are advised to used the new method,
     * this method is supported only for legacy-bukkit version
     *
     * @param player
     * @param id
     * @param amount
     * @return
     * @deprecated use {@link #takeItem(Player, String, int)} instead
     */
    @Deprecated
    public abstract boolean takeItem(Player player, int id, int amount);

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, "STONE", 1); #MESSAGE "Removed one stone."; ELSE; #MESSAGE "You don't have a stone"; ENDIF;
     * </p>
     * You can find item names in <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html">Material</a>
     *
     * @param player target player
     * @param id     item id
     * @param amount amount
     * @return true if took it; false if player doesn't have it
     */
    public abstract boolean takeItem(Player player, String id, int amount);

    /**
     * Deprecated since 1.13. Even though you are advised to used the new method,
     * this method is supported only for legacy-bukkit version
     *
     * @param player
     * @param id
     * @param amount
     * @param data
     * @return
     * @deprecated use {@link #takeItem(Player, String, int, int)} instead
     */
    @Deprecated
    public abstract boolean takeItem(Player player, int id, int amount, int data);

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, "STONE", 1, 1); #MESSAGE "Removed one granite."; ELSE; #MESSAGE "You don't have a granite"; ENDIF;
     * </p>
     * You can find item names in <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html">Material</a>
     *
     * @param player target player
     * @param id     item id
     * @param amount amount
     * @param data   data of item
     * @return true if took it; false if player doesn't have it
     */
    public abstract boolean takeItem(Player player, String id, int amount, int data);

    /**
     * take ItemStack from player. This check for every single metadata (title, lores, enchantment, etc.),
     * so only the exactly matching items will be removed
     * <p>
     * Example) /trg run IF takeItem(player, {"some.item"}, 1);
     * </p>
     * You can find item names in <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html">Material</a>
     *
     * @param player target player
     * @param IS     item id
     * @param amount amount
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, ItemStack IS, int amount) {
        if (!player.getInventory().containsAtLeast(IS, amount))
            return false;
        IS.setAmount(amount);

        player.getInventory().removeItem(IS);
        return true;
    }

    /**
     * Create Location instance.
     *
     * @param world the name of world.
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @return the Location.
     */
    public Location location(String world, double x, double y, double z) {
        return location(world, x, y, z, 0.0, 0.0);
    }

    /**
     * Create Location instance including pitch and yaw
     *
     * @param world the name of world.
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @param yaw   yaw
     * @param pitch pitch
     * @return the Location instance
     */
    public Location location(String world, double x, double y, double z, double yaw, double pitch) {
        World w = Bukkit.getWorld(world);
        if (w == null)
            throw new RuntimeException("world " + world + " does not exists!");
        return new Location(w, x, y, z, toFloat(yaw), toFloat(pitch));
    }

    /**
     * get Block instance. Be aware that when using the Block instance, it directly
     * interferes with the Server Thread, so it may cause Exception if any action
     * taken to this instance was from asynchronous triggers.
     *
     * @param world name of the world
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @return the Block instance
     */
    public Block block(String world, int x, int y, int z) {
        return location(world, x, y, z).getBlock();
    }

    /**
     * check if two location are equal not considering their decimal points
     * <p>
     * Example) /trg run IF locationEqual(player.getLocation(), {"otherLocation"}); #MESSAGE "match"; ENDIF;
     * </p>
     *
     * @param loc1 location
     * @param loc2 other location
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
     *
     * @param EffectType the name of the PotionEffectType to use
     * @param duration   how long the potioneffect should last when applied to an enitity
     * @param amplifier  how strong the effect should be
     * @param ambient    if true particle effects will be more transparent
     * @param particles  if false potion particle effects will not be shown
     * @param color      use bukkitColor() or is no longer available since 1.13
     * @return returns a PotionEffect object or null if specified PotionEffectType was not found.
     */
    public abstract PotionEffect makePotionEffect(String EffectType, int duration, int amplifier, boolean ambient,
                                                  boolean particles, Color color);

    /**
     * try to get a player from name. Mostly online player.
     * <p>
     * Example) player("wysohn").performCommand("spawn")
     * </p>
     *
     * @param name name of player
     * @return the Player object; it can be null if no player is found with the name
     */
    public Player player(String name) {
        return Bukkit.getPlayer(name);
    }

    /**
     * try to get offline player from name.
     * <p>
     * Example) /trg run #MESSAGE "UUID is: "+oplayer("wysohn").getUniqueId()
     * </p>
     *
     * @param name name of player
     * @return the OfflinePlayer object; it never returns null but always return an offline player even if the player
     * doesn't exist.
     */
    public OfflinePlayer oplayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    /**
     * get list of online players online
     * <p>
     * Example) /trg run FOR p = getPlayers(); p.performCommand("spawn"); ENDFOR;
     * </p>
     *
     * @return player iterator
     */
    public abstract Collection<? extends Player> getPlayers();

    /**
     * Get the name of area where entity is currently standing on.
     * <p>
     * Example) /trg run #MESSAGE "You are in the AreaTrigger named: "+currentArea(player)
     * </p>
     *
     * @param entity any entity(including player)
     * @return name of area; null if player is not on any area.
     * @deprecated this returns only one area among the areas where entity is in. Use {@link #currentAreas(Entity)}
     * instead to get all the areas.
     */
    @Deprecated
    public String currentArea(Entity entity) {
        String[] areas = currentAreasAt(entity.getLocation());
        return areas.length > 0 ? areas[0] : null;
    }

    /**
     * Get the name of area trigger at the target location.
     * Since 2.1.8, as Area Triggers can overlap each other, there can be multiple result.
     * This method is left as is, but it will return only the first area found.
     * To get the full list of Area Triggers, use {@link #currentAreasAt(Location)}
     *
     * @param location the location to check
     * @return name of area; null if there is no area trigger at location
     * @deprecated this only return one AreaTrigger's name, yet there could be more
     */
    @Deprecated
    public String currentAreaAt(Location location) {
        String[] areaNames = currentAreasAt(location);
        return areaNames.length > 0 ? areaNames[0] : null;
    }

    /**
     * Get the name of area where entity is currently standing on.
     * <p>
     * Example) /trg run #MESSAGE "You are in the AreaTrigger named: "+currentArea(player)
     * </p>
     *
     * @param entity any entity(including player)
     * @return array of name of areas; array can be empty but never null.
     */
    public String[] currentAreas(Entity entity) {
        return currentAreasAt(entity.getLocation());
    }

    /**
     * Get the name of area triggers containing the given location.
     *
     * @param location the location to check
     * @return array of AreaTrigger names. The array can be empty but never null.
     */
    public String[] currentAreasAt(Location location) {
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        return areaManager.getAreas(LocationUtil.convertToSimpleLocation(location)).stream()
                .map(Map.Entry::getValue)
                .map(Trigger::getInfo)
                .map(TriggerInfo::getTriggerName)
                .toArray(String[]::new);
    }

    /**
     * Get list of entities tracked by this AreaTrigger.
     *
     * @param areaTriggerName name of AreaTrigger to get entities from
     * @return List of entities. null if the AreaTrigger with specified name doesn't exist.
     */
    public List<Entity> getEntitiesInArea(String areaTriggerName) {
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        AreaTrigger trigger = areaManager.get(areaTriggerName);
        if (trigger == null)
            return null;

        List<Entity> entities = new ArrayList<>();
        for (IEntity ie : trigger.getEntities())
            entities.add(ie.get());

        return entities;
    }

    /**
     * Translate & into minecraft color code
     * <p>
     * Example) /trg run player.sendMessage(color("&aGREEN, &cRED"))
     * </p>
     *
     * @param str unprocessed string
     * @return string with minecraft color codes
     */
    public String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    /**
     * creates and returns a bukkit color object using
     * the RGB values given. the max value for the int arguments is 255
     * exceeding it may cause errors.
     * <p>
     * Example) /trg run partColor = bukkitColor(255,255,255)
     * </p>
     *
     * @param red   red the value of red in RGB
     * @param green green the value of green in RGB
     * @param blue  blue the value of blue in RGB
     * @return returns a Color object from org.bukkit.Color
     */
    public Color bukkitColor(int red, int green, int blue) {
        return Color.fromRGB(red, green, blue);
    }

    /**
     * Deprecated since 1.13. Even though you are advised to used the new method,
     * this method is supported only for legacy-bukkit version
     *
     * @param type
     * @param amount
     * @param data
     * @return
     * @deprecated use {@link #item(String, int, int)} instead
     */
    @Deprecated
    public abstract ItemStack item(int type, int amount, int data);

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item(1, 64, 0)
     * </p>
     *
     * @param type   typeId
     * @param amount amount of item
     * @param data   data
     * @return the ItemStack
     */
    public abstract ItemStack item(String type, int amount, int data);

    /**
     * Deprecated since 1.13. Even though you are advised to used the new method,
     * this method is supported only for legacy-bukkit version
     *
     * @param type
     * @param amount
     * @return
     * @deprecated use {@link #item(String, int)} instead
     */
    @Deprecated
    public abstract ItemStack item(int type, int amount);

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item(1, 32)
     * </p>
     *
     * @param type   typeId
     * @param amount amount of item
     * @return the ItemStack
     */
    public abstract ItemStack item(String type, int amount);

    /**
     * Get title of the specified ItemStack. Empty String if not exist.
     * <p>
     * Example) /trg run #MESSAGE "item name is: "+getItemTitle(player.getItemInHand())
     * </p>
     *
     * @param IS the target ItemStack to get title
     * @return the title of the ItemStack. Empty string if it's not set.
     */
    public String getItemTitle(ItemStack IS) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            return "";

        String dispName = IM.getDisplayName();
        return dispName == null ? "" : dispName;
    }

    /**
     * Set title of the specified ItemStack
     * <p>
     * Example) /trg run myitem = item(1, 1); setItemTitle(myitem, "I'm stone"); #GIVE myitem;
     * </p>
     *
     * @param IS    target ItemStack to change the title
     * @param title the title
     */
    public void setItemTitle(ItemStack IS, String title) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if (IM == null)
            return;
        IM.setDisplayName(color(title));
        IS.setItemMeta(IM);
    }

    /**
     * Check if the specified ItemStack contains the 'lore.' At least one is contained
     * in the lore will return true.
     *
     * @param IS   ItemStack to check lores fore
     * @param lore the lore
     * @return true if 'lore' is in IS; false if not
     */
    public boolean hasLore(ItemStack IS, String lore) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            return false;

        List<String> lores = IM.getLore();
        if (lores == null)
            return false;

        return lores.contains(lore);
    }

    /**
     * get Lore at the specified index
     *
     * @param IS    the ItemStack to get lore
     * @param index index of the lore
     * @return String of lore. If you use 'index' out of range or lore not exist, it returns null.
     */
    public String getLore(ItemStack IS, int index) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            return null;

        List<String> lores = IM.getLore();
        if (lores == null)
            return null;

        if (index < 0 || index >= lores.size())
            return null;

        return lores.get(index);
    }

    /**
     * Append a lore to the specified ItemStack
     *
     * @param IS   the target ItemStack
     * @param lore the lore to add
     */
    public void addLore(ItemStack IS, String lore) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if (IM == null)
            return;
        List<String> lores = IM.getLore();
        if (lores == null)
            lores = new ArrayList<>();
        lores.add(color(lore));
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    /**
     * Replace a lore at 'index' for the specified ItemStack.
     * If the 'index' is less than 0, it will throw an error, but if
     * 'index' is larger than previous size of lores, it will append empty
     * strings as much as needed.
     *
     * @param IS    the target ItemStack
     * @param index index to replace the lore
     * @param lore  the lore to set
     */
    public void setLore(ItemStack IS, int index, String lore) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if (IM == null)
            return;
        List<String> lores = IM.getLore();
        if (lores == null)
            lores = new ArrayList<>();

        while (index >= lores.size())
            lores.add("");

        lores.set(index, color(lore));
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    /**
     * Remove lore at the 'index' of the specified ItemStack.
     * if the 'index' is out of range, nothing will happen.
     *
     * @param IS    target ItemStack
     * @param index index of the lore to delete
     */
    public void removeLore(ItemStack IS, int index) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            IM = Bukkit.getItemFactory().getItemMeta(IS.getType());
        if (IM == null)
            return;
        List<String> lores = IM.getLore();
        if (lores == null)
            lores = new ArrayList<>();

        if (index < 0 || index >= lores.size())
            return;

        lores.remove(index);
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    /**
     * Clear all lores from item.
     *
     * @param IS target ItemStack
     */
    public void clearLore(ItemStack IS) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            return;
        IM.setLore(new ArrayList<>());
        IS.setItemMeta(IM);
    }

    /**
     * get size of the lores for the specified ItemStack
     *
     * @param IS target ItemStack
     * @return the number of lores
     */
    public int loreSize(ItemStack IS) {
        ItemMeta IM = IS.getItemMeta();
        if (IM == null)
            return 0;
        List<String> lores = IM.getLore();
        if (lores == null)
            return 0;
        return lores.size();
    }

    /**
     * Translate money into specified country's currency format. You need to provide exact locale provided
     * in http://www.oracle.com/technetwork/java/javase/java8locales-2095355.html
     *
     * @param money   money
     * @param locale1 language code (Ex. en)
     * @param locale2 country code (Ex. US)
     * @return formatted currecy
     */
    @Override
    public String formatCurrency(double money, String locale1, String locale2) {
        Locale locale = new Locale(locale1, locale2);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        return currencyFormatter.format(money);
    }

    /**
     * Translate money into specified country's currency format. US currency will be used.
     *
     * @param money money
     * @return formatted currecy
     */
    @Override
    public String formatCurrency(double money) {
        return formatCurrency(money, "en", "US");
    }

    /**
     * Get the block which player is looking at
     * <p>
     * Example) /trg run #MESSAGE "looking at the block: "+getTargetBlock(player, 30);
     * </p>
     *
     * @param player      Player instance
     * @param maxDistance maximum distance. More distance cause more loads to CPU.
     * @return block
     */
    public Block getTargetBlock(Player player, int maxDistance) {
        return player.getTargetBlock(null, maxDistance);
    }

    /**
     * Create a player head with given name.
     *
     * <p>
     * Example) /trg run #GIVE headForName("wysohn")
     * </p>
     *
     * @param targetName name of the owner of head
     * @return the ItemStack head
     */
    public ItemStack headForName(String targetName) {
        return headForName(targetName, 1);
    }

    /**
     * Create a player head with given name.
     *
     * <p>
     * Example) /trg run #GIVE headForName("wysohn")
     * </p>
     *
     * @param targetName name of the owner of head
     * @param amount     amount
     * @return the ItemStack head
     */
    public abstract ItemStack headForName(String targetName, int amount);

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
     * @param textureValue base64 encoded texture value
     * @return the ItemStack head
     */
    public abstract ItemStack headForValue(String textureValue);

    /**
     * Create a new SerializableLocation, which implements ConfigurationSerializable
     * <p>
     * Example) /trg run serialized = serializeLocation("wildw", 2, 13, 44)
     * </p>
     *
     * @param world The world instance in which this location resides
     * @param x The x-coordinate of this location
     * @param y The y-coordinate of this location
     * @param z The z-coordinate of this location
     * @return The SerializableLocation value
     */
    public abstract Object serializeLocation(World world, double x, double y, double z);

    /**
     * Create a new SerializableLocation, which implements ConfigurationSerializable
     * <p>
     * Example) /trg run serialized = serializeLocation("wildw", 2, 13, 44.6, 13)
     * </p>
     *
     * @param world The world instance in which this location resides
     * @param x The x-coordinate of this location
     * @param y The y-coordinate of this location
     * @param z The z-coordinate of this location
     * @param yaw The absolute rotation on the x-plane, in degrees
     * @param pitch The absolute rotation on the y-plane, in degrees
     * @return The SerializableLocation value
     */
    public abstract Object serializeLocation(World world, double x, double y, double z, double yaw, double pitch);

    /**
     * Create a new SerializableLocation, which implements ConfigurationSerializable
     * <p>
     * Example) /trg run serialized = serializeLocation("wildw", 2, 13, 44.6, 13)
     * </p>
     *
     * @param loc The Location value
     * @return The SerializableLocation value
     */
    public abstract Object serializeLocation(Location loc);
}
