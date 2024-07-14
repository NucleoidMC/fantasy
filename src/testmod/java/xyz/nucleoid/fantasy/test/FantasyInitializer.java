package xyz.nucleoid.fantasy.test;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.slf4j.Logger;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class FantasyInitializer implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private HashMap<Identifier, RuntimeWorldHandle> worlds = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            Fantasy.get(s).openTemporaryWorld(
                    new RuntimeWorldConfig().setGenerator(new VoidChunkGenerator(s.getRegistryManager().get(RegistryKeys.BIOME).getEntry(0).get())).setWorldConstructor(CustomWorld::new)
            );

            Fantasy.get(s).openTemporaryWorld(
                    new RuntimeWorldConfig().setGenerator(s.getOverworld().getChunkManager().getChunkGenerator()).setWorldConstructor(CustomWorld::new)
            );

            var biome = s.getRegistryManager().get(RegistryKeys.BIOME).getEntry(s.getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS));
            FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
            FlatChunkGenerator generator = new FlatChunkGenerator(flat);

            Fantasy.get(s).openTemporaryWorld(new RuntimeWorldConfig()
                    .setDimensionType(DimensionTypes.OVERWORLD)
                    .setGenerator(generator)
                    .setShouldTickTime(true));
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("fantasy_open").then(
                    argument("name", IdentifierArgumentType.identifier())
                            .executes((context -> {
                                ServerCommandSource source = context.getSource();
                                try {

                                    var ref = new Object() {
                                        long t = System.currentTimeMillis();
                                    };

                                    var id = IdentifierArgumentType.getIdentifier(context, "name");

                                    var x = Fantasy.get(source.getServer()).getOrOpenPersistentWorld(
                                            id,
                                            new RuntimeWorldConfig()
                                                    .setGenerator(source.getServer().getOverworld().getChunkManager().getChunkGenerator())
                                                    .setDimensionType(source.getServer().getOverworld().getDimensionEntry())
                                                    .setSeed(id.hashCode())
                                    );

                                    source.sendFeedback(() -> Text.literal("WorldCreate: " + (System.currentTimeMillis() - ref.t)), false);

                                    this.worlds.put(id, x);

                                    ref.t = System.currentTimeMillis();
                                    if (source.getEntity() != null) {
                                        source.getEntity().teleportTo(new TeleportTarget(x.asWorld(), new Vec3d(0, 100, 0), Vec3d.ZERO, 0, 0, TeleportTarget.NO_OP));
                                    }

                                    source.sendFeedback(() -> Text.literal("Teleport: " + (System.currentTimeMillis() - ref.t)), false);
                                    return 1;
                                } catch (Throwable e) {
                                    LOGGER.error("Failed to open world", e);
                                    source.sendError(Text.literal("Failed to open world"));
                                    return 0;
                                }
                            }))
            ));

            dispatcher.register(literal("fantasy_delete").then(
                    argument("name", IdentifierArgumentType.identifier())
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                try {
                                    var id = IdentifierArgumentType.getIdentifier(context, "name");
                                    if (this.worlds.get(id) == null) {
                                        source.sendError(Text.literal("This world does not exist"));
                                        return 0;
                                    }
                                    this.worlds.get(id).delete();
                                    this.worlds.remove(id);

                                    source.sendFeedback(() -> Text.literal("World \"" + id + "\" deleted"), true);
                                } catch (Throwable e) {
                                    LOGGER.error("Failed to delete world", e);
                                    source.sendError(Text.literal("Failed to delete world"));
                                }
                                return 1;
                            })
            ));

            dispatcher.register(literal("fantasy_unload").then(
                    argument("name", IdentifierArgumentType.identifier())
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                try {
                                    var id = IdentifierArgumentType.getIdentifier(context, "name");
                                    if (this.worlds.get(id) == null) {
                                        source.sendError(Text.literal("This world does not exist"));
                                        return 0;
                                    }
                                    this.worlds.get(id).unload();
                                    this.worlds.remove(id);
                                    source.sendFeedback(() -> Text.literal("World \"" + id + "\" unloaded"), true);
                                } catch (Throwable e) {
                                    LOGGER.error("Failed to unload world", e);
                                    source.sendError(Text.literal("Failed to unload world"));
                                }

                                return 1;
                            })
            ));
        }));
    }
}
