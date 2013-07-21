package com.zachsthings.liftplates.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import java.util.*;

/**
 * A simple queue to work around MC physics issues when setting blocks
 */
public class BlockQueue {
    public static enum BlockOrder {
        /**
         * Applies changes from the top down. This is most useful when clearing an area.
         */
        TOP_DOWN(true, new Comparator<Point>() {
            public int compare(Point a, Point b) {
                return BOTTOM_UP.getComparator().compare(b, a);
            }
        }),

        /**
         * Applies changes from the bottom up. This is most useful when filling an area.
         */
        BOTTOM_UP(false, new Comparator<Point>() {
            public int compare(Point a, Point b) {
                return a.getY() - b.getY();
            }
        }),
        ;
        private final Comparator<Point> comparator;
        private final boolean changeLastFirst;

        private BlockOrder(boolean changeLastFirst, Comparator<Point> comparator) {
            this.changeLastFirst = changeLastFirst;
            this.comparator = comparator;
        }

        public Comparator<Point> getComparator() {
            return comparator;
        }

        public boolean shouldChangeLastFirst() {
            return changeLastFirst;
        }
    }

    private final BlockOrder order;
    private final World world;
    private final Map<Point, MaterialData> changesNormal = new HashMap<Point, MaterialData>();
    private final Map<Point, MaterialData> changesLast = new HashMap<Point, MaterialData>();
	private final Map<Point, NMSTileEntityInterface.TEntWrapper> tileEntityBlocks = new HashMap<Point, NMSTileEntityInterface.TEntWrapper>();

    public BlockQueue(World world, BlockOrder order) {
        this.world = world;
        this.order = order;
    }

	public void set(Point point, MaterialData mat) {
		set(point, mat, null);
	}

    public void set(Point point, MaterialData mat, NMSTileEntityInterface.TEntWrapper tentData) {
        MaterialData testMat = mat;
        if (mat.getItemType() == Material.AIR) {
            Block testBlock = point.getBlock(world);
            testMat = testBlock.getType().getNewData(testBlock.getData());
        }

        if (needsSideAttachment(testMat)) {
            changesLast.put(point, mat);
            changesNormal.remove(point);
        } else {
            changesNormal.put(point, mat);
            changesLast.remove(point);
        }

		if (tentData != null) {
			tileEntityBlocks.put(point, tentData);
		}
    }

    public void setAll(Map<Point, MaterialData> changes) {
        for (Map.Entry<Point, MaterialData> entry : changes.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public void apply() {
        List<Map.Entry<Point, MaterialData>> changesNormalList = new ArrayList<Map.Entry<Point, MaterialData>>(changesNormal.entrySet());
        Collections.sort(changesNormalList, new Comparator<Map.Entry<Point, MaterialData>>() {
            public int compare(Map.Entry<Point, MaterialData> a, Map.Entry<Point, MaterialData> b) {
                return order.getComparator().compare(a.getKey(), b.getKey());
            }
        });
        Iterable<Map.Entry<Point, MaterialData>> first = order.shouldChangeLastFirst() ? changesLast.entrySet() : changesNormalList;
        Iterable<Map.Entry<Point, MaterialData>> second = order.shouldChangeLastFirst() ? changesNormalList : changesLast.entrySet();


        for (Map.Entry<Point, MaterialData> entry : first) {
           applyBlockChange(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Point, MaterialData> entry : second) {
			applyBlockChange(entry.getKey(), entry.getValue());
        }
    }

	protected void applyBlockChange(Point pt, MaterialData mat) {
		MaterialData type = modifyMaterialData(mat);
		Block target = pt.getBlock(world);
		if (target.getType() == type.getItemType() && target.getData() == type.getData()) { // Only do things if the type is different
			return;
		}

		if (type.getItemType() == Material.AIR) { // Clear out inventory blocks so they don't drop items
			BlockState state = target.getState();
			if (state instanceof InventoryHolder) {
				((InventoryHolder) state).getInventory().clear();
			}
		}
		target.setTypeIdAndData(type.getItemTypeId(),
				type.getData(), !setNoPhysics(type.getItemType()));
		NMSTileEntityInterface.TEntWrapper tileEntityData = tileEntityBlocks.get(pt);
		if (tileEntityData != null) {
			NMSTileEntityInterface.applyData(tileEntityData, target);
		}
	}

    protected MaterialData modifyMaterialData(MaterialData input) {
        return input;
    }

    /**
     * Returns whether the provided block requires an attachment to the side of another block
     * @param data The material to check
     * @return Whether an attachment is required
     */
    private static boolean needsSideAttachment(MaterialData data) {
        if (data instanceof Attachable && ((Attachable) data).getAttachedFace() != BlockFace.DOWN) {
            return true;
        }
        Material mat = data.getItemType();
        return mat == Material.PISTON_BASE
                || mat == Material.PISTON_STICKY_BASE;
    }

    /**
     * Returns whether physics should not be run at all when setting a block.
     * This is only used for REALLY weird blocks.
     *
     * @param mat The material to check
     * @return Whether physics should be stopped when setting a block of this type
     */
    private static boolean setNoPhysics(Material mat) {
        return mat == Material.PISTON_EXTENSION
                || mat == Material.PISTON_MOVING_PIECE;
    }
}
