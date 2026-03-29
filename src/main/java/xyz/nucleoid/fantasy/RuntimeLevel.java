package xyz.nucleoid.fantasy;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.timeline.Timeline;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.util.List;
import java.util.concurrent.Executor;

import static xyz.nucleoid.fantasy.RuntimeServerClockManager.*;

public class RuntimeLevel extends ServerLevel {
    final Style style;
    private boolean flat;
    @Nullable
    private GameRules rules;
    @ApiStatus.Internal
    public final Holder<WorldClock> worldClock;
    @ApiStatus.Internal
    public final Holder<Timeline> timeline;

    protected RuntimeLevel(MinecraftServer server, ResourceKey<Level> dimension, RuntimeLevelConfig config, Style style) {
        Registry<WorldClock> worldClockRegistry = server.registryAccess()
                .lookupOrThrow(Registries.WORLD_CLOCK);
        Registry<Timeline> timelineRegistry = server.registryAccess()
                .lookupOrThrow(Registries.TIMELINE);
        Holder<WorldClock> worldClock;
        Holder<Timeline> timeline;

        try (var _ = RemoveFromRegistry.thaw(worldClockRegistry); var _ = RemoveFromRegistry.thaw(timelineRegistry)) {
            if (worldClockRegistry.containsKey(dimension.identifier())) {
                worldClock = worldClockRegistry.getOrThrow(ResourceKey.create(Registries.WORLD_CLOCK, dimension.identifier()));
            } else {
                worldClock = Registry.registerForHolder(
                        worldClockRegistry,
                        dimension.identifier(),
                        new WorldClock()
                );
            }

            if (timelineRegistry.containsKey(dimension.identifier())) {
                timeline = timelineRegistry.getOrThrow(ResourceKey.create(Registries.TIMELINE, dimension.identifier()));
            } else {
                timeline = Registry.registerForHolder(
                        timelineRegistry,
                        dimension.identifier(),
                        RuntimeServerClockManager.createDefaultTimeline(timelineRegistry, worldClock)
                );
            }
        }

        LevelStem dimensionOptions = config.createDimensionOptions(server);
        DimensionType dimensionType = dimensionOptions.type().value();
        WORLD_CLOCKS.add(worldClock);
        DIMENSION_TYPE_2_WORLD_CLOCKS.put(dimensionType, worldClock);
        WORLD_CLOCKS_2_LEVEL.put(worldClock, dimension);
        @SuppressWarnings("unchecked") // Always the same, correct type
        Holder<Timeline>[] timelines = dimensionType.timelines().stream()
                .map(holder -> {
                    if (holder.is(Fantasy.DEFAULT_TIMELINE)) {
                        return timeline;
                    } else {
                        return holder;
                    }
                })
                .toArray(Holder[]::new);
        DIMENSION_TYPE_2_TIMELINES.put(dimensionType, timelines);
        super(
                server, Util.backgroundExecutor(), ((MinecraftServerAccess) server).getStorageSource(),
                new RuntimeLevelData(server.getWorldData(), config),
                dimension,
                dimensionOptions,
                false,
                BiomeManager.obfuscateSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime()
        );
        this.style = style;
        this.flat = config.isFlat().orElse(super.isFlat());
        this.worldClock = worldClock;
        this.timeline = timeline;
        this.clockManager().registerClock(
                this.worldClock,
                this.timeline,
                this.style.equals(Style.TEMPORARY)
        );
        this.clockManager().unloadedClocks.remove(this.worldClock);
        this.getRuntimeLevelData().setGameTime(this.clockManager().getTotalTicks(this.worldClock) % this.timeline.value().periodTicks().orElse(24000));

        if (!config.shouldMirrorOverworldGameRules()) {
            this.rules = new GameRules(server.getWorldData().enabledFeatures());
            config.getGameRules().applyTo(this.rules, null);
        }
    }

    protected RuntimeLevel(MinecraftServer server, Executor executor, LevelStorageSource.LevelStorageAccess levelStorage, ServerLevelData levelData, ResourceKey<Level> dimension, LevelStem levelStem, boolean isDebug, long biomeZoomSeed, List<CustomSpawner> customSpawners, boolean tickTime, Style style, Holder<WorldClock> worldClock, Holder<Timeline> timeline) {
        super(server, executor, levelStorage, levelData, dimension, levelStem, isDebug, biomeZoomSeed, customSpawners, tickTime);
        this.style = style;
        this.worldClock = worldClock;
        this.timeline = timeline;
    }

    @Override
    public GameRules getGameRules() {
        if(this.rules != null) {
            return this.rules;
        }
        return super.getGameRules();
    }

    @Override
    public long getSeed() {
        return this.getRuntimeLevelData().config.getSeed();
    }

    @Override
    protected void tickTime() {
        if (this.tickTime) {
            long time = this.getRuntimeLevelData().getGameTime() + 1L;
            this.getRuntimeLevelData().setGameTime(time);
            Profiler.get().push("scheduledFunctions");
            this.getServer().getScheduledEvents().tick(this.getServer(), time);
            Profiler.get().pop();
        }
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean enabled) {
        if (this.style == Style.PERSISTENT || !flush) {
            super.save(progressListener, flush, enabled);
        }
    }

    @Override
    public boolean isFlat() {
        return this.flat;
    }

    private RuntimeLevelData getRuntimeLevelData() {
        return (RuntimeLevelData) this.levelData;
    }

    @Override
    public RuntimeServerClockManager clockManager() {
        return ((MinecraftServerExtension) this.getServer()).fantasy$clockManager();
    }

    public enum Style {
        PERSISTENT,
        TEMPORARY
    }

    public interface Constructor {
        RuntimeLevel createLevel(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeLevelConfig config, Style style);
    }
}
