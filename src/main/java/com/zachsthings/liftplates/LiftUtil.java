package com.zachsthings.liftplates;

import com.zachsthings.liftplates.util.Point;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Comparator;

/**
 * @author zml2008
 */
public final class LiftUtil {
    private LiftUtil() {}

    /**
     * The {@link BlockFace#NORTH}, {@link BlockFace#SOUTH}, {@link BlockFace#EAST}, and {@link BlockFace#WEST} BlockFaces, for easy iteration
     */
    public static final BlockFace[] NSEW_FACES = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    /**
     * Apply the given BlockFace to a new copy of the location
     *
     * @param loc The location to get a modified version of
     * @param face The face to offset by
     * @return A copy of {@code loc} modified by the given face offset
     */
    public static Location mod(Location loc, BlockFace face) {
        return loc.clone().add(face.getModX(), face.getModY(), face.getModZ());
    }

    public static boolean isPressurePlate(Material mat) {
        return mat == Material.STONE_PLATE
                || mat == Material.WOOD_PLATE;
    }

    /**
     * A {@link Comparator} instance that compares objects by their height
     */
    public static final Comparator<Point> POINT_Y_COMPARE = new Comparator<Point>() {
        public int compare(Point a, Point b) {
            return a.getY() - b.getY();
        }
    };

    /**
     * A {@link Comparator} instance that compares objects by their height
     */
    public static final Comparator<Point> POINT_Y_COMPARE_REVERSE = new Comparator<Point>() {
        public int compare(Point a, Point b) {
            return b.getY() - a.getY();
        }
    };
}
