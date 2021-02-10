package koral.guildsaddons.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableLocation that = (SerializableLocation) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0 &&
                Float.compare(that.yaw, yaw) == 0 && Float.compare(that.pitch, pitch) == 0 && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, yaw, pitch);
    }
}
