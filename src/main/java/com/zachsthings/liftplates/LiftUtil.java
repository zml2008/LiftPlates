package com.zachsthings.liftplates;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                || mat == Material.WOOD_PLATE
				|| mat == Material.GOLD_PLATE
				|| mat == Material.IRON_PLATE;
    }

    public static Location matchLocation(CommandSender sender, String testString) throws CommandException {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (testString.equalsIgnoreCase("target")) {
                return player.getTargetBlock(null, 300).getLocation();
            } else if (testString.equalsIgnoreCase("pos")
                    || testString.equalsIgnoreCase("cur")
                    || testString.equalsIgnoreCase("current")) {
                return player.getLocation();
            }
        }
        String[] worldSplit = testString.split(":");
        World world;
        if (worldSplit.length == 1) {
            if (!(sender instanceof Player)) {
                throw new CommandException("No player or world provided for location!");
            }
            world = ((Player) sender).getWorld();
        } else {
            world = Bukkit.getServer().getWorld(worldSplit[0]);
            if (world == null) {
                throw new CommandException("Unknown world specified: " + worldSplit[0]);
            }
        }

        String[] pointSplit = worldSplit[worldSplit.length - 1].split(",");
        if (pointSplit.length != 3) {
            throw new CommandException("The third dimension, do you get it? Three points are required to specify a location");
        }
        int x = Integer.parseInt(pointSplit[0]);
        int y = Integer.parseInt(pointSplit[1]);
        int z = Integer.parseInt(pointSplit[2]);
        return new Location(world, x, y, z);

    }
}
