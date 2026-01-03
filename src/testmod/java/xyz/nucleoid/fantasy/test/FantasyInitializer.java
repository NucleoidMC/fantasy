package xyz.nucleoid.fantasy.test;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import xyz.nucleoid.fantasy.storage.SingleDimensionLevelStorageSource;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class FantasyInitializer implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HashMap<Identifier, RuntimeWorldHandle> worlds = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            Fantasy.get(s).openTemporaryWorld(
                    new RuntimeWorldConfig().setGenerator(new VoidChunkGenerator(s.registryAccess().lookupOrThrow(Registries.BIOME).get(0).orElseThrow())).setWorldConstructor(CustomLevel::new)
            );

            Fantasy.get(s).openTemporaryWorld(
                    new RuntimeWorldConfig().setGenerator(s.overworld().getChunkSource().getGenerator()).setWorldConstructor(CustomLevel::new)
            );

            var biome = s.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
            var flat = new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of());
            var generator = new FlatLevelSource(flat);

            Fantasy.get(s).openTemporaryWorld(new RuntimeWorldConfig()
                    .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
                    .setGenerator(generator)
                    .setShouldTickTime(true));
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("fantasy_open").then(
                    argument("name", IdentifierArgument.id())
                            .then(argument("temp", BoolArgumentType.bool())
                                    .executes((context -> {
                                        var source = context.getSource();
                                        try {
                                            var temp = BoolArgumentType.getBool(context, "temp");
                                            var ref = new Object() {
                                                long t = System.currentTimeMillis();
                                            };

                                            var id = IdentifierArgument.getId(context, "name");

                                            RuntimeWorldHandle x;
                                            var config = new RuntimeWorldConfig()
                                                    .setGenerator(source.getServer().overworld().getChunkSource().getGenerator())
                                                    .setDimensionType(source.getServer().overworld().dimensionTypeRegistration())
                                                    .setSeed(id.hashCode());

                                            if (temp) {
                                                x = Fantasy.get(source.getServer()).openTemporaryWorld(id, config);
                                            } else {
                                                x = Fantasy.get(source.getServer()).getOrOpenPersistentWorld(id, config);
                                            }

                                            source.sendSuccess(() -> Component.literal("WorldCreate: " + (System.currentTimeMillis() - ref.t)), false);

                                            this.worlds.put(id, x);

                                            ref.t = System.currentTimeMillis();
                                            if (source.getEntity() != null) {
                                                source.getEntity().teleport(new TeleportTransition(x.asWorld(), new Vec3(0, 100, 0), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
                                            }

                                            source.sendSuccess(() -> Component.literal("Teleport: " + (System.currentTimeMillis() - ref.t)), false);
                                            return 1;
                                        } catch (Throwable e) {
                                            LOGGER.error("Failed to open world", e);
                                            source.sendFailure(Component.literal("Failed to open world"));
                                            return 0;
                                        }
                                    }))
                            )
            ));

            dispatcher.register(literal("fantasy_load_external").then(
                    argument("name", IdentifierArgument.id())
                            .then(argument("path", StringArgumentType.greedyString())
                                    .executes((context -> {
                                        var source = context.getSource();
                                        try {
                                            var id = IdentifierArgument.getId(context, "name");
                                            var path = StringArgumentType.getString(context, "path");

                                            var ref = new Object() {
                                                long t = System.currentTimeMillis();
                                            };

                                            var config = new RuntimeWorldConfig()
                                                    .setGenerator(source.getServer().overworld().getChunkSource().getGenerator())
                                                    .setDimensionType(source.getServer().overworld().dimensionTypeRegistration())
                                                    .setSeed(id.hashCode());

                                            var levelSource = new SingleDimensionLevelStorageSource(Path.of(path), new DirectoryValidator(it -> false), DataFixers.getDataFixer());

                                            var x = Fantasy.get(source.getServer()).getOrOpenPersistentWorld(id, config, levelSource.validateAndCreateAccess(id.toDebugFileName()));

                                            source.sendSuccess(() -> Component.literal("WorldCreate: " + (System.currentTimeMillis() - ref.t)), false);

                                            this.worlds.put(id, x);

                                            ref.t = System.currentTimeMillis();
                                            if (source.getEntity() != null) {
                                                source.getEntity().teleport(new TeleportTransition(x.asWorld(), new Vec3(0, 100, 0), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
                                            }

                                            source.sendSuccess(() -> Component.literal("Teleport: " + (System.currentTimeMillis() - ref.t)), false);
                                            return 1;
                                        } catch (Throwable e) {
                                            LOGGER.error("Failed to load world", e);
                                            source.sendFailure(Component.literal("Failed to load world"));
                                            return 0;
                                        }
                                    }))
                            )
            ));

            dispatcher.register(literal("fantasy_delete").then(
                    argument("name", IdentifierArgument.id())
                            .executes(context -> {
                                var source = context.getSource();
                                try {
                                    var id = IdentifierArgument.getId(context, "name");
                                    if (this.worlds.get(id) == null) {
                                        source.sendFailure(Component.literal("This world does not exist"));
                                        return 0;
                                    }
                                    this.worlds.get(id).delete();
                                    this.worlds.remove(id);

                                    source.sendSuccess(() -> Component.literal("World \"" + id + "\" deleted"), true);
                                } catch (Throwable e) {
                                    LOGGER.error("Failed to delete world", e);
                                    source.sendFailure(Component.literal("Failed to delete world"));
                                }
                                return 1;
                            })
            ));

            dispatcher.register(literal("fantasy_unload").then(
                    argument("name", IdentifierArgument.id())
                            .executes(context -> {
                                var source = context.getSource();
                                try {
                                    var id = IdentifierArgument.getId(context, "name");
                                    if (this.worlds.get(id) == null) {
                                        source.sendFailure(Component.literal("This world does not exist"));
                                        return 0;
                                    }
                                    this.worlds.get(id).unload();
                                    this.worlds.remove(id);
                                    source.sendSuccess(() -> Component.literal("World \"" + id + "\" unloaded"), true);
                                } catch (Throwable e) {
                                    LOGGER.error("Failed to unload world", e);
                                    source.sendFailure(Component.literal("Failed to unload world"));
                                }

                                return 1;
                            })
            ));
        }));
    }
}
