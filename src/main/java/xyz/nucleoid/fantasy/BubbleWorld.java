package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.player.BubblePlayerTeleporter;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class BubbleWorld extends ServerWorld {
    private final Fantasy fantasy;

    final BubbleWorldConfig config;

    private final Set<UUID> players = new ObjectOpenHashSet<>();

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
        this.onAddPlayer(player);
    }

    @Override
    public void onPlayerChangeDimension(ServerPlayerEntity player) {
        super.onPlayerChangeDimension(player);
        this.onAddPlayer(player);
    }

    @Override
    public void onPlayerConnected(ServerPlayerEntity player) {
        super.onPlayerConnected(player);
        this.onAddPlayer(player);
    }

    @Override
    public void onPlayerRespawned(ServerPlayerEntity player) {
        super.onPlayerRespawned(player);
        this.onAddPlayer(player);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);

        this.onRemovePlayer(player);
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        if (!this.containsBubblePlayer(player.getUuid())) {
            this.kickPlayer(player);
            return;
        }

        this.notifyAddPlayer(player);
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        this.players.remove(player.getUuid());

        this.notifyRemovePlayer(player);
    }

    List<ServerPlayerEntity> kickPlayers() {
        List<ServerPlayerEntity> players = new ArrayList<>(this.getPlayers());
        for (ServerPlayerEntity player : players) {
            this.kickPlayer(player);
        }
        this.players.clear();
        return players;
    }

    void kickPlayer(ServerPlayerEntity player) {
        BubblePlayerTeleporter teleporter = new BubblePlayerTeleporter(player);
        teleporter.teleportFromBubble(this.config);
    }

    boolean addBubblePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        if (this.players.add(player.getUuid())) {
            BubblePlayerTeleporter teleporter = new BubblePlayerTeleporter(player);
            teleporter.teleportIntoBubble(this, this.config);
            return true;
        }

        return false;
    }

    boolean removeBubblePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        try {
            if (this.players.remove(player.getUuid())) {
                this.kickPlayer(player);
                return true;
            }
        } catch (Exception e) {
            player.networkHandler.disconnect(new LiteralText("Failed to transfer from world!"));
            Fantasy.LOGGER.error("Failed to remove player from bubble world", e);
        }

        return false;
    }

    private void notifyAddPlayer(ServerPlayerEntity player) {
        for (WorldPlayerListener listener : this.playerListeners) {
            listener.onAddPlayer(player);
        }
    }

    private void notifyRemovePlayer(ServerPlayerEntity player) {
        for (WorldPlayerListener listener : this.playerListeners) {
            listener.onRemovePlayer(player);
        }
    }

    boolean containsBubblePlayer(UUID id) {
        return this.players.contains(id);
    }

    private void assertServerThread() {
        Thread currentThread = Thread.currentThread();
        Thread serverThread = this.getServer().getThread();
        if (currentThread != serverThread) {
            throw new UnsupportedOperationException("cannot execute on " + currentThread.getName() + ": expected server thread (" + serverThread.getName() + ")!");
        }
    }
}
