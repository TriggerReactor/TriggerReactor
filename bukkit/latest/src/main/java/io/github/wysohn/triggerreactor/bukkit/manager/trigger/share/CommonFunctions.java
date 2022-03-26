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
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class CommonFunctions extends AbstractCommonFunctions implements SelfReference {

    @Override
    public Collection<? extends Player> getPlayers() {
        return BukkitUtil.getOnlinePlayers();
    }

    @Override
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

    @Override
    @Deprecated
    public ItemStack item(int type, int amount, int data) {
        throw new UnsupportedOperationException(
                "Cannot use numeric value for type since 1.13. Use appropriate Material value.");
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack item(String type, int amount, int data) {
        return new ItemStack(Material.valueOf(type), amount, (short) data);
    }

    @Override
    @Deprecated
    public ItemStack item(int type, int amount) {
        throw new UnsupportedOperationException(
                "Cannot use numeric value for type since 1.13. Use appropriate Material value.");
    }

    @Override
    public ItemStack item(String type, int amount) {
        return item(type, amount, 0);
    }

    @Override
    public PotionEffect makePotionEffect(String EffectType,
                                         int duration,
                                         int amplifier,
                                         boolean ambient,
                                         boolean particles,
                                         Color color) {
        return makePotionEffect(EffectType, duration, amplifier, ambient, particles);
    }

    @Override
    public PotionEffect makePotionEffect(String EffectType,
                                         int duration,
                                         int amplifier,
                                         boolean ambient,
                                         boolean particles) {
        PotionEffectType type = null;
        type = PotionEffectType.getByName(EffectType);

        if (type != null) {
            return new PotionEffect(type, duration, amplifier, ambient, particles);
        } else {
            return null;
        }
    }

    /**
     * @deprecated Location itself implements ConfigurationSerializable on latest versions.
     */
    @Deprecated
    @Override
    public SerializableLocation serializeLocation(World world, double x, double y, double z) {
        return new SerializableLocation(new Location(world, x, y, z));
    }

    /**
     * @deprecated Location itself implements @ConfigurationSerializable on latest versions.
     */
    @Deprecated
    @Override
    public SerializableLocation serializeLocation(World world, double x, double y, double z, double yaw, double pitch) {
        return new SerializableLocation(new Location(world, x, y, z, toFloat(yaw), toFloat(pitch)));
    }

    /**
     * @deprecated Location itself implements ConfigurationSerializable on latest versions.
     */
    @Deprecated
    @Override
    public SerializableLocation serializeLocation(Location loc) {
        return new SerializableLocation(loc);
    }

    @Override
    @Deprecated
    public boolean takeItem(Player player, int id, int amount) {
        throw new UnsupportedOperationException(
                "Cannot use numeric value for id since 1.13. Use appropriate Material value.");
    }

    @Override
    public boolean takeItem(Player player, String id, int amount) {
        ItemStack IS = new ItemStack(Material.valueOf(id), amount);
        return takeItem(player, IS, amount);
    }

    @Override
    @Deprecated
    public boolean takeItem(Player player, int id, int amount, int data) {
        throw new UnsupportedOperationException(
                "Cannot use numeric value for id since 1.13. Use appropriate Material value.");
    }

    @Override
    public boolean takeItem(Player player, String id, int amount, int data) {
        @SuppressWarnings("deprecation") ItemStack IS = new ItemStack(Material.valueOf(id), amount, (short) data);
        return takeItem(player, IS, amount);
    }

    @Override
    public ItemStack headForName(String targetName, int amount) {
        OfflinePlayer offp = oplayer(targetName);
        ItemStack head = BukkitUtil.getPlayerHeadItem();
        head.setAmount(amount);
        ItemMeta IM = head.getItemMeta();
        SkullMeta SM = (SkullMeta) IM;
        SM.setOwningPlayer(offp);
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
//            BarObj = (BossBar) ReflectionUtil.invokeMethod(Bukkit.class, (Object) null, "createBossBar", title,
//            colorEnum, styleEnum);
//        } catch (NoSuchMethodException e) {
//            return null;
//        } catch (InvocationTargetException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        return BarObj;
//    }
}
