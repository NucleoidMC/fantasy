package xyz.nucleoid.fantasy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
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

final class RuntimeLevelManager {
    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    RuntimeLevelManager(MinecraftServer server) {
        this.server = server;
        this.serverAccess = (MinecraftServerAccess) server;
    }

    RuntimeLevel add(ResourceKey<Level> levelKey, RuntimeLevelConfig config, RuntimeLevel.Style style) {
        LevelStem options = config.createDimensionOptions(this.server);

        if (style == RuntimeLevel.Style.TEMPORARY) {
            ((FantasyDimensionOptions) (Object) options).fantasy$setSave(false);
        }
        ((FantasyDimensionOptions) (Object) options).fantasy$setSaveProperties(false);

        MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
        boolean isFrozen = ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$isFrozen();
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(false);

        var key = ResourceKey.create(Registries.LEVEL_STEM, levelKey.identifier());
        if(!dimensionsRegistry.containsKey(key)) {
            dimensionsRegistry.register(key, options, RegistrationInfo.BUILT_IN);
        }
        ((RemoveFromRegistry<?>) dimensionsRegistry).fantasy$setFrozen(isFrozen);

        RuntimeLevel level = config.getLevelConstructor().createLevel(this.server, levelKey, config, style);

        this.serverAccess.getLevels().put(level.dimension(), level);
        ServerLevelEvents.LOAD.invoker().onLevelLoad(this.server, level);

        // tick the level to ensure it is ready for use right away
        level.tick(() -> true);

        return level;
    }

    void delete(ServerLevel level) {
        ResourceKey<Level> dimensionKey = level.dimension();

        if (this.serverAccess.getLevels().remove(dimensionKey, level)) {
            ServerLevelEvents.UNLOAD.invoker().onLevelUnload(this.server, level);

            MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.identifier());

            LevelStorageSource.LevelStorageAccess session = this.serverAccess.getStorageSource();
            File levelDirectory = session.getDimensionPath(dimensionKey).toFile();
            if (levelDirectory.exists()) {
                try {
                    FileUtils.deleteDirectory(levelDirectory);
                } catch (IOException e) {
                    Fantasy.LOGGER.warn("Failed to delete level directory", e);
                    try {
                        FileUtils.forceDeleteOnExit(levelDirectory);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    void unload(ServerLevel level) {
        ResourceKey<Level> dimensionKey = level.dimension();

        if (this.serverAccess.getLevels().remove(dimensionKey, level)) {
            level.save(new ProgressListener() {
                @Override
                public void progressStartNoAbort(Component title) {}

                @Override
                public void progressStart(Component title) {}

                @Override
                public void progressStage(Component task) {}

                @Override
                public void progressStagePercentage(int percentage) {}

                @Override
                public void stop() {}
            }, true, false);

            ServerLevelEvents.UNLOAD.invoker().onLevelUnload(RuntimeLevelManager.this.server, level);

            MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(RuntimeLevelManager.this.server);
            RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.identifier());
        }
    }

    private static MappedRegistry<LevelStem> getDimensionsRegistry(MinecraftServer server) {
        RegistryAccess registryManager = server.registries().compositeAccess();
        return (MappedRegistry<LevelStem>) registryManager.lookupOrThrow(Registries.LEVEL_STEM);
    }
}
