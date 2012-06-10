package com.zachsthings.liftplates.util;

import org.bukkit.Material;
import org.bukkit.World;
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
        TOP_DOWN(new Comparator<Point>() {
            public int compare(Point a, Point b) {
                return BOTTOM_UP.getComparator().compare(b, a);
            }
        }),

        /**
         * Applies changes from the bottom up. This is most useful when filling an area.
         */
        BOTTOM_UP(new Comparator<Point>() {
            public int compare(Point a, Point b) {
                return a.getY() - b.getY();
            }
        }),
        ;
        private final Comparator<Point> comparator;

        private BlockOrder(Comparator<Point> comparator) {
            this.comparator = comparator;
        }

        public Comparator<Point> getComparator() {
            return comparator;
        }
    }

    private final BlockOrder order;
    private final World world;
    private final Map<Point, MaterialData> changesNormal;
    private final Map<Point, MaterialData> changesLast;

    public BlockQueue(World world, BlockOrder order) {
        this.world = world;
        this.order = order;
        changesNormal = new HashMap<Point, MaterialData>();
        changesLast = new HashMap<Point, MaterialData>();
    }

    public void set(Point point, MaterialData mat) {
        if (needsSideAttachment(mat.getItemType())) {
            changesLast.put(point, mat);
            changesNormal.remove(point);
        } else {
            changesNormal.put(point, mat);
            changesLast.remove(point);
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

        for (Map.Entry<Point, MaterialData> entry : changesNormalList) {
            MaterialData type = modifyMaterialData(entry.getValue());
            entry.getKey().getBlock(world).setTypeIdAndData(type.getItemTypeId(),
                    type.getData(), !setNoPhysics(type.getItemType()));
        }

        for (Map.Entry<Point, MaterialData> entry : changesLast.entrySet()) {
            MaterialData type = modifyMaterialData(entry.getValue());
            entry.getKey().getBlock(world).setTypeIdAndData(type.getItemTypeId(),
                    type.getData(), !setNoPhysics(type.getItemType()));
        }
    }

    protected MaterialData modifyMaterialData(MaterialData input) {
        return input;
    }

    /**
     * Returns whether the provided block requires an attachment to the side of another block
     * @param mat The material to check
     * @return Whether an attachment is required
     */
    private static boolean needsSideAttachment(Material mat) {
        return mat == Material.TORCH
                || mat == Material.REDSTONE_TORCH_OFF
                || mat == Material.REDSTONE_TORCH_ON
                || mat == Material.LADDER
                || mat == Material.PISTON_BASE
                || mat == Material.PISTON_STICKY_BASE
                || mat == Material.FIRE
                || mat == Material.WALL_SIGN
                || mat == Material.LEVER
                || mat == Material.TRAP_DOOR
                || mat == Material.FENCE_GATE;
    }

    /**
     * Returns whether pyhiscs should not be run at all when setting a block.
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
