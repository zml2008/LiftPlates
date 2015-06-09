package com.zachsthings.liftplates.specialblock;

import org.spongepowered.api.event.Event;

public interface SpecialBlockRegisterEvent extends Event {

    SpecialBlock getRegisteredBlock();
}
