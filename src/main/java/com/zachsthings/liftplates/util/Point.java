package com.zachsthings.liftplates.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a point in the world
 */
@SerializableAs("Point")
public class Point implements ConfigurationSerializable {
    protected final int x, y, z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(Location loc) {
        this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Point setX(int x) {
        return new Point(x, this.y, this.z);
    }

    public Point setY(int y) {
        return new Point(this.x, y, this.z);
    }

    public Point setZ(int z) {
        return new Point(this.x, this.y, z);
    }

    public Point modify(BlockFace face, int count) {
        return new Point(this.x + face.getModX() * count, this.y + face.getModY() * count, this.z + face.getModZ() * count);
    }

    public Point modify(BlockFace face) {
        return modify(face, 1);
    }

    public Point add(int x, int y, int z) {
        return new Point(this.x + x, this.y + y, this.z + z);
    }

    public int distanceSquared(Point other) {
        return (other.x - this.x) * (other.x - this.x) + (other.y - this.y) * (other.y - this.y) + (other.z - this.z) * (other.z - this.z);
    }

    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    public Block getBlock(World world) {
        return world.getBlockAt(this.x, this.y, this.z);
    }

    public Chunk getChunk(World world) {
        return world.getChunkAt(this.x >> 4, this.z >> 4);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) {
            return false;
        }

        Point other = (Point) obj;
        return this.x == other.x
                && this.y == other.y
                && this.z == other.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "Point{x=" + this.x + ",y=" + this.y + ",z=" + this.z + "}";
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("x", this.x);
        ret.put("y", this.y);
        ret.put("z", this.z);
        return ret;
    }

    public static Point deserialize(Map<String, Object> data) {
        final int x = ((Number) data.get("x")).intValue();
        final int y = ((Number) data.get("y")).intValue();
        final int z = ((Number) data.get("z")).intValue();
        return new Point(x, y, z);
    }
}
