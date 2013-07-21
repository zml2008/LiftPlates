package com.zachsthings.liftplates.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.UUID;

/**
 * A Point with a built-in world
 */
public class WorldPoint extends Point {
    private final World world;
    public WorldPoint(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = world;
    }

    public WorldPoint(Location loc) {
        super(loc);
        this.world = loc.getWorld();
    }

    public Block getBlock() {
        return super.getBlock(world);
    }

    public Chunk getChunk() {
        return super.getChunk(world);
    }

    public Point toPoint() {
        return new Point(this.x, this.y, this.z);
    }

    public Location toLocation() {
        return super.toLocation(world);
    }

    public World getWorld() {
        return world;
    }

    public Point setWorld(World world) {
        return new WorldPoint(world, getY(), getY(), getZ());
    }

	@Override
	protected WorldPoint modified(int x, int y, int z) {
		return new WorldPoint(world, x, y, z);
	}

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> ret = super.serialize();
        ret.put("uuidLsv", world.getUID().getLeastSignificantBits());
        ret.put("uuidMsv", world.getUID().getMostSignificantBits());
        return ret;
    }

    public static WorldPoint deserialize(Map<String, Object> data) {
        Point parent = Point.deserialize(data);
        long lsv = ((Number) data.get("uuidLsv")).longValue();
        long msv = ((Number) data.get("uuidMsv")).longValue();
        World world = Bukkit.getServer().getWorld(new UUID(lsv, msv));
        if (world == null) {
            throw new IllegalArgumentException("Unknown world provided to WorldPoint!");
        }
        return new WorldPoint(world, parent.getX(), parent.getY(), parent.getZ());
    }
}
