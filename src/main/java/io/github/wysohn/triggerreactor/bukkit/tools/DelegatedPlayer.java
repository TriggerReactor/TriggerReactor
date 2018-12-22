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
package io.github.wysohn.triggerreactor.bukkit.tools;

import java.net.InetSocketAddress;
import java.util.*;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.*;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class DelegatedPlayer implements Player {
    private final ConsoleCommandSender sender;
    public DelegatedPlayer(ConsoleCommandSender sender) {
        this.sender = sender;
    }
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }
    public boolean isOp() {
        return sender.isOp();
    }
    public boolean isConversing() {
        return sender.isConversing();
    }
    public boolean isPermissionSet(String name) {
        return sender.isPermissionSet(name);
    }
    public void sendMessage(String[] messages) {
        sender.sendMessage(messages);
    }
    public void setOp(boolean value) {
        sender.setOp(value);
    }
    public void acceptConversationInput(String input) {
        sender.acceptConversationInput(input);
    }
    public Server getServer() {
        return sender.getServer();
    }
    public boolean isPermissionSet(Permission perm) {
        return sender.isPermissionSet(perm);
    }
    public String getName() {
        return sender.getName();
    }
    public boolean beginConversation(Conversation conversation) {
        return sender.beginConversation(conversation);
    }
    public boolean hasPermission(String name) {
        return sender.hasPermission(name);
    }
    public void abandonConversation(Conversation conversation) {
        sender.abandonConversation(conversation);
    }
    public boolean hasPermission(Permission perm) {
        return sender.hasPermission(perm);
    }
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        sender.abandonConversation(conversation, details);
    }
    public void sendRawMessage(String message) {
        sender.sendRawMessage(message);
    }
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return sender.addAttachment(plugin, name, value);
    }
    public Spigot spigot() {
        return null;
    }
    public PermissionAttachment addAttachment(Plugin plugin) {
        return sender.addAttachment(plugin);
    }
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return sender.addAttachment(plugin, name, value, ticks);
    }
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return sender.addAttachment(plugin, ticks);
    }
    public void removeAttachment(PermissionAttachment attachment) {
        sender.removeAttachment(attachment);
    }
    public void recalculatePermissions() {
        sender.recalculatePermissions();
    }
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return sender.getEffectivePermissions();
    }
    public String getDisplayName() {
        return null;
    }
    public void setDisplayName(String name) {

    }
    public String getPlayerListName() {
        return null;
    }
    public void setPlayerListName(String s) {

    }
    public void setCompassTarget(Location location) {

    }
    public Location getCompassTarget() {
        return null;
    }
    public InetSocketAddress getAddress() {
        return null;
    }
    public void kickPlayer(String s) {

    }
    public void chat(String s) {

    }
    public boolean performCommand(String s) {
        return false;
    }
    public boolean isSneaking() {
        return false;
    }
    public void setSneaking(boolean b) {

    }
    public boolean isSprinting() {
        return false;
    }
    public void setSprinting(boolean b) {

    }
    public void saveData() {

    }
    public void loadData() {

    }
    public void setSleepingIgnored(boolean b) {

    }
    public boolean isSleepingIgnored() {
        return false;
    }
    public void playNote(Location location, byte b, byte b1) {

    }
    public void playNote(Location location, Instrument instrument, Note note) {

    }
    public void playSound(Location location, Sound sound, float v, float v1) {

    }
    public void playSound(Location location, String s, float v, float v1) {

    }
    public void playEffect(Location location, Effect effect, int i) {

    }
    public <T> void playEffect(Location location, Effect effect, T t) {

    }
    public void sendBlockChange(Location location, Material material, byte b) {

    }
    public boolean sendChunkChange(Location location, int i, int i1, int i2, byte[] bytes) {
        return false;
    }

    public void sendBlockChange(Location location, int i, byte b) {

    }
    public void sendSignChange(Location location, String[] strings) throws IllegalArgumentException {

    }
    public void sendMap(MapView mapView) {

    }
    public void updateInventory() {

    }
    public void awardAchievement(Achievement achievement) {

    }
    public void removeAchievement(Achievement achievement) {

    }
    public boolean hasAchievement(Achievement achievement) {
        return false;
    }
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }
    public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }
    public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }
    public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        return 0;
    }
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        return 0;
    }
    public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }
    public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }
    public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        return 0;
    }
    public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {

    }
    public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {

    }
    public void setStatistic(Statistic statistic, EntityType entityType, int i) {

    }
    public void setPlayerTime(long l, boolean b) {

    }
    public long getPlayerTime() {
        return 0;
    }
    public long getPlayerTimeOffset() {
        return 0;
    }
    public boolean isPlayerTimeRelative() {
        return false;
    }
    public void resetPlayerTime() {

    }
    public void setPlayerWeather(WeatherType weatherType) {

    }
    public WeatherType getPlayerWeather() {
        return null;
    }
    public void resetPlayerWeather() {

    }
    public void giveExp(int i) {

    }
    public void giveExpLevels(int i) {

    }
    public float getExp() {
        return 0;
    }
    public void setExp(float v) {

    }
    public int getLevel() {
        return 0;
    }
    public void setLevel(int i) {

    }
    public int getTotalExperience() {
        return 0;
    }
    public void setTotalExperience(int i) {

    }
    public float getExhaustion() {
        return 0;
    }
    public void setExhaustion(float v) {

    }
    public float getSaturation() {
        return 0;
    }
    public void setSaturation(float v) {

    }
    public int getFoodLevel() {
        return 0;
    }
    public void setFoodLevel(int i) {

    }
    public Location getBedSpawnLocation() {
        return null;
    }
    public void setBedSpawnLocation(Location location) {

    }
    public void setBedSpawnLocation(Location location, boolean b) {

    }
    public boolean getAllowFlight() {
        return false;
    }
    public void setAllowFlight(boolean b) {

    }
    public void hidePlayer(Player player) {

    }
    public void showPlayer(Player player) {

    }
    public boolean canSee(Player player) {
        return false;
    }
    public boolean isOnGround() {
        return false;
    }
    public boolean isFlying() {
        return false;
    }
    public void setFlying(boolean b) {

    }
    public void setFlySpeed(float v) throws IllegalArgumentException {

    }
    public void setWalkSpeed(float v) throws IllegalArgumentException {

    }
    public float getFlySpeed() {
        return 0;
    }
    public float getWalkSpeed() {
        return 0;
    }
    public void setTexturePack(String s) {

    }
    public void setResourcePack(String s) {

    }
    public Scoreboard getScoreboard() {
        return null;
    }
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {

    }
    public boolean isHealthScaled() {
        return false;
    }
    public void setHealthScaled(boolean b) {

    }
    public void setHealthScale(double v) throws IllegalArgumentException {

    }
    public double getHealthScale() {
        return 0;
    }
    public Entity getSpectatorTarget() {
        return null;
    }
    public void setSpectatorTarget(Entity entity) {

    }
    public void sendTitle(String s, String s1) {

    }
    public void resetTitle() {

    }
    public boolean isOnline() {
        return false;
    }
    public boolean isBanned() {
        return false;
    }

    public void setBanned(boolean b) {

    }
    public boolean isWhitelisted() {
        return false;
    }
    public void setWhitelisted(boolean b) {

    }
    public Player getPlayer() {
        return null;
    }
    public long getFirstPlayed() {
        return 0;
    }
    public long getLastPlayed() {
        return 0;
    }
    public boolean hasPlayedBefore() {
        return false;
    }
    public Map<String, Object> serialize() {
        return null;
    }
    public PlayerInventory getInventory() {
        return null;
    }
    public Inventory getEnderChest() {
        return null;
    }
    public boolean setWindowProperty(Property property, int i) {
        return false;
    }
    public InventoryView getOpenInventory() {
        return null;
    }
    public InventoryView openInventory(Inventory inventory) {
        return null;
    }
    public InventoryView openWorkbench(Location location, boolean b) {
        return null;
    }
    public InventoryView openEnchanting(Location location, boolean b) {
        return null;
    }
    public void openInventory(InventoryView inventoryView) {

    }
    public void closeInventory() {

    }
    public ItemStack getItemInHand() {
        return null;
    }
    public void setItemInHand(ItemStack itemStack) {

    }
    public ItemStack getItemOnCursor() {
        return null;
    }
    public void setItemOnCursor(ItemStack itemStack) {

    }
    public boolean isSleeping() {
        return false;
    }
    public int getSleepTicks() {
        return 0;
    }
    public GameMode getGameMode() {
        return null;
    }
    public void setGameMode(GameMode gameMode) {

    }
    public boolean isBlocking() {
        return false;
    }
    public int getExpToLevel() {
        return 0;
    }
    public double getEyeHeight() {
        return 0;
    }
    public double getEyeHeight(boolean b) {
        return 0;
    }
    public Location getEyeLocation() {
        return null;
    }

    public List<Block> getLineOfSight(HashSet<Byte> hashSet, int i) {
        return null;
    }
    public List<Block> getLineOfSight(Set<Material> set, int i) {
        return null;
    }

    public Block getTargetBlock(HashSet<Byte> hashSet, int i) {
        return null;
    }
    public Block getTargetBlock(Set<Material> set, int i) {
        return null;
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hashSet, int i) {
        return null;
    }
    public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) {
        return null;
    }

    public Egg throwEgg() {
        return null;
    }

    public Snowball throwSnowball() {
        return null;
    }

    public Arrow shootArrow() {
        return null;
    }
    public int getRemainingAir() {
        return 0;
    }
    public void setRemainingAir(int i) {

    }
    public int getMaximumAir() {
        return 0;
    }
    public void setMaximumAir(int i) {

    }
    public int getMaximumNoDamageTicks() {
        return 0;
    }
    public void setMaximumNoDamageTicks(int i) {

    }
    public double getLastDamage() {
        return 0;
    }
    public void setLastDamage(double v) {

    }
    public int getNoDamageTicks() {
        return 0;
    }
    public void setNoDamageTicks(int i) {

    }
    public Player getKiller() {
        return null;
    }
    public boolean addPotionEffect(PotionEffect potionEffect) {
        return false;
    }
    public boolean addPotionEffect(PotionEffect potionEffect, boolean b) {
        return false;
    }
    public boolean addPotionEffects(Collection<PotionEffect> collection) {
        return false;
    }
    public boolean hasPotionEffect(PotionEffectType potionEffectType) {
        return false;
    }
    public void removePotionEffect(PotionEffectType potionEffectType) {

    }
    public Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }
    public boolean hasLineOfSight(Entity entity) {
        return false;
    }
    public boolean getRemoveWhenFarAway() {
        return false;
    }
    public void setRemoveWhenFarAway(boolean b) {

    }
    public EntityEquipment getEquipment() {
        return null;
    }
    public void setCanPickupItems(boolean b) {

    }
    public boolean getCanPickupItems() {
        return false;
    }
    public boolean isLeashed() {
        return false;
    }
    public Entity getLeashHolder() throws IllegalStateException {
        return null;
    }
    public boolean setLeashHolder(Entity entity) {
        return false;
    }
    public void damage(double v) {

    }
    public void damage(double v, Entity entity) {

    }
    public double getHealth() {
        return 0;
    }
    public void setHealth(double v) {

    }
    public double getMaxHealth() {
        return 0;
    }
    public void setMaxHealth(double v) {

    }
    public void resetMaxHealth() {

    }
    public Location getLocation() {
        return null;
    }
    public Location getLocation(Location location) {
        return null;
    }
    public void setVelocity(Vector vector) {

    }
    public Vector getVelocity() {
        return null;
    }
    public World getWorld() {
        return null;
    }
    public boolean teleport(Location location) {
        return false;
    }
    public boolean teleport(Location location, TeleportCause teleportCause) {
        return false;
    }
    public boolean teleport(Entity entity) {
        return false;
    }
    public boolean teleport(Entity entity, TeleportCause teleportCause) {
        return false;
    }
    public List<Entity> getNearbyEntities(double v, double v1, double v2) {
        return null;
    }
    public int getEntityId() {
        return 0;
    }
    public int getFireTicks() {
        return 0;
    }
    public int getMaxFireTicks() {
        return 0;
    }
    public void setFireTicks(int i) {

    }
    public void remove() {

    }
    public boolean isDead() {
        return false;
    }
    public boolean isValid() {
        return false;
    }
    public Entity getPassenger() {
        return null;
    }
    public boolean setPassenger(Entity entity) {
        return false;
    }
    public boolean isEmpty() {
        return false;
    }
    public boolean eject() {
        return false;
    }
    public float getFallDistance() {
        return 0;
    }
    public void setFallDistance(float v) {

    }
    public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {

    }
    public EntityDamageEvent getLastDamageCause() {
        return null;
    }
    public UUID getUniqueId() {
        return null;
    }
    public int getTicksLived() {
        return 0;
    }
    public void setTicksLived(int i) {

    }
    public void playEffect(EntityEffect entityEffect) {

    }
    public EntityType getType() {
        return null;
    }
    public boolean isInsideVehicle() {
        return false;
    }
    public boolean leaveVehicle() {
        return false;
    }
    public Entity getVehicle() {
        return null;
    }
    public void setCustomName(String s) {

    }
    public String getCustomName() {
        return null;
    }
    public void setCustomNameVisible(boolean b) {

    }
    public boolean isCustomNameVisible() {
        return false;
    }
    public void setMetadata(String s, MetadataValue metadataValue) {

    }
    public List<MetadataValue> getMetadata(String s) {
        return null;
    }
    public boolean hasMetadata(String s) {
        return false;
    }
    public void removeMetadata(String s, Plugin plugin) {

    }
    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {

    }
    public Set<String> getListeningPluginChannels() {
        return null;
    }
    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) {
        return null;
    }


    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector) {
        return null;
    }

    public String getPlayerListHeader() {
        return null;
    }

    public String getPlayerListFooter() {
        return null;
    }

    public void setPlayerListHeader(String header) {

    }

    public void setPlayerListFooter(String footer) {

    }

    public void setPlayerListHeaderFooter(String header, String footer) {

    }

    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {

    }

    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {

    }

    public void stopSound(Sound sound) {

    }

    public void stopSound(String sound) {

    }

    public void stopSound(Sound sound, SoundCategory category) {

    }

    public void stopSound(String sound, SoundCategory category) {

    }

    public void sendBlockChange(Location loc, BlockData block) {

    }

    public void hidePlayer(Plugin plugin, Player player) {

    }

    public void showPlayer(Plugin plugin, Player player) {

    }

    public void setResourcePack(String url, byte[] hash) {

    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {

    }

    public void spawnParticle(Particle particle, Location location, int count) {

    }

    public void spawnParticle(Particle particle, double x, double y, double z, int count) {

    }

    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {

    }

    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {

    }

    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {

    }

    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {

    }

    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {

    }

    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {

    }

    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {

    }

    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {

    }

    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {

    }

    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {

    }

    public AdvancementProgress getAdvancementProgress(Advancement advancement) {
        return null;
    }

    public int getClientViewDistance() {
        return 0;
    }

    public String getLocale() {
        return null;
    }

    public void updateCommands() {

    }

    public MainHand getMainHand() {
        return null;
    }

    public InventoryView openMerchant(Villager trader, boolean force) {
        return null;
    }

    public InventoryView openMerchant(Merchant merchant, boolean force) {
        return null;
    }

    public boolean hasCooldown(Material material) {
        return false;
    }

    public int getCooldown(Material material) {
        return 0;
    }

    public void setCooldown(Material material, int ticks) {

    }

    public boolean isHandRaised() {
        return false;
    }

    public boolean discoverRecipe(NamespacedKey recipe) {
        return false;
    }

    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        return 0;
    }

    public boolean undiscoverRecipe(NamespacedKey recipe) {
        return false;
    }

    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        return 0;
    }

    public Entity getShoulderEntityLeft() {
        return null;
    }

    public void setShoulderEntityLeft(Entity entity) {

    }

    public Entity getShoulderEntityRight() {
        return null;
    }

    public void setShoulderEntityRight(Entity entity) {

    }

    public Block getTargetBlockExact(int maxDistance) {
        return null;
    }

    public Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    public RayTraceResult rayTraceBlocks(double maxDistance) {
        return null;
    }

    public RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    public PotionEffect getPotionEffect(PotionEffectType type) {
        return null;
    }

    public boolean isGliding() {
        return false;
    }

    public void setGliding(boolean gliding) {

    }

    public boolean isSwimming() {
        return false;
    }

    public void setSwimming(boolean swimming) {

    }

    public boolean isRiptiding() {
        return false;
    }

    public void setAI(boolean ai) {

    }

    public boolean hasAI() {
        return false;
    }

    public void setCollidable(boolean collidable) {

    }

    public boolean isCollidable() {
        return false;
    }

    public AttributeInstance getAttribute(Attribute attribute) {
        return null;
    }

    public double getHeight() {
        return 0;
    }

    public double getWidth() {
        return 0;
    }

    public BoundingBox getBoundingBox() {
        return null;
    }

    public boolean isPersistent() {
        return false;
    }

    public void setPersistent(boolean persistent) {

    }

    public List<Entity> getPassengers() {
        return null;
    }

    public boolean addPassenger(Entity passenger) {
        return false;
    }

    public boolean removePassenger(Entity passenger) {
        return false;
    }

    public void setGlowing(boolean flag) {

    }

    public boolean isGlowing() {
        return false;
    }

    public void setInvulnerable(boolean flag) {

    }

    public boolean isInvulnerable() {
        return false;
    }

    public boolean isSilent() {
        return false;
    }

    public void setSilent(boolean flag) {

    }

    public boolean hasGravity() {
        return false;
    }

    public void setGravity(boolean gravity) {

    }

    public int getPortalCooldown() {
        return 0;
    }

    public void setPortalCooldown(int cooldown) {

    }

    public Set<String> getScoreboardTags() {
        return null;
    }

    public boolean addScoreboardTag(String tag) {
        return false;
    }

    public boolean removeScoreboardTag(String tag) {
        return false;
    }

    public PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    public BlockFace getFacing() {
        return null;
    }
}
