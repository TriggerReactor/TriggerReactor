package io.github.wysohn.triggerreactor.sponge.tools;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.*;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.entity.*;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.message.MessageChannelEvent.Chat;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class TemporarilyPrivilegedPlayer implements Player {
    private final Player player;

    public TemporarilyPrivilegedPlayer(Player player) {
        super();
        this.player = player;
    }

    @Override
    public boolean addPassenger(Entity entity) {
        return player.addPassenger(entity);
    }

    @Override
    public SubjectReference asSubjectReference() {
        return player.asSubjectReference();
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        return player.canEquip(type);
    }

    @Override
    public boolean canEquip(EquipmentType type, ItemStack equipment) {
        return player.canEquip(type, equipment);
    }

    @Override
    public boolean canSee(Entity entity) {
        return player.canSee(entity);
    }

    @Override
    public void clearPassengers() {
        player.clearPassengers();
    }

    @Override
    public void clearTitle() {
        player.clearTitle();
    }

    @Override
    public boolean closeInventory() throws IllegalArgumentException {
        return player.closeInventory();
    }

    @Override
    public DataHolder copy() {
        return player.copy();
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that) {
        return player.copyFrom(that);
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return player.copyFrom(that, function);
    }

    @Override
    public EntityArchetype createArchetype() {
        return player.createArchetype();
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return player.createSnapshot();
    }

    @Override
    public boolean damage(double damage, DamageSource damageSource) {
        return player.damage(damage, damageSource);
    }

    @Override
    public boolean equip(EquipmentType type, ItemStack equipment) {
        return player.equip(type, equipment);
    }

    @Override
    public MutableBoundedValue<Double> exhaustion() {
        return player.exhaustion();
    }

    @Override
    public Value<Instant> firstPlayed() {
        return player.firstPlayed();
    }

    @Override
    public MutableBoundedValue<Integer> foodLevel() {
        return player.foodLevel();
    }

    @Override
    public Value<GameMode> gameMode() {
        return player.gameMode();
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return player.get(containerClass);
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return player.get(key);
    }

    @Override
    public Set<Context> getActiveContexts() {
        return player.getActiveContexts();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return player.getApplicableProperties();
    }

    @Override
    public Entity getBaseVehicle() {
        return player.getBaseVehicle();
    }

    @Override
    public Optional<ItemStack> getBoots() {
        return player.getBoots();
    }

    @Override
    public void setBoots(ItemStack boots) {
        player.setBoots(boots);
    }

    @Override
    public Optional<AABB> getBoundingBox() {
        return player.getBoundingBox();
    }

    @Override
    public ChatVisibility getChatVisibility() {
        return player.getChatVisibility();
    }

    @Override
    public Optional<ItemStack> getChestplate() {
        return player.getChestplate();
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        player.setChestplate(chestplate);
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return player.getCommandSource();
    }

    @Override
    public PlayerConnection getConnection() {
        return player.getConnection();
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return player.getContainers();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return player.getContainingCollection();
    }

    @Override
    public int getContentVersion() {
        return player.getContentVersion();
    }

    @Override
    public CooldownTracker getCooldownTracker() {
        return player.getCooldownTracker();
    }

    @Override
    public Optional<UUID> getCreator() {
        return player.getCreator();
    }

    @Override
    public void setCreator(UUID uuid) {
        player.setCreator(uuid);
    }

    @Override
    public DamageableData getDamageableData() {
        return player.getDamageableData();
    }

    @Override
    public DisplayNameData getDisplayNameData() {
        return player.getDisplayNameData();
    }

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return player.getDisplayedSkinParts();
    }

    @Override
    public Inventory getEnderChestInventory() {
        return player.getEnderChestInventory();
    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        return player.getEquipped(type);
    }

    @Override
    public FoodData getFoodData() {
        return player.getFoodData();
    }

    @Override
    public Optional<String> getFriendlyIdentifier() {
        return player.getFriendlyIdentifier();
    }

    @Override
    public GameModeData getGameModeData() {
        return player.getGameModeData();
    }

    @Override
    public Vector3d getHeadRotation() {
        return player.getHeadRotation();
    }

    @Override
    public void setHeadRotation(Vector3d rotation) {
        player.setHeadRotation(rotation);
    }

    @Override
    public HealthData getHealthData() {
        return player.getHealthData();
    }

    @Override
    public Optional<ItemStack> getHelmet() {
        return player.getHelmet();
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        player.setHelmet(helmet);
    }

    @Override
    public String getIdentifier() {
        return player.getIdentifier();
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return player.getInventory();
    }

    @Override
    public Optional<ItemStack> getItemInHand(HandType handType) {
        return player.getItemInHand(handType);
    }

    @Override
    public JoinData getJoinData() {
        return player.getJoinData();
    }

    @Override
    public Set<Key<?>> getKeys() {
        return player.getKeys();
    }

    @Override
    public Optional<ItemStack> getLeggings() {
        return player.getLeggings();
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        player.setLeggings(leggings);
    }

    @Override
    public Locale getLocale() {
        return player.getLocale();
    }

    @Override
    public Location<World> getLocation() {
        return player.getLocation();
    }

    @Override
    public MessageChannel getMessageChannel() {
        return player.getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        player.setMessageChannel(channel);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Collection<Entity> getNearbyEntities(double distance) {
        return player.getNearbyEntities(distance);
    }

    @Override
    public Collection<Entity> getNearbyEntities(Predicate<Entity> predicate) {
        return player.getNearbyEntities(predicate);
    }

    @Override
    public Optional<UUID> getNotifier() {
        return player.getNotifier();
    }

    @Override
    public void setNotifier(UUID uuid) {
        player.setNotifier(uuid);
    }

    @Override
    public Optional<Container> getOpenInventory() {
        return player.getOpenInventory();
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return player.getOption(contexts, key);
    }

    @Override
    public Optional<String> getOption(String key) {
        return player.getOption(key);
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return player.getOrCreate(containerClass);
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return player.getOrElse(key, defaultValue);
    }

    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return player.getOrNull(key);
    }

    @Override
    public List<SubjectReference> getParents() {
        return player.getParents();
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        return player.getParents(contexts);
    }

    @Override
    public List<Entity> getPassengers() {
        return player.getPassengers();
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return Tristate.TRUE;
    }

    @Override
    public Optional<Player> getPlayer() {
        return player.getPlayer();
    }

    @Override
    public Vector3d getPosition() {
        return player.getPosition();
    }

    @Override
    public GameProfile getProfile() {
        return player.getProfile();
    }

    @Override
    public AdvancementProgress getProgress(Advancement advancement) {
        return player.getProgress(advancement);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return player.getProperty(propertyClass);
    }

    @Override
    public Random getRandom() {
        return player.getRandom();
    }

    @Override
    public Vector3d getRotation() {
        return player.getRotation();
    }

    @Override
    public void setRotation(Vector3d rotation) {
        player.setRotation(rotation);
    }

    @Override
    public Vector3d getScale() {
        return player.getScale();
    }

    @Override
    public void setScale(Vector3d scale) {
        player.setScale(scale);
    }

    @Override
    public Scoreboard getScoreboard() {
        return player.getScoreboard();
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        player.setScoreboard(scoreboard);
    }

    @Override
    public Optional<Entity> getSpectatorTarget() {
        return player.getSpectatorTarget();
    }

    @Override
    public void setSpectatorTarget(Entity entity) {
        player.setSpectatorTarget(entity);
    }

    @Override
    public StatisticData getStatisticData() {
        return player.getStatisticData();
    }

    @Override
    public SubjectData getSubjectData() {
        return player.getSubjectData();
    }

    @Override
    public TabList getTabList() {
        return player.getTabList();
    }

    @Override
    public Text getTeamRepresentation() {
        return player.getTeamRepresentation();
    }

    @Override
    public Transform<World> getTransform() {
        return player.getTransform();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return player.getTransientSubjectData();
    }

    @Override
    public Translation getTranslation() {
        return player.getTranslation();
    }

    @Override
    public EntityType getType() {
        return player.getType();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public Collection<AdvancementTree> getUnlockedAdvancementTrees() {
        return player.getUnlockedAdvancementTrees();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return player.getValue(key);
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return player.getValues();
    }

    @Override
    public Optional<Entity> getVehicle() {
        return player.getVehicle();
    }

    @Override
    public Vector3d getVelocity() {
        return player.getVelocity();
    }

    @Override
    public int getViewDistance() {
        return player.getViewDistance();
    }

    @Override
    public World getWorld() {
        return player.getWorld();
    }

    @Override
    public Optional<WorldBorder> getWorldBorder() {
        return player.getWorldBorder();
    }

    @Override
    public Optional<UUID> getWorldUniqueId() {
        return player.getWorldUniqueId();
    }

    @Override
    public Value<Boolean> gravity() {
        return player.gravity();
    }

    @Override
    public boolean hasPassenger(Entity entity) {
        return player.hasPassenger(entity);
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean hasPlayedBefore() {
        return player.hasPlayedBefore();
    }

    @Override
    public MutableBoundedValue<Double> health() {
        return player.health();
    }

    @Override
    public boolean isChatColorsEnabled() {
        return player.isChatColorsEnabled();
    }

    @Override
    public boolean isChildOf(SubjectReference parent) {
        return player.isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
        return player.isChildOf(contexts, parent);
    }

    @Override
    public boolean isLoaded() {
        return player.isLoaded();
    }

    @Override
    public boolean isOnGround() {
        return player.isOnGround();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public boolean isRemoved() {
        return player.isRemoved();
    }

    @Override
    public boolean isSleepingIgnored() {
        return player.isSleepingIgnored();
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {
        player.setSleepingIgnored(sleepingIgnored);
    }

    @Override
    public boolean isSubjectDataPersisted() {
        return player.isSubjectDataPersisted();
    }

    @Override
    public boolean isViewingInventory() {
        return player.isViewingInventory();
    }

    @Override
    public void kick() {
        player.kick();
    }

    @Override
    public void kick(Text reason) {
        player.kick(reason);
    }

    @Override
    public OptionalValue<EntitySnapshot> lastAttacker() {
        return player.lastAttacker();
    }

    @Override
    public OptionalValue<Double> lastDamage() {
        return player.lastDamage();
    }

    @Override
    public Value<Instant> lastPlayed() {
        return player.lastPlayed();
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return player.launchProjectile(projectileClass);
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        return player.launchProjectile(projectileClass, velocity);
    }

    @Override
    public void lookAt(Vector3d targetPos) {
        player.lookAt(targetPos);
    }

    @Override
    public MutableBoundedValue<Double> maxHealth() {
        return player.maxHealth();
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        return player.offer(key, value);
    }

    @Override
    public <E> DataTransactionResult offer(BaseValue<E> value) {
        return player.offer(value);
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer) {
        return player.offer(valueContainer);
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        return player.offer(valueContainer, function);
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers) {
        return player.offer(valueContainers);
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers, MergeFunction function) {
        return player.offer(valueContainers, function);
    }

    @Override
    public Optional<Container> openInventory(Inventory inventory) throws IllegalArgumentException {
        return player.openInventory(inventory);
    }

    @Override
    public Optional<Container> openInventory(Inventory inventory, Text displayName) {
        return player.openInventory(inventory, displayName);
    }

    @Override
    public void playRecord(Vector3i position, RecordType recordType) {
        player.playRecord(position, recordType);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        player.playSound(sound, position, volume);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        player.playSound(sound, category, position, volume);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        player.playSound(sound, position, volume, pitch);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch) {
        player.playSound(sound, category, position, volume, pitch);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        player.playSound(sound, position, volume, pitch, minVolume);
    }

    @Override
    public void playSound(SoundType sound,
                          SoundCategory category,
                          Vector3d position,
                          double volume,
                          double pitch,
                          double minVolume) {
        player.playSound(sound, category, position, volume, pitch, minVolume);
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        return player.remove(containerClass);
    }

    @Override
    public void remove() {
        player.remove();
    }

    @Override
    public DataTransactionResult remove(BaseValue<?> value) {
        return player.remove(value);
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        return player.remove(key);
    }

    @Override
    public void removePassenger(Entity entity) {
        player.removePassenger(entity);
    }

    @Override
    public <E> E require(Key<? extends BaseValue<E>> key) {
        return player.require(key);
    }

    @Override
    public <T extends DataManipulator<?, ?>> T require(Class<T> containerClass) {
        return player.require(containerClass);
    }

    @Override
    public void resetBlockChange(Vector3i vec) {
        player.resetBlockChange(vec);
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        player.resetBlockChange(x, y, z);
    }

    @Override
    public void resetTitle() {
        player.resetTitle();
    }

    @Override
    public boolean respawnPlayer() {
        return player.respawnPlayer();
    }

    @Override
    public MutableBoundedValue<Double> saturation() {
        return player.saturation();
    }

    @Override
    public void sendBlockChange(Vector3i vec, BlockState state) {
        player.sendBlockChange(vec, state);
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        player.sendBlockChange(x, y, z, state);
    }

    @Override
    public void sendBookView(BookView bookView) {
        player.sendBookView(bookView);
    }

    @Override
    public void sendMessage(Text message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        player.sendMessage(type, message);
    }

    @Override
    public void sendMessage(TextTemplate template) {
        player.sendMessage(template);
    }

    @Override
    public void sendMessage(ChatType type, TextTemplate template) {
        player.sendMessage(type, template);
    }

    @Override
    public void sendMessage(TextTemplate template, Map<String, TextElement> parameters) {
        player.sendMessage(template, parameters);
    }

    @Override
    public void sendMessage(ChatType type, TextTemplate template, Map<String, TextElement> parameters) {
        player.sendMessage(type, template, parameters);
    }

    @Override
    public void sendMessages(Text... messages) {
        player.sendMessages(messages);
    }

    @Override
    public void sendMessages(Iterable<Text> messages) {
        player.sendMessages(messages);
    }

    @Override
    public void sendMessages(ChatType type, Text... messages) {
        player.sendMessages(type, messages);
    }

    @Override
    public void sendMessages(ChatType type, Iterable<Text> messages) {
        player.sendMessages(type, messages);
    }

    @Override
    public void sendResourcePack(ResourcePack pack) {
        player.sendResourcePack(pack);
    }

    @Override
    public void sendTitle(Title title) {
        player.sendTitle(title);
    }

    @Override
    public void setItemInHand(HandType hand, ItemStack itemInHand) {
        player.setItemInHand(hand, itemInHand);
    }

    @Override
    public boolean setLocation(Location<World> location) {
        return player.setLocation(location);
    }

    @Override
    public boolean setLocation(Vector3d position, UUID world) {
        return player.setLocation(position, world);
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation) {
        return player.setLocationAndRotation(location, rotation);
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location,
                                          Vector3d rotation,
                                          EnumSet<RelativePositions> relativePositions) {
        return player.setLocationAndRotation(location, rotation, relativePositions);
    }

    @Override
    public boolean setLocationAndRotationSafely(Location<World> location, Vector3d rotation) {
        return player.setLocationAndRotationSafely(location, rotation);
    }

    @Override
    public boolean setLocationAndRotationSafely(Location<World> location,
                                                Vector3d rotation,
                                                EnumSet<RelativePositions> relativePositions) {
        return player.setLocationAndRotationSafely(location, rotation, relativePositions);
    }

    @Override
    public boolean setLocationSafely(Location<World> location) {
        return player.setLocationSafely(location);
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        player.setRawData(container);
    }

    @Override
    public boolean setTransform(Transform<World> transform) {
        return player.setTransform(transform);
    }

    @Override
    public boolean setTransformSafely(Transform<World> transform) {
        return player.setTransformSafely(transform);
    }

    @Override
    public boolean setVehicle(Entity entity) {
        return player.setVehicle(entity);
    }

    @Override
    public DataTransactionResult setVelocity(Vector3d vector3d) {
        return player.setVelocity(vector3d);
    }

    @Override
    public void setWorldBorder(WorldBorder border, Cause cause) {
        player.setWorldBorder(border, cause);
    }

    @Override
    public Chat simulateChat(Text message, Cause cause) {
        return player.simulateChat(message, cause);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        player.spawnParticles(particleEffect, position);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        player.spawnParticles(particleEffect, position, radius);
    }

    @Override
    public void stopRecord(Vector3i position) {
        player.stopRecord(position);
    }

    @Override
    public void stopSounds() {
        player.stopSounds();
    }

    @Override
    public void stopSounds(SoundType sound) {
        player.stopSounds(sound);
    }

    @Override
    public void stopSounds(SoundCategory category) {
        player.stopSounds(category);
    }

    @Override
    public void stopSounds(SoundType sound, SoundCategory category) {
        player.stopSounds(sound, category);
    }

    @Override
    public boolean supports(Key<?> key) {
        return player.supports(key);
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        return player.supports(baseValue);
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        return player.supports(holderClass);
    }

    @Override
    public DataContainer toContainer() {
        return player.toContainer();
    }

    @Override
    public boolean transferToWorld(World world) {
        return player.transferToWorld(world);
    }

    @Override
    public boolean transferToWorld(World world, Vector3d position) {
        return player.transferToWorld(world, position);
    }

    @Override
    public boolean transferToWorld(String worldName, Vector3d position) {
        return player.transferToWorld(worldName, position);
    }

    @Override
    public boolean transferToWorld(UUID uuid, Vector3d position) {
        return player.transferToWorld(uuid, position);
    }

    @Override
    public <E> DataTransactionResult transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return player.transform(key, function);
    }

    @Override
    public <E> DataTransactionResult tryOffer(Key<? extends BaseValue<E>> key,
                                              E value) throws IllegalArgumentException {
        return player.tryOffer(key, value);
    }

    @Override
    public <E> DataTransactionResult tryOffer(BaseValue<E> value) throws IllegalArgumentException {
        return player.tryOffer(value);
    }

    @Override
    public DataTransactionResult tryOffer(DataManipulator<?, ?> valueContainer) {
        return player.tryOffer(valueContainer);
    }

    @Override
    public DataTransactionResult tryOffer(DataManipulator<?, ?> valueContainer,
                                          MergeFunction function) throws IllegalArgumentException {
        return player.tryOffer(valueContainer, function);
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        return player.undo(result);
    }

    @Override
    public boolean validateRawData(DataView container) {
        return player.validateRawData(container);
    }

}
