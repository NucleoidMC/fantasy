package xyz.nucleoid.fantasy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.timeline.Timeline;
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
        try (var _ = RemoveFromRegistry.thaw(dimensionsRegistry)) {
            var key = ResourceKey.create(Registries.LEVEL_STEM, levelKey.identifier());
            if(!dimensionsRegistry.containsKey(key)) {
                dimensionsRegistry.register(key, options, RegistrationInfo.BUILT_IN);
            }
        }

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
            this.unregister((RuntimeLevel) level, dimensionKey, dimensionsRegistry, true);

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
            this.unregister((RuntimeLevel) level, dimensionKey, dimensionsRegistry, false);
        }
    }

    private void unregister(RuntimeLevel level, ResourceKey<Level> dimensionKey, MappedRegistry<LevelStem> dimensionsRegistry, boolean alwaysDelete) {
        RemoveFromRegistry.remove(dimensionsRegistry, dimensionKey.identifier());
        RuntimeServerClockManager.WORLD_CLOCKS_2_LEVEL.remove(level.worldClock);
        level.clockManager().unloadedClocks.add(level.worldClock);

        if (level.style.equals(RuntimeLevel.Style.TEMPORARY) || alwaysDelete) {
            level.clockManager().fantasy$getClocks().remove(level.worldClock);
            level.clockManager().fantasy$getPackedClockStates().clocks().remove(level.worldClock);
            level.clockManager().temporaryClocks.remove(level.worldClock);
            RuntimeServerClockManager.DIMENSION_TYPE_2_WORLD_CLOCKS.remove(level.dimensionType());
            RuntimeServerClockManager.DIMENSION_TYPE_2_TIMELINES.remove(level.dimensionType());
            RuntimeServerClockManager.WORLD_CLOCKS.remove(level.worldClock);
        }

        Registry<WorldClock> worldClockRegistry = this.server.registryAccess()
                .lookupOrThrow(Registries.WORLD_CLOCK);
        Registry<Timeline> timelineRegistry = this.server.registryAccess()
                .lookupOrThrow(Registries.TIMELINE);

        if (!level.worldClock.is(Fantasy.DEFAULT_WORLD_CLOCK) && level.style.equals(RuntimeLevel.Style.TEMPORARY)) {
            ((RemoveFromRegistry<WorldClock>) worldClockRegistry).fantasy$remove(level.worldClock.value());
        }

        if (!level.timeline.is(Fantasy.DEFAULT_TIMELINE)) {
            ((RemoveFromRegistry<Timeline>) timelineRegistry).fantasy$remove(level.timeline.value());
        }
    }

    private static MappedRegistry<LevelStem> getDimensionsRegistry(MinecraftServer server) {
        RegistryAccess registryManager = server.registries().compositeAccess();
        return (MappedRegistry<LevelStem>) registryManager.lookupOrThrow(Registries.LEVEL_STEM);
    }
}
