package com.zachsthings.liftplates;

import com.zachsthings.liftplates.util.Point;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.Set;

/**
 * The contents of a {@link Lift}. This can be calculated whenever the lift is moved.
 */
public class LiftContents {
    private final Set<SpecialBlock> specialBlocks;
    private final Set<Point> locations;
    private final Set<Entity> entities;

    public LiftContents(Set<SpecialBlock> specialBlocks, Set<Point> locations, Set<Entity> entities) {
        this.specialBlocks = Collections.unmodifiableSet(specialBlocks);
        this.locations = Collections.unmodifiableSet(locations);
        this.entities = Collections.unmodifiableSet(entities);
    }

    public Set<SpecialBlock> getSpecialBlocks() {
        return specialBlocks;
    }

    public Set<Point> getBlocks() {
        return locations;
    }

    public Set<Entity> getEntities() {
        return entities;
    }
}
