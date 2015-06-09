package ninja.leaping.liftplates;

import com.google.inject.Inject;
import ninja.leaping.liftplates.specialblock.SpecialBlock;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zml2008
 */
public class LiftPlatesConfig {
    public static final ObjectMapper<LiftPlatesConfig> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(LiftPlatesConfig.class);
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Whether to treat a bunch of pressure plate lifts next to each other
     * with the same base block type as the same lift
     */
    @Setting("recursive-lifts") public boolean recursiveLifts = true;
    /**
     * Allow configuration of which block types that have special functionality
     */
    @Setting("special-blocks") public Map<BlockType, SpecialBlock> specialBlocks = new HashMap<BlockType, SpecialBlock>();

    /**
     * The maximum distance from the triggered pressure plate to look for blocks of the same type
     */
    @Setting("max-lift-size") public int maxLiftSize = 5;

    /**
     * How many blocks tall the lift should be. Setting to below 2 will prevent lifts from functioning,
     * since pressure plates will not be considered part of the lift when trying to move the lift.
     */
    @Setting("lift-height") public int liftHeight = 2;

    private ConfigurationNode config;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final File configDir;

    @Inject
    LiftPlatesConfig(@DefaultConfig(sharedRoot =  false) ConfigurationLoader<CommentedConfigurationNode> loader,
            @ConfigDir(sharedRoot = false) File configDir) {
        this.loader = loader;
        configDir.mkdirs();
        this.configDir = configDir;
    }

    public void load() throws IOException {
        this.config = this.loader.load();
        try {
            MAPPER.bind(this).populate(this.config);
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
        for (SpecialBlock block : SpecialBlock.getAll()) {
            if (!this.specialBlocks.containsValue(block)) {
                this.specialBlocks.put(block.getDefaultType(), block);
            }
        }
        this.loader.save(this.config);
    }

    public void save() throws IOException {
        if (this.config == null) {
            this.config = this.loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        try {
            MAPPER.bind(this).serialize(this.config);
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
        this.loader.save(this.config);

    }

    public File getConfigDir() {
        return this.configDir;
    }
}
