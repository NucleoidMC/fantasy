package xyz.nucleoid.fantasy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.clock.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.fantasy.mixin.TimelineAccess;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ApiStatus.Internal
public final class RuntimeServerClockManager extends ServerClockManager implements ServerClockManagerExtension {
    public static Registry<WorldClock> worldClockRegistry;
    public static final SavedDataType<RuntimeServerClockManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Fantasy.ID, "world_clocks"),
            () -> new RuntimeServerClockManager(PackedClockStates.EMPTY),
            Codec.unboundedMap(Identifier.CODEC.flatComapMap(identifier -> {
                Holder<WorldClock> worldClock = worldClockRegistry.get(ResourceKey.create(Registries.WORLD_CLOCK, identifier)).orElse(null);

                if (worldClock == null) {
                    try (var _ = RemoveFromRegistry.thaw(worldClockRegistry)) {
                        worldClock = Registry.registerForHolder(worldClockRegistry, identifier, new WorldClock());
                    }
                }

                return worldClock;
            }, holder -> {
                Optional<ResourceKey<WorldClock>> resourceKey = holder.unwrapKey();

                //noinspection OptionalIsPresent
                if (resourceKey.isEmpty()) {
                    return DataResult.error(() -> "WorldClock is a direct Holder instead of a Holder.Reference");
                } else {
                    return DataResult.success(resourceKey.get().identifier());
                }
            }), ClockState.CODEC)
                    .xmap(PackedClockStates::new, PackedClockStates::clocks)
                    .xmap(RuntimeServerClockManager::new, RuntimeServerClockManager::packState),
            DataFixTypes.SAVED_DATA_WORLD_CLOCKS // we can't use data fixers in modded minecraft, so IDK what to do about this
    );
    public static final Map<DimensionType, Holder<WorldClock>> DIMENSION_TYPE_2_WORLD_CLOCKS = new HashMap<>();
    public static final Map<DimensionType, Holder<Timeline>[]> DIMENSION_TYPE_2_TIMELINES = new HashMap<>();
    final Set<Holder<WorldClock>> temporaryClocks = new HashSet<>();
    final Set<Holder<WorldClock>> unloadedClocks = new HashSet<>();
    public static final Set<Holder<WorldClock>> WORLD_CLOCKS = new HashSet<>();
    public static final Map<Holder<WorldClock>, ResourceKey<Level>> WORLD_CLOCKS_2_LEVEL = new HashMap<>();

    public RuntimeServerClockManager(PackedClockStates packedClockStates) {
        PackedClockStates packedClockStates1;

        if (!(packedClockStates.clocks() instanceof HashMap<Holder<WorldClock>, ClockState>)) {
            packedClockStates1 = new PackedClockStates(new HashMap<>(packedClockStates.clocks()));
        } else {
            packedClockStates1 = packedClockStates;
        }

        super(packedClockStates1);
    }

    public static Timeline createDefaultTimeline(Registry<Timeline> timelineRegistry, Holder<WorldClock> worldClock) {
        TimelineAccess overworldTimeline = (TimelineAccess) timelineRegistry.getValueOrThrow(Timelines.OVERWORLD_DAY);
        return TimelineAccess.init(
                worldClock,
                overworldTimeline.getPeriodTicks(),
                overworldTimeline.getTracks(),
                overworldTimeline.getTimeMarkers()
        );
    }

    @Override
    public void init(MinecraftServer server) {
        this.fantasy$setServer(server);
        this.fantasy$getPackedClockStates().clocks().forEach((definition, state) -> {
            ServerClockManager.ClockInstance instance = this.getInstance(definition);
            instance.loadFrom(state);
            WORLD_CLOCKS.add(definition);

            if (!server.registryAccess().lookupOrThrow(Registries.TIMELINE).containsKey(definition.unwrapKey().orElseThrow().identifier())) {
                this.unloadedClocks.add(definition);
            }
        });
    }

    @Override
    public void tick() {
        var ref = new Object() {
            boolean timeAdvanced = false;
        };
        this.fantasy$getClocks().forEach((holder, clockInstance) -> {
            ServerLevel level = this.fantasy$getServer().getLevel(ResourceKey.create(Registries.DIMENSION, holder.unwrapKey().orElseThrow().identifier()));

            if (level != null && level.getGameRules().get(GameRules.ADVANCE_TIME)) {
                ref.timeAdvanced = true;
                clockInstance.tick();
            }
        });

        if (ref.timeAdvanced) {
            this.setDirty();
        }
    }

    @Override
    public PackedClockStates packState() {
        Map<Holder<WorldClock>, ClockInstance> clocks = new HashMap<>();

        for (Map.Entry<Holder<WorldClock>, ClockInstance> entry : this.fantasy$getClocks().entrySet()) {
            if (!entry.getKey().is(Fantasy.DEFAULT_WORLD_CLOCK) && hasClock(entry.getKey()) && !this.temporaryClocks.contains(entry.getKey())) {
                clocks.put(entry.getKey(), entry.getValue());
            }
        }

        return new PackedClockStates(Util.mapValues(clocks, ServerClockManager.ClockInstance::packState));
    }

    @Override
    protected ClockInstance getInstance(Holder<WorldClock> definition) {
        return this.fantasy$getClocks().computeIfAbsent(definition, _ -> new ClockInstance());
    }

    @Override
    public Stream<ResourceKey<ClockTimeMarker>> commandTimeMarkersForClock(Holder<WorldClock> clock) {
        try {
            return super.commandTimeMarkersForClock(clock);
        } catch (IllegalStateException e) {
            if (hasClock(clock)) {
                return Stream.of();
            } else {
                throw e;
            }
        }
    }

    @Override
    protected void modifyClock(Holder<WorldClock> clock, Consumer<? super ClockInstance> action) {
        RuntimeLevel runtimeLevel = null;

        if (WORLD_CLOCKS_2_LEVEL.containsKey(clock)) {
            ServerLevel level = this.fantasy$getServer().getLevel(WORLD_CLOCKS_2_LEVEL.get(clock));

            if (level instanceof RuntimeLevel runtimeLevel1) {
                runtimeLevel = runtimeLevel1;
            } else {
                WORLD_CLOCKS_2_LEVEL.remove(clock);
            }
        } else {
            super.modifyClock(clock, action);
            return;
        }

        ServerClockManager.ClockInstance instance = this.getInstance(clock);
        action.accept(instance);

        if (runtimeLevel != null) {
            ((RuntimeLevelData) runtimeLevel.getLevelData()).setGameTime(this.getTotalTicks(clock) % runtimeLevel.timeline.value().periodTicks().orElse(24000));
            this.fantasy$getServer().getPlayerList().broadcastAll(new ClientboundSetTimePacket(runtimeLevel.getGameTime(), Map.of(clock, instance.packNetworkState(this.fantasy$getServer()))), runtimeLevel.dimension());
            runtimeLevel.environmentAttributes().invalidateTickCache();
        }

        this.setDirty();
    }

    public void registerClock(Holder<WorldClock> definition, Holder<Timeline> timeline, boolean temporary) {
        this.fantasy$getClocks().computeIfAbsent(definition, _ -> new ClockInstance());
        timeline.value().registerTimeMarkers(this::fantasy$registerTimeMarker);

        if (temporary) {
            this.temporaryClocks.add(definition);
        }
    }

    public static boolean hasClock(Holder<WorldClock> clock) {
        return WORLD_CLOCKS.contains(clock);
    }
}
