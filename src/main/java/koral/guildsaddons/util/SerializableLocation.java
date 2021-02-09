package koral.guildsaddons.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableLocation {
    public String world;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public SerializableLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
    public static SerializableLocation fromLocation(Location loc) {
        return new SerializableLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
}
