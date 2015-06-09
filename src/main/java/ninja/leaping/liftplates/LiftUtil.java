package ninja.leaping.liftplates;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.Direction;

/**
 * @author zml2008
 */
public final class LiftUtil {

    private LiftUtil() {
    }

    /**
     * The {@link Direction#NORTH}, {@link Direction#SOUTH}, {@link Direction#EAST}, and {@link Direction#WEST} Directions, for easy iteration
     */
    public static final Direction[] NSEW_FACES = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public static boolean isPressurePlate(BlockType mat) {
        return mat == BlockTypes.WOODEN_PRESSURE_PLATE
                || mat == BlockTypes.STONE_PRESSURE_PLATE
                || mat == BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE
                || mat == BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE;
    }

    /*public static Location matchLocation(CommandSender sender, String testString) throws CommandException {
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

    }*/
}
