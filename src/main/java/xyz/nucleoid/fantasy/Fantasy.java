package xyz.nucleoid.fantasy;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public final class Fantasy {
    public static final Logger LOGGER = LogManager.getLogger(Fantasy.class);
    public static final String ID = "fantasy";

    private static Fantasy instance;

    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private final Set<ServerWorld> deletionQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Fantasy fantasy = get(server);
            fantasy.tick();
        });
    }

    private Fantasy(MinecraftServer server) {
        this.server = server;
        this.serverAccess = (MinecraftServerAccess) server;
    }

    public static Fantasy get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new Fantasy(server);
        }
        return instance;
    }

    void enqueueNextTick(Runnable task) {
        this.taskQueue.add(task);
    }

    private void tick() {
        Runnable task;
        while ((task = this.taskQueue.poll()) != null) {
            task.run();
        }

        if (!this.deletionQueue.isEmpty()) {
            this.deletionQueue.removeIf(this::tickDeleteWorld);
        }
    }

    public CompletableFuture<BubbleWorldHandle> openBubble(BubbleWorldConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            BubbleWorld world = this.openBubbleWorld(config);
            return new BubbleWorldHandle(this, world);
        }, this.server);
    }

    public CompletableFuture<PersistentWorldHandle> getOrOpenPersistentWorld(Identifier key, Supplier<DimensionOptions> options) {
        return CompletableFuture.supplyAsync(() -> {
            RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, key);
            ServerWorld world = this.server.getWorld(worldKey);
            if (world != null) {
                this.deletionQueue.remove(world);
                return world;
            }

            // TODO: custom seeds
            return this.openPersistentWorld(key, options.get(), new Random().nextLong());
        }, this.server)
                .thenApply(world -> new PersistentWorldHandle(this, world));
    }

    ServerWorld openPersistentWorld(Identifier key, DimensionOptions options, long seed) {
        RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, key);

        // TODO: custom properties that can be transferred with the map? (we'll have to handle saving them manually!)
        ServerWorldProperties overworldProperties = (ServerWorldProperties) this.server.getOverworld().getLevelProperties();
        ServerWorldProperties properties = new UnmodifiableLevelProperties(this.server.getSaveProperties(), overworldProperties);

        SimpleRegistry<DimensionOptions> dimensionsRegistry = this.getDimensionsRegistry();
        dimensionsRegistry.add(RegistryKey.of(Registry.DIMENSION_OPTIONS, key), options, Lifecycle.stable());

        ServerWorld world = new ServerWorld(
                this.server, Util.getMainWorkerExecutor(),
                this.serverAccess.getSession(),
                properties,
                worldKey,
                options.getDimensionType(),
                VoidWorldProgressListener.INSTANCE,
                options.getChunkGenerator(),
                false,
                BiomeAccess.hashSeed(seed),
                ImmutableList.of(),
                false
        );

        this.serverAccess.getWorlds().put(worldKey, world);

        return world;
    }

    BubbleWorld openBubbleWorld(BubbleWorldConfig config) {
        RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, this.generateBubbleKey());

        BubbleWorld world = new BubbleWorld(this, this.server, worldKey, config);
        this.serverAccess.getWorlds().put(worldKey, world);

        return world;
    }

    void enqueueWorldDeletion(ServerWorld world) {
        this.server.submit(() -> this.deletionQueue.add(world));
    }

    private boolean tickDeleteWorld(ServerWorld world) {
        if (this.isWorldUnloaded(world)) {
            this.deleteWorld(world);
            return true;
        } else {
            this.kickPlayers(world);
            return false;
        }
    }

    private void kickPlayers(ServerWorld world) {
        if (world.getPlayers().isEmpty()) {
            return;
        }

        ServerWorld overworld = this.server.getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        float spawnAngle = overworld.getSpawnAngle();

        List<ServerPlayerEntity> players = new ArrayList<>(world.getPlayers());
        for (ServerPlayerEntity player : players) {
            player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
        }
    }

    private boolean isWorldUnloaded(ServerWorld world) {
        return world.getPlayers().isEmpty() && world.getChunkManager().getLoadedChunkCount() <= 0;
    }

    private void deleteWorld(ServerWorld world) {
        RegistryKey<World> dimensionKey = world.getRegistryKey();

        if (this.serverAccess.getWorlds().remove(dimensionKey, world)) {
            SimpleRegistry<DimensionOptions> dimensionsRegistry = this.getDimensionsRegistry();
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

    private SimpleRegistry<DimensionOptions> getDimensionsRegistry() {
        GeneratorOptions generatorOptions = this.server.getSaveProperties().getGeneratorOptions();
        return generatorOptions.getDimensions();
    }

    private Identifier generateBubbleKey() {
        String random = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
        return new Identifier(Fantasy.ID, "bubble_" + random);
    }
}
