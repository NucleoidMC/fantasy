package xyz.nucleoid.fantasy;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.io.File;
import java.io.IOException;

final class RuntimeWorldManager {
    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    RuntimeWorldManager(MinecraftServer server) {
        this.server = server;
        this.serverAccess = (MinecraftServerAccess) server;
    }

    private RuntimeWorld add(RegistryKey<World> worldKey, RuntimeWorldConfig config,
                             LevelStorage.Session storageSession, RuntimeWorld.Style style) {
        DimensionOptions options = config.createDimensionOptions(this.server);

        if (style == RuntimeWorld.Style.TEMPORARY) {
            ((FantasyDimensionOptions) (Object) options).fantasy$setSave(false);
        }

        SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionsRegistry(this.server);
        boolean isFrozen = ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$isFrozen();
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(false);
        dimensionsRegistry.add(RegistryKey.of(Registry.DIMENSION_KEY, worldKey.getValue()), options, Lifecycle.stable());
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(isFrozen);

        RuntimeWorld world = new RuntimeWorld(this.server, worldKey, config, storageSession, style);

        this.serverAccess.getWorlds().put(world.getRegistryKey(), world);
        ServerWorldEvents.LOAD.invoker().onWorldLoad(this.server, world);

        // tick the world to ensure it is ready for use right away
        world.tick(() -> true);

        return world;
    }

    RuntimeWorld add(RegistryKey<World> worldKey, RuntimeWorldConfig config,
                     LevelStorage.Session storageSession) {
        return this.add(worldKey, config, storageSession, RuntimeWorld.Style.PERSISTENT);
    }

    RuntimeWorld add(RegistryKey<World> worldKey, RuntimeWorldConfig config, RuntimeWorld.Style style) {
        return this.add(worldKey, config, this.serverAccess.getSession(), style);
    }

    void delete(ServerWorld world) {
        RegistryKey<World> dimensionKey = world.getRegistryKey();

        if (this.serverAccess.getWorlds().remove(dimensionKey, world)) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this.server, world);

            SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionsRegistry(this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.getValue());

            File worldDirectory = new File(world.getChunkManager().threadedAnvilChunkStorage.getSaveDir());
            if (worldDirectory.exists()) {
                try {
                    FileUtils.deleteDirectory(worldDirectory);
                } catch (IOException e) {
                    Fantasy.LOGGER.warn("Failed to delete world directory", e);
                    try {
                        FileUtils.forceDeleteOnExit(worldDirectory);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private static SimpleRegistry<DimensionOptions> getDimensionsRegistry(MinecraftServer server) {
        GeneratorOptions generatorOptions = server.getSaveProperties().getGeneratorOptions();
        return (SimpleRegistry<DimensionOptions>) generatorOptions.getDimensions();
    }
}
