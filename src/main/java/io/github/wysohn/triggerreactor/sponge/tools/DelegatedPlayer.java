package io.github.wysohn.triggerreactor.sponge.tools;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.manipulator.mutable.entity.StatisticData;
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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
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
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

public class DelegatedPlayer implements Player{
    private final CommandSource src;

    public DelegatedPlayer(CommandSource player) {
        super();
        this.src = player;
    }

    @Override
    public Location<World> getLocation() {
        return null;
    }

    @Override
    public Translation getTranslation() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return UUID.fromString("11111111-2222-4333-4444-555555555555");
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return null;
    }

    @Override
    public String getName() {
        return src.getName();
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return null;
    }

    @Override
    public World getWorld() {
        for(World world : Sponge.getServer().getWorlds()) {
            if(world.getDimension().getType() == DimensionTypes.OVERWORLD)
                return world;
        }

        return null;
    }

    @Override
    public String getIdentifier() {
        return src.getIdentifier();
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return null;
    }

    @Override
    public void sendMessage(Text message) {
        src.sendMessage(message);
    }

    @Override
    public int getContentVersion() {
        return -1;
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        return false;
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        src.sendMessage(message);
    }

    @Override
    public boolean validateRawData(DataView container) {
        return false;
    }

    @Override
    public FoodData getFoodData() {
        return null;
    }

    @Override
    public void sendMessage(TextTemplate template) {
        src.sendMessage(template);
    }

    @Override
    public Locale getLocale() {
        return src.getLocale();
    }

    @Override
    public GameProfile getProfile() {
        return null;
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        return null;
    }

    @Override
    public Optional<String> getFriendlyIdentifier() {
        return src.getFriendlyIdentifier();
    }

    @Override
    public Text getTeamRepresentation() {
        return null;
    }

    @Override
    public Optional<ItemStack> getHelmet() {
        return null;
    }

    @Override
    public boolean canEquip(EquipmentType type, ItemStack equipment) {
        return false;
    }

    @Override
    public void sendMessage(ChatType type, TextTemplate template) {
        src.sendMessage(template);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        return;
    }

    @Override
    public MutableBoundedValue<Integer> foodLevel() {
        return Sponge.getRegistry().getValueFactory().createBoundedValueBuilder(Keys.FOOD_LEVEL).actualValue(-1).build();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return null;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }

    @Override
    public void sendMessage(TextTemplate template, Map<String, TextElement> parameters) {
        src.sendMessage(template, parameters);
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {

    }

    @Override
    public void setHelmet(ItemStack helmet) {

    }

    @Override
    public HealthData getHealthData() {
        return null;
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.of(this);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {

    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        return null;
    }

    @Override
    public Set<Context> getActiveContexts() {
        return src.getActiveContexts();
    }

    @Override
    public MutableBoundedValue<Double> exhaustion() {
        return Sponge.getRegistry().getValueFactory().createBoundedValueBuilder(Keys.EXHAUSTION).actualValue(-1.0).build();
    }

    @Override
    public MutableBoundedValue<Double> health() {
        return Sponge.getRegistry().getValueFactory().createBoundedValueBuilder(Keys.HEALTH).actualValue(-1.0).build();
    }

    @Override
    public StatisticData getStatisticData() {
        return null;
    }

    @Override
    public void sendMessage(ChatType type, TextTemplate template, Map<String, TextElement> parameters) {
        src.sendMessage(template, parameters);
    }

    @Override
    public Optional<ItemStack> getChestplate() {
        return null;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return null;
    }

    @Override
    public boolean equip(EquipmentType type, ItemStack equipment) {
        return false;
    }

    @Override
    public void sendMessages(Text... messages) {
        src.sendMessages(messages);
    }

    @Override
    public EntityType getType() {
        return EntityTypes.PLAYER;
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {

    }

    @Override
    public EntitySnapshot createSnapshot() {
        return null;
    }

    @Override
    public void setChestplate(ItemStack chestplate) {

    }

    @Override
    public MutableBoundedValue<Double> maxHealth() {
        return Sponge.getRegistry().getValueFactory().createBoundedValueBuilder(Keys.MAX_HEALTH).actualValue(-1.0).build();
    }

    @Override
    public MutableBoundedValue<Double> saturation() {
        return Sponge.getRegistry().getValueFactory().createBoundedValueBuilder(Keys.SATURATION).actualValue(-1.0).build();
    }

    @Override
    public Random getRandom() {
        return new Random();
    }

    @Override
    public void sendMessages(Iterable<Text> messages) {
        src.sendMessages(messages);
    }

    @Override
    public <E> E require(Key<? extends BaseValue<E>> key) {
        return null;
    }

    @Override
    public void sendMessages(ChatType type, Text... messages) {
        src.sendMessages(messages);
    }

    @Override
    public boolean setLocation(Location<World> location) {
        return false;
    }

    @Override
    public Optional<ItemStack> getLeggings() {
        return Optional.empty();
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {

    }

    @Override
    public DamageableData getDamageableData() {
        return null;
    }

    @Override
    public MessageChannel getMessageChannel() {
        return src.getMessageChannel();
    }

    @Override
    public boolean isViewingInventory() {
        return false;
    }

    @Override
    public OptionalValue<EntitySnapshot> lastAttacker() {
        return null;
    }

    @Override
    public void sendMessages(ChatType type, Iterable<Text> messages) {
        src.sendMessages(messages);
    }

    @Override
    public void setLeggings(ItemStack leggings) {

    }

    @Override
    public boolean setLocationSafely(Location<World> location) {
        return false;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        src.setMessageChannel(channel);
    }

    @Override
    public Optional<Container> getOpenInventory() {
        return Optional.empty();
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {

    }

    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return null;
    }

    @Override
    public OptionalValue<Double> lastDamage() {
        return null;
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of(this);
    }

    @Override
    public Optional<ItemStack> getBoots() {
        return Optional.empty();
    }

    @Override
    public Optional<Container> openInventory(Inventory inventory) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Vector3d getHeadRotation() {
        return null;
    }

    @Override
    public <T extends DataManipulator<?, ?>> T require(Class<T> containerClass) {
        return null;
    }

    @Override
    public void setBoots(ItemStack boots) {

    }

    @Override
    public Vector3d getRotation() {
        return null;
    }

    @Override
    public boolean closeInventory() throws IllegalArgumentException {
        return false;
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch) {

    }

    @Override
    public SubjectCollection getContainingCollection() {
        return src.getContainingCollection();
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return null;
    }

    @Override
    public void setRotation(Vector3d rotation) {

    }

    @Override
    public void setHeadRotation(Vector3d rotation) {

    }

    @Override
    public SubjectReference asSubjectReference() {
        return null;
    }

    @Override
    public Optional<ItemStack> getItemInHand(HandType handType) {
        return Optional.empty();
    }

    @Override
    public int getViewDistance() {
        return -1;
    }

    @Override
    public boolean isSubjectDataPersisted() {
        return src.isSubjectDataPersisted();
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation) {
        return false;
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return null;
    }

    @Override
    public ChatVisibility getChatVisibility() {
        return null;
    }

    @Override
    public void setItemInHand(HandType hand, ItemStack itemInHand) {

    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {

    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public void lookAt(Vector3d targetPos) {

    }

    @Override
    public boolean isChatColorsEnabled() {
        return false;
    }

    @Override
    public Chat simulateChat(Text message, Cause cause) {
        return null;
    }

    @Override
    public SubjectData getSubjectData() {
        return src.getSubjectData();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation,
            EnumSet<RelativePositions> relativePositions) {
        return false;
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        return false;
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return src.getTransientSubjectData();
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch,
            double minVolume) {
    }

    @Override
    public DataHolder copy() {
        return null;
    }

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return null;
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return src.hasPermission(contexts, permission);
    }

    @Override
    public Set<Key<?>> getKeys() {
        return null;
    }

    @Override
    public PlayerConnection getConnection() {
        return null;
    }

    @Override
    public boolean setLocationAndRotationSafely(Location<World> location, Vector3d rotation) {
        return false;
    }

    @Override
    public void sendResourcePack(ResourcePack pack) {
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return null;
    }

    @Override
    public void stopSounds() {
    }

    @Override
    public void stopSounds(SoundType sound) {
    }

    @Override
    public TabList getTabList() {
        return null;
    }

    @Override
    public boolean hasPermission(String permission) {
        return src.hasPermission(permission);
    }

    @Override
    public void kick() {
    }

    @Override
    public void stopSounds(SoundCategory category) {
    }

    @Override
    public void kick(Text reason) {
    }

    @Override
    public void stopSounds(SoundType sound, SoundCategory category) {
    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return src.getPermissionValue(contexts, permission);
    }

    @Override
    public void playRecord(Vector3i position, RecordType recordType) {
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        return false;
    }

    @Override
    public boolean setLocationAndRotationSafely(Location<World> location, Vector3d rotation,
            EnumSet<RelativePositions> relativePositions) {
        return false;
    }

    @Override
    public JoinData getJoinData() {
        return null;
    }

    @Override
    public <E> DataTransactionResult transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return null;
    }

    @Override
    public void stopRecord(Vector3i position) {
    }

    @Override
    public Value<Instant> firstPlayed() {
        return null;
    }

    @Override
    public void sendTitle(Title title) {
    }

    @Override
    public void resetTitle() {
    }

    @Override
    public boolean isChildOf(SubjectReference parent) {
        return src.isChildOf(parent);
    }

    @Override
    public Value<Instant> lastPlayed() {
        return null;
    }

    @Override
    public void clearTitle() {
    }

    @Override
    public void sendBookView(BookView bookView) {
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public Vector3d getScale() {
        return null;
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        return null;
    }

    @Override
    public void sendBlockChange(Vector3i vec, BlockState state) {
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
        return src.isChildOf(contexts, parent);
    }

    @Override
    public void setScale(Vector3d scale) {
    }

    @Override
    public DisplayNameData getDisplayNameData() {
        return null;
    }

    @Override
    public Transform<World> getTransform() {
        return null;
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
    }

    @Override
    public GameModeData getGameModeData() {
        return null;
    }

    @Override
    public boolean setTransform(Transform<World> transform) {
        return false;
    }

    @Override
    public <E> DataTransactionResult tryOffer(Key<? extends BaseValue<E>> key, E value)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public Value<GameMode> gameMode() {
        return null;
    }

    @Override
    public void resetBlockChange(Vector3i vec) {
    }

    @Override
    public List<SubjectReference> getParents() {
        return src.getParents();
    }

    @Override
    public boolean setTransformSafely(Transform<World> transform) {
        return false;
    }

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        return src.getParents(contexts);
    }

    @Override
    public <E> DataTransactionResult offer(BaseValue<E> value) {
        return null;
    }

    @Override
    public boolean transferToWorld(World world) {
        return false;
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return src.getOption(contexts, key);
    }

    @Override
    public Inventory getEnderChestInventory() {
        return null;
    }

    @Override
    public boolean respawnPlayer() {
        return false;
    }

    @Override
    public <E> DataTransactionResult tryOffer(BaseValue<E> value) throws IllegalArgumentException {
        return null;
    }

    @Override
    public boolean transferToWorld(World world, Vector3d position) {
        return false;
    }

    @Override
    public Optional<Entity> getSpectatorTarget() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getOption(String key) {
        return src.getOption(key);
    }

    @Override
    public void setSpectatorTarget(Entity entity) {
    }

    @Override
    public boolean transferToWorld(String worldName, Vector3d position) {
        return false;
    }

    @Override
    public Optional<WorldBorder> getWorldBorder() {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer) {
        return null;
    }

    @Override
    public void setWorldBorder(WorldBorder border, Cause cause) {
    }

    @Override
    public CooldownTracker getCooldownTracker() {
        return null;
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        return null;
    }

    @Override
    public AdvancementProgress getProgress(Advancement advancement) {
        return null;
    }

    @Override
    public boolean transferToWorld(UUID uuid, Vector3d position) {
        return false;
    }

    @Override
    public Collection<AdvancementTree> getUnlockedAdvancementTrees() {
        return null;
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers) {
        return null;
    }

    @Override
    public Optional<AABB> getBoundingBox() {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers,
            MergeFunction function) {
        return null;
    }

    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    @Override
    public boolean hasPassenger(Entity entity) {
        return false;
    }

    @Override
    public boolean addPassenger(Entity entity) {
        return false;
    }

    @Override
    public void removePassenger(Entity entity) {
    }

    @Override
    public DataTransactionResult tryOffer(DataManipulator<?, ?> valueContainer) {
        return null;
    }

    @Override
    public void clearPassengers() {
    }

    @Override
    public Optional<Entity> getVehicle() {
        return Optional.empty();
    }

    @Override
    public boolean setVehicle(Entity entity) {
        return false;
    }

    @Override
    public Entity getBaseVehicle() {
        return null;
    }

    @Override
    public DataTransactionResult tryOffer(DataManipulator<?, ?> valueContainer, MergeFunction function)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public Vector3d getVelocity() {
        return null;
    }

    @Override
    public DataTransactionResult setVelocity(Vector3d vector3d) {
        return null;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void remove() {
    }

    @Override
    public boolean damage(double damage, DamageSource damageSource) {
        return false;
    }

    @Override
    public DataTransactionResult remove(BaseValue<?> value) {
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(double distance) {
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        return null;
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        return null;
    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.empty();
    }

    @Override
    public void setCreator(UUID uuid) {
    }

    @Override
    public void setNotifier(UUID uuid) {
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that) {
        return null;
    }

    @Override
    public boolean canSee(Entity entity) {
        return false;
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return null;
    }

    @Override
    public EntityArchetype createArchetype() {
        return null;
    }

    @Override
    public Value<Boolean> gravity() {
        return null;
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return null;
    }


}
