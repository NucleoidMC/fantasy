package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        this.notifyAddPlayer(player);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);

        this.removeBubblePlayer(player, false);
    }

    List<ServerPlayerEntity> kickPlayers() {
        List<ServerPlayerEntity> players = new ArrayList<>(this.getPlayers());
        for (ServerPlayerEntity player : players) {
            this.kickPlayer(player);
        }
        return players;
    }

    void kickPlayer(ServerPlayerEntity player) {
        if (!this.removeBubblePlayer(player, true) || player.world == this) {
            try {
                ServerWorld overworld = this.getServer().getOverworld();
                BlockPos spawnPos = overworld.getSpawnPos();
                float spawnAngle = overworld.getSpawnAngle();
                player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
            } catch (Exception e) {
                player.networkHandler.disconnect(new LiteralText("Failed to transfer from world!"));
                Fantasy.LOGGER.error("Failed to kick from bubble world", e);
            }
        } else {
            this.notifyRemovePlayer(player);
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

    boolean removeBubblePlayer(ServerPlayerEntity player, boolean immediate) {
        this.assertServerThread();

        try {
            PlayerSnapshot snapshot = this.players.remove(player.getUuid());
            if (snapshot != null) {
                this.notifyRemovePlayer(player);

                if (immediate) {
                    snapshot.restore(player);
                } else {
                    // this might be called from a player being teleported out of the dimension
                    // in that case, we don't want to recursively teleport: wait for next tick to restore
                    this.fantasy.enqueueNextTick(() -> {
                        snapshot.restore(player);
                    });
                }

                return true;
            }
        } catch (Exception e) {
            player.networkHandler.disconnect(new LiteralText("Failed to transfer from world!"));
            Fantasy.LOGGER.error("Failed to remove player from bubble world", e);
        }

        return false;
    }

    private void joinBubblePlayer(ServerPlayerEntity player) {
        try {
            player.inventory.clear();
            player.getEnderChestInventory().clear();

            player.setHealth(20.0F);
            player.getHungerManager().setFoodLevel(20);

            Fantasy.resetPlayer(player);

            player.setGameMode(this.config.getDefaultGameMode());
            this.config.getSpawner().spawnPlayer(this, player);
        } catch (Exception e) {
            player.networkHandler.disconnect(new LiteralText("Failed to transfer into world!"));
            Fantasy.LOGGER.error("Failed to join player into bubble world", e);
        }
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
