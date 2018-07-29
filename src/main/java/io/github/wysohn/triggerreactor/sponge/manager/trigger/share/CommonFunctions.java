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
package io.github.wysohn.triggerreactor.sponge.manager.trigger.share;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.boss.BossBar;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.AreaTriggerManager;
import io.github.wysohn.triggerreactor.sponge.tools.LocationUtil;

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
    public Object plugin(String name) {
        PluginContainer container = Sponge.getPluginManager().getPlugin(name).get();
        if (container == null)
            return null;

        return container.getInstance();
    }

    public ItemType itemType(String name) {
        return Sponge.getGame().getRegistry().getType(ItemType.class, name).get();
    }

    /**
     * take item from player.
     * <p>
     * Example) /trg run IF takeItem(player, 1, 1); #MESSAGE "Removed one
     * stone."; ELSE; #MESSAGE "You don't have a stone"; ENDIF;
     * </p>
     *
     * @param player
     *            target player
     * @param id
     *            item name. Sponge does not support numerical item id.
     * @param amount
     *            amount
     * @return true if took it; false if player doesn't have it
     */
    public boolean takeItem(Player player, String id, int amount) {
        ItemStack IS = ItemStack.of(itemType(id), amount);
        if (!player.getInventory().contains(IS))
            return false;

        Optional<ItemStack> result = player.getInventory().query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(IS))
                .peek(amount);
        if (result.isPresent() && result.get().getQuantity() < amount)
            return false;

        return player.getInventory().query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(IS)).poll(amount).get()
                .getQuantity() == amount;
    }

    public Location<World> location(String world, int x, int y, int z) {
        World w = Sponge.getServer().getWorld(world).get();
        if (world == null)
            throw new RuntimeException("world " + world + " does not exists!");
        return new Location<World>(w, x, y, z);
    }

    /**
     * check if two location are equal not considering their decimal points
     * <p>
     * Example) /trg run IF locationEqual(player.getLocation(),
     * {"otherLocation"}); #MESSAGE "match"; ENDIF;
     * </p>
     *
     * @param loc1
     * @param loc2
     * @return true if equal; false if not
     */
    public boolean locationEqual(Location<World> loc1, Location<World> loc2) {
        return loc1.getExtent().equals(loc2.getExtent()) && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * create a PotionEffect for use in entity.addPotionEffect();
     * <p>
     * Example) /trg run player.addPotionEffect( makePotionEffect("SPEED",
     * 1000000, 5, false, true, bukkitColor(21,2,24) ))
     * </p>
     *
     * @param EffectType
     *            the name of the PotionEffectType to use
     * @param duration
     *            how long the potioneffect should last when applied to an
     *            enitity
     * @param amplifier
     *            how strong the effect should be
     * @param ambient
     *            if true particle effects will be more transparent
     * @param particles
     *            if false potion particle effects will not be shown
     * @param color
     *            sets the color of the potion particle effects shown
     * @return returns a PotionEffect object or null if specified
     *         PotionEffectType was not found.
     */
    public PotionEffect makePotionEffect(String EffectType, int duration, int amplifier, boolean ambient,
            boolean particles, Color color) {
        PotionEffectType type = null;
        try {
            type = (PotionEffectType) PotionEffectTypes.class.getField(EffectType).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException(EffectType + " is not a valid PotionEffectType!");
        }

        if (type != null) {
            return PotionEffect.builder().duration(duration).amplifier(amplifier).ambience(ambient).particles(particles)
                    .potionType(type).build();
        } else {
            return null;
        }

    }

    /**
     * try to get a player from name. Mostly online player.
     * <p>
     * Example) player("wysohn").performCommand("spawn")
     * </p>
     *
     * @param name
     *            name of player
     * @return the Player object; it can be null if no player is found with the
     *         name
     */
    public Player player(String name) {
        return Sponge.getServer().getPlayer(name).get();
    }

    /**
     * try to get offline player from name.
     * <p>
     * Example) /trg run #MESSAGE "UUID is: "+oplayer("wysohn").getUniqueId()
     * </p>
     *
     * @param name
     *            name of player
     * @return the OfflinePlayer object; it never returns null but always return
     *         an offline player even if the player doesn't exist.
     */
    public User oplayer(String name) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(name).get();
    }

    /**
     * get list of online players online
     * <p>
     * Example) /trg run FOR p = getPlayers(); p.performCommand("spawn");
     * ENDFOR;
     * </p>
     *
     * @return player iterator
     */
    public Collection<? extends Player> getPlayers() {
        return Sponge.getServer().getOnlinePlayers();
    }

    /*
     * public static void main(String[] ar) throws ClassNotFoundException,
     * NoSuchMethodException, InstantiationException, IllegalArgumentException,
     * InvocationTargetException { CommonFunctions com = new
     * CommonFunctions(null); com.newInstance(Test.class.getName(), new
     * Child()); }
     *
     * public static class Parent{
     *
     * }
     *
     * public static class Child extends Parent{
     *
     * }
     *
     * public static class Test{ public Test(Parent parent) {
     *
     * } }
     */

    /**
     * Get the name of area where entity is currently standing on.
     * <p>
     * Example) /trg run #MESSAGE "You are in the AreaTrigger named:
     * "+currentArea(player)
     * </p>
     *
     * @param entity
     *            any entity(including player)
     * @return name of area; null if player is not on any area.
     */
    public String currentArea(Entity entity) {
        return currentAreaAt(entity.getLocation());
    }

    /**
     * Get the name of area trigger at the target location.
     *
     * @param location
     *            the location to check
     * @return name of area; null if there is no area trigger at location
     */
    public String currentAreaAt(Location<World> location) {
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        AreaTriggerManager.AreaTrigger trigger = areaManager.getArea(LocationUtil.convertToSimpleLocation(location));
        if (trigger == null)
            return null;

        return trigger.getTriggerName();
    }

    /**
     * Get list of entities tracked by this AreaTrigger.
     *
     * @param areaTriggerName
     *            name of AreaTrigger to get entities from
     * @return List of entities. null if the AreaTrigger with specified name
     *         doesn't exist.
     */
    public List<Entity> getEntitiesInArea(String areaTriggerName) {
        AbstractAreaTriggerManager areaManager = plugin.getAreaManager();
        AreaTriggerManager.AreaTrigger trigger = areaManager.getArea(areaTriggerName);
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
     * @param str
     *            unprocessed string
     * @return the sponge Text.
     */
    public Text color(String str) {
        return text(str);
    }

    /**
     * convert plain String to Sponge usable Text.
     * @param str String to convert
     * @return Text
     */
    public Text text(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    /**
     * creates and returns a bukkit color object using the RGB values given. the
     * max value for the int arguments is 255 exceeding it may cause errors.
     * <p>
     * Example) /trg run partColor = bukkitColor(255,255,255)
     * </p>
     *
     * @param int
     *            red the value of red in RGB
     * @param int
     *            green the value of green in RGB
     * @param int
     *            blue the value of blue in RGB
     * @return returns a Color object from org.bukkit.Color
     */
    public Color spongeColor(int red, int green, int blue) {
        Color color = null;
        color = Color.ofRgb(red, green, blue);
        return color;
    }

    /**
     * Create a new ItemStack
     * <p>
     * Example) /trg run #GIVE item(1, 64, 0)
     * </p>
     *
     * @param type
     *            type name. Sponge does not support numerical item types.
     * @param amount
     *            amount of item
     * @return the ItemStack
     */
    public ItemStack item(String type, int amount) {
        return ItemStack.of(itemType(type), amount);
    }

    /**
     * Get title of the specified ItemStack. Empty String if not exist.
     * <p>
     * Example) /trg run #MESSAGE "item name is:
     * "+getItemTitle(player.getItemInHand())
     * </p>
     *
     * @param IS
     * @return
     */
    public String getItemTitle(ItemStack IS) {
        if (!IS.get(Keys.DISPLAY_NAME).isPresent())
            return "";

        return IS.get(Keys.DISPLAY_NAME).get().toPlain();
    }

    /**
     * Set title of the specified ItemStack
     * <p>
     * Example) /trg run myitem = item(1, 1); setItemTitle(myitem, "I'm stone");
     * #GIVE myitem;
     * </p>
     *
     * @param IS
     * @param title
     */
    public void setItemTitle(ItemStack IS, String title) {
        setItemTitle(IS, TextSerializers.FORMATTING_CODE.deserialize(title));
    }

    public void setItemTitle(ItemStack IS, Text title) {
        IS.offer(Keys.DISPLAY_NAME, title);
    }

    /**
     * Check if the specified ItemStack contains the 'lore.' At least one is
     * contained in the lore will return true.
     *
     * @param IS
     * @param lore
     * @return true if 'lore' is in IS; false if not
     */
    public boolean hasLore(ItemStack IS, String lore) {
        if(!IS.get(Keys.ITEM_LORE).isPresent())
            return false;

        for(Text txt : IS.get(Keys.ITEM_LORE).get()) {
            if(txt.toPlain().equals(lore))
                return true;
        }

        return false;
    }

    /**
     * get Lore at the specified index
     *
     * @param IS
     * @param index
     * @return String of lore; null if not found
     */
    public String getLore(ItemStack IS, int index) {
        if(!IS.get(Keys.ITEM_LORE).isPresent())
            return null;

        List<Text> texts = IS.get(Keys.ITEM_LORE).get();

        return texts.get(index).toPlain();
    }

    /**
     * Append a lore to the specified ItemStack
     *
     * @param IS
     * @param lore
     */
    public void addLore(ItemStack IS, String lore) {
        List<Text> texts = IS.get(Keys.ITEM_LORE).get();
        if(texts == null)
            texts = new ArrayList<>();

        texts.add(Text.of(lore));

        IS.offer(Keys.ITEM_LORE, texts);
    }

    /**
     * Replace a lore at 'index' for the specified ItemStack
     *
     * @param IS
     * @param index
     * @param lore
     */
    public void setLore(ItemStack IS, int index, String lore) {
        setLore(IS, index, TextSerializers.FORMATTING_CODE.deserialize(lore));
    }

    public void setLore(ItemStack IS, int index, Text lore) {
        List<Text> texts = IS.get(Keys.ITEM_LORE).get();
        if(texts == null)
            texts = new ArrayList<>();

        texts.set(index, lore);

        IS.offer(Keys.ITEM_LORE, texts);
    }

    /**
     * Remove lore at the 'index' of the specified ItemStack
     *
     * @param IS
     * @param index
     */
    public void removeLore(ItemStack IS, int index) {
        List<Text> texts = IS.get(Keys.ITEM_LORE).get();
        if(texts == null)
            texts = new ArrayList<>();

        texts.remove(index);

        IS.offer(Keys.ITEM_LORE, texts);
    }

    /**
     * Clear all lores from item.
     *
     * @param IS
     */
    public void clearLore(ItemStack IS) {
        List<Text> texts = IS.get(Keys.ITEM_LORE).get();
        if(texts == null)
            texts = new ArrayList<>();

        texts.clear();

        IS.offer(Keys.ITEM_LORE, texts);
    }

    /**
     * get size of the lores for the specified ItemStack
     *
     * @param IS
     * @return
     */
    public int loreSize(ItemStack IS) {
        List<Text> texts = IS.get(Keys.ITEM_LORE).get();
        if(texts == null)
            texts = new ArrayList<>();

        return texts.size();
    }

    /**
     * Get the block snapshot which player is looking at
     * <p>
     * Example) /trg run #MESSAGE "looking at the block:
     * "+getTargetBlock(player, 30);
     * </p>
     *
     * @param player
     * @param maxDistance
     * @return block
     */
    public BlockState getTargetBlock(Player player, int maxDistance) {
        BlockRayHit<World> brh = BlockRay.from(player).distanceLimit(maxDistance).build().end().get();
        return brh.getExtent().getBlock(brh.getBlockPosition());
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
        return headForName(targetName, 1);
    }

    public ItemStack headForName(String targetName, int amount) {
        ItemStack IS = ItemStack.of(ItemTypes.SKULL, amount);
        IS.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);

        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Optional<User> user = userStorage.get().get(targetName);
        user.ifPresent((oplayer) -> {
            GameProfile profile = GameProfile.of(oplayer.getUniqueId(), targetName);
            IS.offer(Keys.REPRESENTED_PLAYER, profile);
        });

        return IS;
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
     * <b> Notice that there is an extra \ sign as \ is an escape sequence. The
     * final String will be ...OWQifX19\ instead of ...OWQifX19\\" </b>
     * </p>
     *
     * @param textureValue
     *            base64 encoded texture value
     * @return the ItemStack head
     */
    public ItemStack headForValue(String textureValue) {
        ItemStack IS = ItemStack.of(ItemTypes.SKULL, 1);
        IS.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);

        GameProfile profile = GameProfile.of(UUID.randomUUID(), "None");
        profile.addProperty(ProfileProperty.of("textures", textureValue));
        IS.offer(Keys.REPRESENTED_PLAYER, profile);

        return IS;
    }

    public BossBarColor barColor(String name) throws IllegalArgumentException, NoSuchFieldException {
        try {
            return (BossBarColor) BossBarColors.class.getField(name).get(null);
        } catch (IllegalAccessException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BossBar makeBossBar(String title, BossBarColor color, String style) {
        return ServerBossBar.builder().color(color).build();
    }
}
