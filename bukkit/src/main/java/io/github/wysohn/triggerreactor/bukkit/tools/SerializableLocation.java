package io.github.wysohn.triggerreactor.bukkit.tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

@SerializableAs(value = "org.bukkit.Location")
public class SerializableLocation extends Location implements ConfigurationSerializable {

    public SerializableLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("world", this.getWorld().getName());

        data.put("x", this.getX());
        data.put("y", this.getY());
        data.put("z", this.getZ());

        data.put("yaw", this.getYaw());
        data.put("pitch", this.getPitch());

        return data;
    }

    public static SerializableLocation deserialize(Map<String, Object> args) {
        World world = Bukkit.getWorld((String) args.get("world"));
        if (world == null) {
            throw new IllegalArgumentException("unknown world");
        }

        return new SerializableLocation(new Location(world, NumberConversions.toDouble(args.get("x")),
                NumberConversions.toDouble(args.get("y")), NumberConversions.toDouble(args.get("z")),
                NumberConversions.toFloat(args.get("yaw")), NumberConversions.toFloat(args.get("pitch"))));
    }


}
