package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Fantasy is a library that allows for dimensions to be created and destroyed at runtime on the server.
 * It supports both temporary dimensions which do not get saved, as well as persistent dimensions which can be safely
 * used across server restarts.
 *
 * @see Fantasy#get(MinecraftServer)
 * @see Fantasy#openTemporaryWorld(RuntimeWorldConfig)
 * @see Fantasy#getOrOpenPersistentWorld(Identifier, RuntimeWorldConfig)
 */
public final class Fantasy {
    public static final Logger LOGGER = LogManager.getLogger(Fantasy.class);
    public static final String ID = "fantasy";
    public static final RegistryKey<DimensionType> DEFAULT_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(Fantasy.ID, "default"));

    private static Fantasy instance;

    private final MinecraftServer server;
    private final MinecraftServerAccess serverAccess;

    private final RuntimeWorldManager worldManager;

    private final Set<ServerWorld> deletionQueue = new ReferenceOpenHashSet<>();
    private final Set<ServerWorld> unloadingQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Fantasy fantasy = get(server);
            fantasy.tick();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Fantasy fantasy = get(server);
            fantasy.onServerStopping();
        });
    }

    private Fantasy(MinecraftServer server) {
        this.server = server;
        this.serverAccess = (MinecraftServerAccess) server;

        this.worldManager = new RuntimeWorldManager(server);
    }

    /**
     * Gets the {@link Fantasy} instance for the given server instance.
     *
     * @param server the server to work with
     * @return the {@link Fantasy} instance to work with runtime dimensions
     */
    public static Fantasy get(MinecraftServer server) {
        Preconditions.checkState(server.isOnThread(), "cannot create worlds from off-thread!");

        if (instance == null || instance.server != server) {
            instance = new Fantasy(server);
        }

        return instance;
    }

    private void tick() {
        Set<ServerWorld> deletionQueue = this.deletionQueue;
        if (!deletionQueue.isEmpty()) {
            deletionQueue.removeIf(this::tickDeleteWorld);
        }

        Set<ServerWorld> unloadingQueue = this.unloadingQueue;
        if (!unloadingQueue.isEmpty()) {
            unloadingQueue.removeIf(this::tickUnloadWorld);
        }
    }

    /**
     * Creates a new temporary world with the given {@link RuntimeWorldConfig} that will not be saved and will be
     * deleted when the server exits.
     * <p>
     * The created world is returned asynchronously through a {@link RuntimeWorldHandle}.
     * This handle can be used to acquire the {@link ServerWorld} object through {@link RuntimeWorldHandle#asWorld()},
     * as well as to delete the world through {@link RuntimeWorldHandle#delete()}.
     *
     * @param config the config with which to construct this temporary world
     * @return a future providing the created world
     */
    public RuntimeWorldHandle openTemporaryWorld(RuntimeWorldConfig config) {
        return this.openTemporaryWorld(generateTemporaryWorldKey(), config);
    }

    /**
     * Creates a new temporary world with the given identifier and {@link RuntimeWorldConfig} that will not be saved and will be
     * deleted when the server exits.
     * <p>
     * The created world is returned asynchronously through a {@link RuntimeWorldHandle}.
     * This handle can be used to acquire the {@link ServerWorld} object through {@link RuntimeWorldHandle#asWorld()},
     * as well as to delete the world through {@link RuntimeWorldHandle#delete()}.
     *
     * @param key the unique identifier for this dimension
     * @param config the config with which to construct this temporary world
     * @return a future providing the created world
     */
    public RuntimeWorldHandle openTemporaryWorld(Identifier key, RuntimeWorldConfig config) {
        RuntimeWorld world = this.addTemporaryWorld(key, config);
        return new RuntimeWorldHandle(this, world);
    }

    /**
     * Gets or creates a new persistent world with the given identifier and {@link RuntimeWorldConfig}. These worlds
     * will be saved to disk and can be restored after a server restart.
     * <p>
     * If a world with this identifier exists already, it will be returned and no new world will be constructed.
     * <p>
     * <b>Note!</b> These persistent worlds will not be automatically restored! This function
     * must be called after a server restart with the relevant identifier and configuration such that it can be loaded.
     * <p>
     * The created world is returned asynchronously through a {@link RuntimeWorldHandle}.
     * This handle can be used to acquire the {@link ServerWorld} object through {@link RuntimeWorldHandle#asWorld()},
     * as well as to delete the world through {@link RuntimeWorldHandle#delete()}.
     *
     * @param key the unique identifier for this dimension
     * @param config the config with which to construct this persistent world
     * @return a future providing the created world
     */
    public RuntimeWorldHandle getOrOpenPersistentWorld(Identifier key, RuntimeWorldConfig config) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, key);

        ServerWorld world = this.server.getWorld(worldKey);
        if (world == null) {
            world = this.addPersistentWorld(key, config);
        } else {
            this.deletionQueue.remove(world);
        }

        return new RuntimeWorldHandle(this, world);
    }

    private RuntimeWorld addPersistentWorld(Identifier key, RuntimeWorldConfig config) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, key);
        return this.worldManager.add(worldKey, config, RuntimeWorld.Style.PERSISTENT);
    }

    private RuntimeWorld addTemporaryWorld(Identifier key, RuntimeWorldConfig config) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, key);

        try {
            LevelStorage.Session session = this.serverAccess.getSession();
            FileUtils.forceDeleteOnExit(session.getWorldDirectory(worldKey).toFile());
        } catch (IOException ignored) {
        }

        return this.worldManager.add(worldKey, config, RuntimeWorld.Style.TEMPORARY);
    }

    void enqueueWorldDeletion(ServerWorld world) {
        this.server.submit(() -> {
            this.deletionQueue.add(world);
        });
    }

    void enqueueWorldUnloading(ServerWorld world) {
        this.server.submit(() -> {
            this.unloadingQueue.add(world);
        });
    }

    public boolean tickDeleteWorld(ServerWorld world) {
        if (this.isWorldUnloaded(world)) {
            this.worldManager.delete(world);
            return true;
        } else {
            this.kickPlayers(world);
            return false;
        }
    }

    public boolean tickUnloadWorld(ServerWorld world) {
        if (this.isWorldUnloaded(world)) {
            this.worldManager.unload(world);
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

    private void onServerStopping() {
        List<RuntimeWorld> temporaryWorlds = this.collectTemporaryWorlds();
        for (RuntimeWorld temporary : temporaryWorlds) {
            this.kickPlayers(temporary);
            this.worldManager.delete(temporary);
        }
    }

    private List<RuntimeWorld> collectTemporaryWorlds() {
        List<RuntimeWorld> temporaryWorlds = new ArrayList<>();
        for (ServerWorld world : this.server.getWorlds()) {
            if (world instanceof RuntimeWorld) {
                RuntimeWorld runtimeWorld = (RuntimeWorld) world;
                if (runtimeWorld.style == RuntimeWorld.Style.TEMPORARY) {
                    temporaryWorlds.add(runtimeWorld);
                }
            }
        }
        return temporaryWorlds;
    }

    private static Identifier generateTemporaryWorldKey() {
        String key = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
        return Identifier.of(Fantasy.ID, key);
    }
}
