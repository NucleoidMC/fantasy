package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.PlayerSnapshot;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.io.IOException;
import java.util.*;

final class BubbleWorld extends ServerWorld {
    private final Fantasy fantasy;

    private final BubbleWorldConfig config;

    private final Map<UUID, PlayerSnapshot> players = new Object2ObjectOpenHashMap<>();

    private final List<WorldPlayerListener> playerListeners = new ArrayList<>();

    BubbleWorld(
            Fantasy fantasy,
            MinecraftServer server,
            RegistryKey<World> registryKey,
            BubbleWorldConfig config
    ) {
        super(
                server,
                Util.getMainWorkerExecutor(),
                ((MinecraftServerAccess) server).getSession(),
                new BubbleWorldProperties(server.getSaveProperties(), config),
                registryKey,
                Preconditions.checkNotNull(server.getRegistryManager().getDimensionTypes().get(config.getDimensionType()), "invalid dimension type!"),
                VoidWorldProgressListener.INSTANCE,
                config.getGenerator(),
                false,
                BiomeAccess.hashSeed(config.getSeed()),
                ImmutableList.of(),
                false
        );
        this.fantasy = fantasy;
        this.config = config;
    }

    public void addPlayerListener(WorldPlayerListener listener) {
        this.playerListeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.fantasy.enqueueWorldDeletion(this);
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean enabled) {
        if (!flush) {
            super.save(progressListener, flush, enabled);
        }
    }

    @Override
    public void onPlayerTeleport(ServerPlayerEntity player) {
        super.onPlayerTeleport(player);
        this.addPlayer(player);
    }

    @Override
    public void onPlayerChangeDimension(ServerPlayerEntity player) {
        super.onPlayerChangeDimension(player);
        this.addPlayer(player);
    }

    @Override
    public void onPlayerConnected(ServerPlayerEntity player) {
        super.onPlayerConnected(player);
        this.addPlayer(player);
    }

    @Override
    public void onPlayerRespawned(ServerPlayerEntity player) {
        super.onPlayerRespawned(player);
        this.addPlayer(player);
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.containsBubblePlayer(player.getUuid())) {
            this.kickPlayer(player);
            return;
        }

        for (WorldPlayerListener listener : this.playerListeners) {
            listener.onAddPlayer(player);
        }
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);

        for (WorldPlayerListener listener : this.playerListeners) {
            listener.onRemovePlayer(player);
        }

        this.removeBubblePlayer(player);
    }

    List<ServerPlayerEntity> kickPlayers() {
        List<ServerPlayerEntity> players = new ArrayList<>(this.getPlayers());
        for (ServerPlayerEntity player : players) {
            this.kickPlayer(player);
        }
        return players;
    }

    void kickPlayer(ServerPlayerEntity player) {
        if (!this.removeBubblePlayer(player) || player.world == this) {
            ServerWorld overworld = this.getServer().getOverworld();
            BlockPos spawnPos = overworld.getSpawnPos();
            float spawnAngle = overworld.getSpawnAngle();
            player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
        } else {
            for (WorldPlayerListener listener : this.playerListeners) {
                listener.onRemovePlayer(player);
            }
        }
    }

    boolean addBubblePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        if (!this.players.containsKey(player.getUuid())) {
            this.players.put(player.getUuid(), PlayerSnapshot.take(player));
            this.joinBubblePlayer(player);
            return true;
        }

        return false;
    }

    boolean removeBubblePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        PlayerSnapshot snapshot = this.players.remove(player.getUuid());
        if (snapshot != null) {
            snapshot.restore(player);
            return true;
        }

        return false;
    }

    private void joinBubblePlayer(ServerPlayerEntity player) {
        player.inventory.clear();
        player.getEnderChestInventory().clear();

        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.clearStatusEffects();

        player.setFireTicks(0);
        player.stopFallFlying();

        player.setGameMode(this.config.getDefaultGameMode());
        this.config.getSpawner().spawnPlayer(this, player);
    }

    boolean containsBubblePlayer(UUID id) {
        return this.players.containsKey(id);
    }

    private void assertServerThread() {
        Thread currentThread = Thread.currentThread();
        Thread serverThread = this.getServer().getThread();
        if (currentThread != serverThread) {
            throw new UnsupportedOperationException("cannot execute on " + currentThread.getName() + ": expected server thread (" + serverThread.getName() + ")!");
        }
    }
}
