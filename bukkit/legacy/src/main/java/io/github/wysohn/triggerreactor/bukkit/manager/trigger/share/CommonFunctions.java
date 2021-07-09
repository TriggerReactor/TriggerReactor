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

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.SerializableLocation;
import io.github.wysohn.triggerreactor.bukkit.tools.SkullUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class CommonFunctions extends AbstractCommonFunctions
        implements SelfReference {

    public CommonFunctions(TriggerReactorCore plugin) {
        super(plugin);
    }

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, 0, 1); #MESSAGE "Removed one stone."; ELSE; #MESSAGE "You don't have a stone"; ENDIF;
     * </p>
     *
     * @param player
     * @param id
     * @param amount
     * @return
     * @deprecated use {@link #takeItem(Player, String, int)} instead
     */
    @Deprecated
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
     * Example) /trg run IF takeItem(player, "STONE", 1); #MESSAGE "Removed one stone."; ELSE; #MESSAGE "You don't have a stone"; ENDIF;
     * </p>
     * You can find item names in <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html">Material</a>
     *
     * @param player target player
     * @param id     item id
     * @param amount amount
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, String id, int amount) {
        ItemStack IS = new ItemStack(Material.valueOf(id), amount);
        return takeItem(player, IS, amount);
    }

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, 0, 1, 1); #MESSAGE "Removed one granite."; ELSE; #MESSAGE "You don't have a granite"; ENDIF;
     * </p>
     *
     * @param player target player
     * @param id     item id
     * @param amount amount
     * @param data   data of item
     * @return true if took it; false if player doesn't have it
     * @deprecated use {@link #takeItem(Player, String, int, int)} instead
     */
    @Deprecated
    public boolean takeItem(Player player, int id, int amount, int data) {
        ItemStack IS = new ItemStack(id, amount, (short) data);
        return takeItem(player, IS, amount);
    }


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
    public boolean takeItem(Player player, String id, int amount, int data) {
        ItemStack IS = new ItemStack(Material.valueOf(id), amount, (short) data);
        return takeItem(player, IS, amount);
    }

    /**
     * get list of online players online
     * <p>
     * Example) /trg run FOR p = getPlayers(); p.performCommand("spawn"); ENDFOR;
     * </p>
     *
     * @return player iterator
     */
    public Collection<? extends Player> getPlayers() {
        return BukkitUtil.getOnlinePlayers();
    }

    @Override
    public PotionEffect makePotionEffect(String EffectType, int duration, int amplifier, boolean ambient,
                                         boolean particles) {
        PotionEffectType type = null;
        type = PotionEffectType.getByName(EffectType);

        if (type != null) {
            return new PotionEffect(type, duration, amplifier, ambient, particles);
        } else {
            return null;
        }
    }

    @Override
    public PotionEffect makePotionEffect(String EffectType, int duration, int amplifier, boolean ambient, boolean particles, Color color) {
        return makePotionEffect(EffectType, duration, amplifier, ambient, particles);
    }

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
     * @deprecated use {@link #item(String, int, int)} instead
     */
    @Deprecated
    public ItemStack item(int type, int amount, int data) {
        return new ItemStack(type, amount, (short) data);
    }

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item("STONE", 64, 0)
     * </p>
     *
     * @param type   typeId
     * @param amount amount of item
     * @param data   data
     * @return the ItemStack
     */
    public ItemStack item(String type, int amount, int data) {
        return new ItemStack(Material.valueOf(type), amount, (short) data);
    }

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
    @Deprecated
    public ItemStack item(int type, int amount) {
        return new ItemStack(type, amount);
    }

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item("STONE", 32)
     * </p>
     *
     * @param type   typeId
     * @param amount amount of item
     * @return the ItemStack
     */
    public ItemStack item(String type, int amount) {
        return item(type, amount, 0);
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
     * @param textureValue base64 encoded texture value
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
    public SerializableLocation serializeLocation(World world, double x, double y, double z){
        return new SerializableLocation(new Location(world, x, y, z));
    }

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
    public SerializableLocation serializeLocation(World world, double x, double y, double z, double yaw, double pitch){
        return new SerializableLocation(new Location(world, x, y, z, toFloat(yaw), toFloat(pitch)));
    }

    /**
     * Create a new SerializableLocation, which implements ConfigurationSerializable
     * <p>
     * Example) /trg run serialized = serializeLocation("wildw", 2, 13, 44.6, 13)
     * </p>
     *
     * @param loc The Location value
     * @return The SerializableLocation value
     */
    public SerializableLocation serializeLocation(Location loc){
        return new SerializableLocation(loc);
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
