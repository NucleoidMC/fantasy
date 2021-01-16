package xyz.nucleoid.fantasy;

import net.minecraft.server.world.ServerWorld;

public final class PersistentWorldHandle implements FantasyWorldHandle {
    private final Fantasy fantasy;
    private final FantasyWorld world;

    PersistentWorldHandle(Fantasy fantasy, FantasyWorld world) {
        this.fantasy = fantasy;
        this.world = world;
    }

    @Override
    public void setTickWhenEmpty(boolean tickWhenEmpty) {
        this.world.setTickWhenEmpty(tickWhenEmpty);
    }

    @Override
    public ServerWorld asWorld() {
        return this.world;
    }

    @Override
    public void delete() {
        this.fantasy.enqueueWorldDeletion(this.world);
    }
}
