package com.zachsthings.liftplates;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author zml2008
 */
public class LiftPlatesPlugin extends JavaPlugin {
    private LiftPlatesState plateState;
    private LiftManager manager;
    private LiftPlatesConfig config = new LiftPlatesConfig();
    @Override
    public void onEnable() {
        plateState = new LiftPlatesState(this);
        manager = new LiftManager(this);
        config.load(getConfig());
        saveConfig();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, plateState, LiftPlatesState.RUN_FREQUENCY, LiftPlatesState.RUN_FREQUENCY);
    }

    public void reload() {
        reloadConfig();
        config.load(getConfig());
    }

    public LiftPlatesState getPlateState() {
        return plateState;
    }
}
