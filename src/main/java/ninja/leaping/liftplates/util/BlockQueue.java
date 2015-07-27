package ninja.leaping.liftplates.util;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.manipulator.block.AttachedData;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple queue to work around MC physics issues when setting blocks
 */
public class BlockQueue {

    public enum BlockOrder {
        /**
         * Applies changes from the top down. This is most useful when clearing an area.
         */
        TOP_DOWN(true, new Comparator<Vector3i>() {
            public int compare(Vector3i a, Vector3i b) {
                return BOTTOM_UP.getComparator().compare(b, a);
            }
        }),

        /**
         * Applies changes from the bottom up. This is most useful when filling an area.
         */
        BOTTOM_UP(false, new Comparator<Vector3i>() {
            public int compare(Vector3i a, Vector3i b) {
                return a.getY() - b.getY();
            }
        }),;
        private final Comparator<Vector3i> comparator;
        private final boolean changeLastFirst;

        BlockOrder(boolean changeLastFirst, Comparator<Vector3i> comparator) {
            this.changeLastFirst = changeLastFirst;
            this.comparator = comparator;
        }

        public Comparator<Vector3i> getComparator() {
            return comparator;
        }

        public boolean shouldChangeLastFirst() {
            return changeLastFirst;
        }
    }

    private final BlockOrder order;
    private final World world;
    private final Map<Vector3i, BlockSnapshot> changesNormal = new HashMap<Vector3i, BlockSnapshot>();
    private final Map<Vector3i, BlockSnapshot> changesLast = new HashMap<Vector3i, BlockSnapshot>();

    public BlockQueue(World world, BlockOrder order) {
        this.world = world;
        this.order = order;
    }

    public void set(Vector3i point, BlockSnapshot mat) { // TODO: Work with block snapshots?
        BlockState testMat = mat.getState();
        if (testMat.getType() == BlockTypes.AIR) {
            testMat = world.getBlock(point);
        }

        if (needsSideAttachment(testMat)) {
            changesLast.put(point, mat);
            changesNormal.remove(point);
        } else {
            changesNormal.put(point, mat);
            changesLast.remove(point);
        }
    }

    public void setAll(Map<Vector3i, BlockSnapshot> changes) {
        for (Map.Entry<Vector3i, BlockSnapshot> entry : changes.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public void apply() {
        List<Map.Entry<Vector3i, BlockSnapshot>> changesNormalList = new ArrayList<Map.Entry<Vector3i, BlockSnapshot>>(changesNormal.entrySet());
        Collections.sort(changesNormalList, new Comparator<Map.Entry<Vector3i, BlockSnapshot>>() {
            public int compare(Map.Entry<Vector3i, BlockSnapshot> a, Map.Entry<Vector3i, BlockSnapshot> b) {
                return order.getComparator().compare(a.getKey(), b.getKey());
            }
        });
        Iterable<Map.Entry<Vector3i, BlockSnapshot>> first = order.shouldChangeLastFirst() ? changesLast.entrySet() : changesNormalList;
        Iterable<Map.Entry<Vector3i, BlockSnapshot>> second = order.shouldChangeLastFirst() ? changesNormalList : changesLast.entrySet();


        for (Map.Entry<Vector3i, BlockSnapshot> entry : first) {
            applyBlockChange(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Vector3i, BlockSnapshot> entry : second) {
            applyBlockChange(entry.getKey(), entry.getValue());
        }
    }

    protected void applyBlockChange(Vector3i pt, BlockSnapshot mat) {
        mat.setBlockState(modifyBlockState(mat.getState()));
        /*if (mat.getState().getType().equals(BlockTypes.AIR)) {
            TileEntity stateEnt = null; // TODO: Add necessary API
            if (stateEnt instanceof TileEntityCarrier) {
                ((TileEntityCarrier) stateEnt).getInventory().clear();
            }
        }*/
        world.setBlockSnapshot(pt, mat);
    }

    protected BlockState modifyBlockState(BlockState input) {
        return input;
    }

    /**
     * Returns whether the provided block requires an attachment to the side of another block
     * @param data The material to check
     * @return Whether an attachment is required
     */
    private static boolean needsSideAttachment(BlockState data) {
        return data.getManipulator(AttachedData.class).isPresent()
                || data.getType() == BlockTypes.PISTON;
    }

    /**
     * Returns whether physics should not be run at all when setting a block.
     * This is only used for REALLY weird blocks.
     *
     * @param mat The material to check
     * @return Whether physics should be stopped when setting a block of this type
     */
    private static boolean setNoPhysics(BlockType mat) {
        return mat == BlockTypes.PISTON_EXTENSION
                || mat == BlockTypes.PISTON_HEAD;
    }
}
