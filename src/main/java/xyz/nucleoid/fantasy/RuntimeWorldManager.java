package xyz.nucleoid.fantasy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
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

    private RuntimeWorld add(ResourceKey<Level> worldKey, RuntimeWorldConfig config, LevelStorageSource.LevelStorageAccess storageAccess, RuntimeWorld.Style style) {
        LevelStem options = config.createDimensionOptions(this.server);

        if (style == RuntimeWorld.Style.TEMPORARY) {
            ((FantasyDimensionOptions) (Object) options).fantasy$setSave(false);
        }
        ((FantasyDimensionOptions) (Object) options).fantasy$setSaveProperties(false);

        MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
        boolean isFrozen = ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$isFrozen();
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(false);

        var key = ResourceKey.create(Registries.LEVEL_STEM, worldKey.identifier());
        if (!dimensionsRegistry.containsKey(key)) {
            dimensionsRegistry.register(key, options, RegistrationInfo.BUILT_IN);
        }
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(isFrozen);

        RuntimeWorld world = config.getWorldConstructor().createWorld(this.server, worldKey, config, storageAccess, style);

        this.serverAccess.getLevels().put(world.dimension(), world);
        ServerWorldEvents.LOAD.invoker().onWorldLoad(this.server, world);

        // tick the world to ensure it is ready for use right away
        world.tick(() -> true);

        return world;
    }

    RuntimeWorld add(ResourceKey<Level> worldKey, RuntimeWorldConfig config,
                     LevelStorageSource.LevelStorageAccess storageAccess) {
        return this.add(worldKey, config, storageAccess, RuntimeWorld.Style.PERSISTENT);
    }

    RuntimeWorld add(ResourceKey<Level> worldKey, RuntimeWorldConfig config, RuntimeWorld.Style style) {
        return this.add(worldKey, config, this.serverAccess.getStorageSource(), style);
    }

    void delete(ServerLevel world) {
        ResourceKey<Level> dimensionKey = world.dimension();

        if (this.serverAccess.getLevels().remove(dimensionKey, world)) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this.server, world);

            MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.identifier());

            LevelStorageSource.LevelStorageAccess session = this.serverAccess.getStorageSource();
            File worldDirectory = session.getDimensionPath(dimensionKey).toFile();
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

    void unload(ServerLevel world) {
        ResourceKey<Level> dimensionKey = world.dimension();

        if (this.serverAccess.getLevels().remove(dimensionKey, world)) {
            world.save(new ProgressListener() {
                @Override
                public void progressStartNoAbort(Component title) {
                }

                @Override
                public void progressStart(Component title) {
                }

                @Override
                public void progressStage(Component task) {
                }

                @Override
                public void progressStagePercentage(int percentage) {
                }

                @Override
                public void stop() {
                }
            }, true, false);

            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(RuntimeWorldManager.this.server, world);

            MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(RuntimeWorldManager.this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.identifier());
        }
    }

    private static MappedRegistry<LevelStem> getDimensionsRegistry(MinecraftServer server) {
        RegistryAccess registryManager = server.registries().compositeAccess();
        return (MappedRegistry<LevelStem>) registryManager.lookupOrThrow(Registries.LEVEL_STEM);
    }
}
