package com.zachsthings.liftplates;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.Set;

/**
 * A snapshot of the lift's state and contents
 */
public class LiftSnapshot {
    private final Lift lift;
    private final Set<Block> changedBlocks;
    private final Set<Entity> movedEntities;

    public LiftSnapshot(Lift lift, Set<Block> changedBlocks, Set<Entity> movedEntities) {
        this.lift = lift;
        this.changedBlocks = Collections.unmodifiableSet(changedBlocks);
        this.movedEntities = Collections.unmodifiableSet(movedEntities);
    }

    public Lift getLift() {
        return lift;
    }

    public Set<Block> getChangedBlocks() {
        return changedBlocks;
    }

    public Set<Entity> getMovedEntities() {
        return movedEntities;
    }
}
