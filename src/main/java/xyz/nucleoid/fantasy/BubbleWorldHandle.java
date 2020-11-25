package xyz.nucleoid.fantasy;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Collection;

public final class BubbleWorldHandle implements FantasyWorldHandle {
    private final Fantasy fantasy;
    private final BubbleWorld world;

    BubbleWorldHandle(Fantasy fantasy, BubbleWorld world) {
        this.fantasy = fantasy;
        this.world = world;
    }

    @Override
    public ServerWorld asWorld() {
        return this.world;
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        return this.world.addBubblePlayer(player);
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        return this.world.removeBubblePlayer(player, false);
    }

    public void addPlayerListener(WorldPlayerListener listener) {
        this.world.addPlayerListener(listener);
    }

    public Collection<ServerPlayerEntity> getPlayers() {
        return this.world.getPlayers();
    }

    @Override
    public void delete() {
        this.world.kickPlayers();
        this.fantasy.enqueueWorldDeletion(this.world);
    }
}
