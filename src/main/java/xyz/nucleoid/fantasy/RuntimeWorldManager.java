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

    RuntimeWorld add(RegistryKey<World> worldKey, RuntimeWorldConfig config, RuntimeWorld.Style style) {
        DimensionOptions options = config.createDimensionOptions(this.server);

        SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionsRegistry(this.server);
        dimensionsRegistry.add(RegistryKey.of(Registry.DIMENSION_OPTIONS, worldKey.getValue()), options, Lifecycle.stable());

        RuntimeWorld world = new RuntimeWorld(this.server, worldKey, config, style);

        this.serverAccess.getWorlds().put(world.getRegistryKey(), world);
        ServerWorldEvents.LOAD.invoker().onWorldLoad(this.server, world);

        // tick the world to ensure it is ready for use right away
        world.tick(() -> true);

        return world;
    }

    void delete(ServerWorld world) {
        RegistryKey<World> dimensionKey = world.getRegistryKey();

        if (this.serverAccess.getWorlds().remove(dimensionKey, world)) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this.server, world);

            SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionsRegistry(this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.getValue());

            LevelStorage.Session session = this.serverAccess.getSession();
            File worldDirectory = session.getWorldDirectory(dimensionKey);
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
        return generatorOptions.getDimensions();
    }
}
