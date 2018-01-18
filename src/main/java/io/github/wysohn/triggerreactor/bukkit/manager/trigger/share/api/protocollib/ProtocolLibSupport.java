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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

public class ProtocolLibSupport extends APISupport {
    private String nmsVersion;

    private ProtocolManager protocolManager;

    public ProtocolLibSupport(TriggerReactor plugin) {
        super(plugin, "ProtocolLib");

        Plugin bukkitPlugin = plugin.getMain();
        String packageName = bukkitPlugin.getServer().getClass().getPackage().getName();
        nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    /**
     * Create empty packet.
     * @param packetType type of packet as String. Should match with any
     * <a href="https://aadnk.github.io/ProtocolLib/Javadoc/com/comphenix/protocol/package-frame.html">PacketType</a>.
     * For example, ENTITY is under PacketType.Play.Server.
     * @return
     */
    public PacketContainer createPacket(String packetType){
        ValidationUtil.notNull(packetType);

        Collection<PacketType> types = PacketType.fromName(packetType);
        if(types.isEmpty())
            throw new RuntimeException("Cannot find packet type "+packetType);

        PacketType type = types.iterator().next();
        return protocolManager.createPacket(type);
    }

    /**
     * create empty data watcher. Use this for fake entities.
     * @return
     */
    public WrappedDataWatcher createEmptyWatcher(){
        return new WrappedDataWatcher();
    }

    /**
     * send packet.
     * @param p player to send packet
     * @param container the packet
     * @throws InvocationTargetException
     */
    public void sendPacket(Player p, PacketContainer container) throws InvocationTargetException{
        ValidationUtil.notNull(p);
        ValidationUtil.notNull(container);

        ProtocolLibrary.getProtocolManager().sendServerPacket(p, container);
    }

    /**
     * Predefined method to spawn a fake entity. It may not work in versions older (perhaps newer) versions than 1.12
     * @param p player to send packet
     * @param entityId entity id to use. Usually you use negative number to distinguish entity id with real entities.
     * @param entityUuid uuid of entity
     * @param type type number of <a href="http://wiki.vg/Entities#Mobs">entity</a>.
     * @param x
     * @param y
     * @param z
     * @param yaw in degree
     * @param pitch in degree
     * @param headPitch in degree
     * @param velX See <a href="http://wiki.vg/Protocol#Entity_Velocity">this</a>
     * @param velY See <a href="http://wiki.vg/Protocol#Entity_Velocity">this</a>
     * @param velZ See <a href="http://wiki.vg/Protocol#Entity_Velocity">this</a>
     * @throws InvocationTargetException
     */
    public void sendEntitySpawn(Player p, int entityId, UUID entityUuid, int type,
            double x, double y, double z, double yaw, double pitch, double headPitch,
            int velX, int velY, int velZ) throws InvocationTargetException{
        PacketContainer container = createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING.name());

        container.getIntegers()
            .write(0, entityId)
            .write(1, type)
            .write(2, velX)
            .write(3, velY)
            .write(4, velZ);

        container.getUUIDs()
            .write(0, entityUuid == null ? UUID.randomUUID() : entityUuid);

        container.getDoubles()
            .write(0, x)
            .write(1, y)
            .write(2, z);

        container.getBytes()
            .write(0, (byte) (yaw * 256.0F / 360.0F))
            .write(1, (byte) (pitch * 256.0F / 360.0F))
            .write(2, (byte) (headPitch * 256.0F / 360.0F));

        container.getDataWatcherModifier()
            .write(0, createEmptyWatcher());

        this.sendPacket(p, container);
    }

    /**
     * Spawn fake entity without velocity set and with random UUID set.
     * @param p player to send packet
     * @param entityId entity id to use. Usually you use negative number to distinguish entity id with real entities.
     * @param type type number of <a href="http://wiki.vg/Entities#Mobs">entity</a>.
     * @param x
     * @param y
     * @param z
     * @param yaw in degree
     * @param pitch in degree
     * @param headPitch in degree
     * @throws InvocationTargetException
     */
    public void sendEntitySpawn(Player p, int entityId, int type,
            double x, double y, double z, double yaw, double pitch, double headPitch) throws InvocationTargetException{
        this.sendEntitySpawn(p, entityId, UUID.randomUUID(), type, x, y, z, yaw, pitch, headPitch, 0, 0, 0);
    }

    /**
     * Spawn entity at location
     * @param p player to send packet
     * @param entityId entity id to use. Usually you use negative number to distinguish entity id with real entities.
     * @param type type number of <a href="http://wiki.vg/Entities#Mobs">entity</a>.
     * @param location the Location
     * @throws InvocationTargetException
     */
    public void sendEntitySpawn(Player p, int entityId, int type,
            Location location) throws InvocationTargetException{
        this.sendEntitySpawn(p, entityId, type,
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.getPitch());
    }

    /**
     * Spawn entity at location
     * @param p player to send packet
     * @param entityId entity id to use. Usually you use negative number to distinguish entity id with real entities.
     * @param type type number of <a href="http://wiki.vg/Entities#Mobs">entity</a>.
     * @param x
     * @param y
     * @param z
     * @throws InvocationTargetException
     */
    public void sendEntitySpawn(Player p, int entityId, int type,
            double x, double y, double z) throws InvocationTargetException{
        this.sendEntitySpawn(p, entityId, type, x, y, z, 0, 0, 0);
    }

    /**
     * Spawn entity at player's current location.
     * @param p player to send packet
     * @param entityId entity id to use. Usually you use negative number to distinguish entity id with real entities.
     * @param type type number of <a href="http://wiki.vg/Entities#Mobs">entity</a>.
     * @throws InvocationTargetException
     */
    public void sendEntitySpawn(Player p, int entityId, int type) throws InvocationTargetException{
        this.sendEntitySpawn(p, entityId, type, p.getLocation());
    }

    /**
     * Send destroy packet for list of entities.
     * @param p player to send packet
     * @param entityId entity ids.
     * @throws InvocationTargetException
     */
    public void sendEntitiesDestroy(Player p, Object... entityId) throws InvocationTargetException{
        PacketContainer container = createPacket(PacketType.Play.Server.ENTITY_DESTROY.name());

        int[] intArray = new int[entityId.length];
        for(int i = 0; i < intArray.length; i++){
            if(!(entityId[i] instanceof Integer))
                throw new RuntimeException("Only integers are allowed in the array.");

            intArray[i] = (int) entityId[i];
        }

        container.getIntegerArrays().write(0, intArray);

        this.sendPacket(p, container);
    }

    /**
     * Send destroy packet for an entity.
     * @param p player to send packet
     * @param entityId entity id
     * @throws InvocationTargetException
     */
    public void sendEntityDestroy(Player p, int entityId) throws InvocationTargetException{
        sendEntitiesDestroy(p, new Object[]{entityId});
    }

    /**
     * send entity movement packet.
     * @param p player to send packet
     * @param entityId entity id of entity to move
     * @param dX delta x
     * @param dY delta y
     * @param dZ delta z
     * @param onGround no documentation
     * @throws InvocationTargetException
     */
    public void sendEntityMove(Player p, int entityId,
            int dX, int dY, int dZ,
            boolean onGround) throws InvocationTargetException{
        PacketContainer container = createPacket(PacketType.Play.Server.REL_ENTITY_MOVE.name());

        container.getIntegers()
            .write(0, entityId)
            .write(1, dX)
            .write(2, dY)
            .write(3, dZ);


        container.getBooleans().write(0, onGround);

        this.sendPacket(p, container);
    }

    /**
     * send entity movement packet.
     * @param p player to send packet
     * @param entityId entity id of entity to move
     * @param fromX
     * @param fromY
     * @param fromZ
     * @param toX
     * @param toY
     * @param toZ
     * @param onGround no documentation
     * @throws InvocationTargetException
     */
    public void sendEntityMove(Player p, int entityId,
            double fromX, double fromY, double fromZ,
            double toX, double toY, double toZ,
            boolean onGround) throws InvocationTargetException{

        int dX = (int) ((toX * 32 - fromX * 32) * 128);
        int dY = (int) ((toY * 32 - fromY * 32) * 128);
        int dZ = (int) ((toZ * 32 - fromZ * 32) * 128);

        this.sendEntityMove(p, entityId, dX, dY, dZ, onGround);
    }

    /**
     * Send entity look packet
     * @param p
     * @param entityId
     * @param yaw A rotation angle in steps of 1/256 of a full turn
     * @param pitch A rotation angle in steps of 1/256 of a full turn
     * @throws InvocationTargetException
     */
    public void sendEntityLook(Player p, int entityId, int yaw, int pitch) throws InvocationTargetException{
        PacketContainer container = createPacket(PacketType.Play.Server.ENTITY_LOOK.name());

        container.getIntegers().write(0, entityId);

        container.getBytes()
            .write(0, (byte) (yaw % 256))
            .write(1, (byte) (pitch % 256));

        this.sendPacket(p, container);

        container = createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION.name());

        container.getIntegers().write(0, entityId);

        container.getBytes()
            .write(0, (byte) (yaw % 256));

        this.sendPacket(p, container);
    }

    /**
     * Send entity look packet relative to the coordinates
     * @param p
     * @param entityId
     * @param fromX
     * @param fromY
     * @param fromZ
     * @param toX
     * @param toY
     * @param toZ
     * @throws InvocationTargetException
     */
    public void sendEntityLook(Player p, int entityId,
            double fromX, double fromY, double fromZ,
            double toX, double toY, double toZ) throws InvocationTargetException{
        double result[] = getAngle(fromX, fromY, fromZ, toX, toY, toZ);

        this.sendEntityLook(p, entityId, (int)result[0], (int)result[1]);
    }

    public double[] getAngle(double fromX, double fromY, double fromZ,
            double toX, double toY, double toZ){
        double[] result = new double[2];

        double relX = toX - fromX;
        double relY = toY - fromY;
        double relZ = toZ - fromZ;

        double r = Math.sqrt(relX*relX + relY*relY + relZ*relZ);

        double yaw = -Math.atan2(relX, relZ)/Math.PI*180;
        while(yaw < 0)
            yaw += 360;
        double pitch = -Math.asin(relY/r)/Math.PI*180;

        result[0] = yaw * 256/360;
        result[1] = pitch * 256/360;

        return result;
    }

    /**
     * Sends equip packet to player for specified entity.
     * @param p player to send packet
     * @param entityId the id of entity to equip item
     * @param slot the slot name. String must be same as one of the enum in {@link EnumItemSlot}
     * @param item the item to equip.
     * @throws ClassNotFoundException This throws if EnumItemSlot doesn't exist in nms package. This can be the case
     *  where you are trying to use this method in somewhere 1.8 version environment. It's advised to create your own with
     *  {@link #createPacket(String)} as PacketPlayoutEntityEquipment.class around 1.8 version specify everything using primitive types.
     * @throws InvocationTargetException
     */
    public void sendEntityEquip(Player p, int entityId, String slot, ItemStack item) throws ClassNotFoundException, InvocationTargetException{
        PacketContainer container = createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT.name());

        container.getIntegers().write(0, entityId);

        Class<?> clazz = Class.forName("net.minecraft.server."+nmsVersion+".EnumItemSlot");
        container.getEnumModifier(EnumItemSlot.class, clazz).write(0, EnumItemSlot.valueOf(slot));

        container.getItemModifier().write(0, item);

        this.sendPacket(p, container);
    }

    public enum EnumItemSlot{
        MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD;
    }
}
