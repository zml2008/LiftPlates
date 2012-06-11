package com.zachsthings.liftplates.specialblock;


import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpecialBlockRegisterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final SpecialBlock registeredBlock;

    public SpecialBlockRegisterEvent(SpecialBlock registeredBlock) {
        this.registeredBlock = registeredBlock;
    }

    public SpecialBlock getRegisteredBlock() {
        return registeredBlock;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
