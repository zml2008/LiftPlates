package ninja.leaping.liftplates;

import static ninja.leaping.liftplates.util.MessageFormatting.normal;
import static org.spongepowered.api.util.command.args.GenericArguments.enumValue;
import static org.spongepowered.api.util.command.args.GenericArguments.location;
import static org.spongepowered.api.util.command.args.GenericArguments.world;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import ninja.leaping.liftplates.specialblock.SpecialBlock;
import ninja.leaping.liftplates.util.SpecialBlockTypeSerializer;
import ninja.leaping.liftplates.util.Vector3iTypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import ninja.leaping.liftplates.util.MessageFormatting;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * @author zml2008
 */
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class LiftPlatesPlugin {
    static {
        TypeSerializers.registerSerializer(new SpecialBlockTypeSerializer());
        TypeSerializers.registerSerializer(new Vector3iTypeSerializer());
    }
    @Inject private Game game;
    @Inject private Logger logger;
    @Inject private LiftPlatesConfig config;
    private LiftRunner liftRunner;
    private final Map<UUID, LiftManager> liftManagers = new HashMap<UUID, LiftManager>();

    @Subscribe
    public void onEnable(PreInitializationEvent event) {
        /*ItemStack specialToolStack = game.getRegistry().getItemBuilder()
                .itemType(ItemTypes.BLAZE_ROD)
                .itemData(game.getRegistry().getManipulatorRegistry().getBuilder(DisplayNameData.class).get().create().setCustomNameVisible(true)
                        .setDisplayName(Texts.of(TextColors.GOLD, "Lift Creator")))
                .itemData(game.getRegistry().getManipulatorRegistry().getBuilder(LoreData.class).get().create().add(Texts.of(TextColors.GRAY,
                        "Left-click to create an up lift\n"
                        + "Right-click to create a down lift")))
        .build();*/

        game.getEventManager().register(this, new LiftPlatesListener(this));

        game.getCommandDispatcher().register(this, CommandSpec.builder()
                .child(CommandSpec.builder()
                        .permission("liftplates.version")
                        .executor(new CommandExecutor() {
                            @Override
                            public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                                src.sendMessage(normal(Texts.of(PomData.NAME, " version ", PomData.VERSION)));
                                return CommandResult.success();
                            }
                        }).build(), "version", "v")
                .child(CommandSpec.builder()
                        .permission("liftplates.reload")
                        .executor(new CommandExecutor() {
                            @Override
                            public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                                try {
                                    reload();
                                    src.sendMessage(normal(Texts.of(PomData.NAME, " successfully reloaded")));
                                } catch (Throwable t) {
                                    logger.error("Error while reloading (executed by" + src.getName() + ")", t);
                                    throw new CommandException(Texts.of("Error while reloading " + PomData.NAME + ": " + t.getMessage(),
                                            Texts.of(TextColors.DARK_RED, "See console for more details")));
                                }
                                return CommandResult.success();
                            }
                        }).build(), "reload", "rel", "r")
                .build(), "liftplates", "liftpl", "lp");

        game.getCommandDispatcher().register(this, CommandSpec.builder()
        .permission("liftplates.lift.new.command")
                .description(Texts.of("Create a new lift"))
                .arguments(location(Texts.of("location"), game), enumValue(Texts.of("direction"), Direction.class))
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        Location loc = args.<Location>getOne("location").get();
                        Direction direction = args.<Direction>getOne("direction").get();
                        Lift lift = getLiftManager(loc.getExtent()).getOrAddLift(loc.getBlockPosition());
                        if (lift == null) {
                            throw new CommandException(Texts.of("No pressure plate at the specified location!"));
                        }

                        lift.setDirection(direction);

                        src.sendMessage(MessageFormatting.normal(Texts.of("Lift successfully created!")));
                        return CommandResult.success();
                    }
                })
        .build(), "mklift", "addlift", "createlift");

        game.getCommandDispatcher().register(this, CommandSpec.builder()
        .permission("liftplates.lift.exists")
        .description(Texts.of("Shows whether a certain point has a lift"))
        .arguments(location(Texts.of("location"), game))
        .executor(new CommandExecutor() {
            @Override
            public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                Location testLoc = args.<Location>getOne("location").get();
                Lift lift = getLiftManager(testLoc.getExtent()).getLift(testLoc.getBlockPosition());
                if (lift == null) {
                    throw new CommandException(Texts.of("There is no lift at the specified location!"));
                } else {
                    src.sendMessage(normal(Texts.of("There is a lift at " + lift.getPosition() + " moving " + lift.getDirection())));
                }
                return CommandResult.success();
            }
        }).build(), "islift", "liftexists", "haslift");

        game.getCommandDispatcher().register(this, CommandSpec.builder()
                .permission("liftplates.lift.list")
                .description(Texts.of("List the lifts in <world>"))
                .arguments(world(Texts.of("world"), game))
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        WorldProperties world = args.<WorldProperties>getOne("world").get();
                        Optional<World> actualWorld = game.getServer().getWorld(world.getUniqueId());
                        if (!actualWorld.isPresent()) {
                            throw new CommandException(Texts.of("World was not loaded!"));
                        }
                        LiftManager manager = getLiftManager(actualWorld.get());
                        game.getServiceManager().provideUnchecked(PaginationService.class).builder()
                                .title(MessageFormatting
                                        .normal(Texts.builder("Lifts in world '").append(Texts.of(world.getWorldName()), Texts.of('\''))))
                                .contents(Collections2.transform(manager.getLifts(), new Function<Lift, Text>() {
                                    @Nullable
                                    @Override
                                    public Text apply(Lift input) {
                                        return MessageFormatting.normal(Texts.builder("Lift at ").append(Texts.of(input.getPosition(), " moving ",
                                                input.getDirection())));
                                    }
                                }))
                                .sendTo(src);
                        return CommandResult.success();
                    }
                })
                .build(), "lslifts");

        liftRunner = new LiftRunner(this);
        game.getSyncScheduler().runRepeatingTaskAfter(this, liftRunner, LiftRunner.RUN_FREQUENCY, LiftRunner.RUN_FREQUENCY);
    }

    @Subscribe
    public void onServerStarting(ServerStartingEvent event) {
        SpecialBlock.registerDefaults();
        try {
            config.load();
        } catch (IOException e) {
            this.logger.error("Unable to load LiftPlates configuration! This is fatal!", e);
            this.game.getServer().shutdown();
        }

    }


    @Subscribe
    public void onStopping(ServerStoppingEvent event) {
        for (LiftManager manager : liftManagers.values()) {
            manager.save();
        }
    }

    public void reload() {
        try {
            this.config.load();
        } catch (IOException e) {
            this.logger.error("Unable to load configuration!", e);
        }
        for (LiftManager manager : liftManagers.values()) {
            manager.load();
        }
    }

    public LiftRunner getLiftRunner() {
        return liftRunner;
    }

    public LiftPlatesConfig getConfiguration() {
        return config;
    }

    public LiftManager getLiftManager(Extent extent) {
        if (!(extent instanceof World)) {
            throw new IllegalArgumentException("Provided extent " + extent + " is not a World!");
        }
        return getLiftManager((World) extent);
    }

    public LiftManager getLiftManager(World world) {
        Preconditions.checkNotNull(world);
        LiftManager manager = liftManagers.get(world.getUniqueId());
        if (manager == null) {
            manager = new LiftManager(this, world);
            manager.load();
            liftManagers.put(world.getUniqueId(), manager);
        }
        return manager;
    }

    public Lift detectLift(Location loc, boolean nearby) {
        Preconditions.checkNotNull(loc, "loc");
        Vector3i pointLoc = loc.getBlockPosition();
        Lift lift = getLiftManager((World) loc.getExtent()).getLift(pointLoc);
        if (lift == null && nearby) {
            return detectLift(loc, (config.maxLiftSize + 1) * (config.maxLiftSize + 1), pointLoc, new ArrayList<Vector3i>(10));
        }
        return lift;
    }


    private Lift detectLift(Location orig, int maxDistSq, Vector3i loc, List<Vector3i> visited) {
        if (orig.getBlockPosition().distanceSquared(loc) > maxDistSq) {
            return null;
        }
        LiftManager manager = getLiftManager(orig.getExtent());
        Lift lift = manager.getLift(loc);
        if (lift != null) {
            return lift;
        }

        boolean lastChance = false;
        int maxCount = orig.getExtent().getBlockMax().getY() - loc.getY();
        for (int i = 0; i < maxCount; ++i) {
            Vector3i raisedPos = loc.add(0, i, 0);
            lift = manager.getLift(raisedPos);
            if (lift != null) {
                return lift;
            } else {
                if (orig.getExtent().getBlockType(raisedPos) != BlockTypes.AIR) {
                    if (lastChance) {
                        break;
                    } else {
                        lastChance = true;
                    }
                }
            }
        }

        for (int i = 0; i <= loc.getY(); ++i) {
            Vector3i loweredY = loc.sub(0, i, 0);
            lift = manager.getLift(loweredY);
            if (lift != null) {
                return lift;
            } else {
                if (orig.getExtent().getBlockType(loweredY) != BlockTypes.AIR) {
                    break;
                }
            }
        }

        visited.add(loc);
        for (Direction face : LiftUtil.NSEW_FACES) {
            Vector3i newLoc = loc.add(face.toVector3d().toInt());
            if (visited.contains(newLoc)) {
                continue;
            }

            lift = detectLift(orig, maxDistSq, newLoc, visited);
            if (lift != null) {
                return lift;
            }
        }
        return null;
    }

    public Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getConfigurationFolder() {
        return config.getConfigDir();
    }
}
