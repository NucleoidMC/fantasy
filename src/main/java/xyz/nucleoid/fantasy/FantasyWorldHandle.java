package xyz.nucleoid.fantasy;

import net.minecraft.server.world.ServerWorld;

public interface FantasyWorldHandle {
    void setTickWhenEmpty(boolean tickWhenEmpty);

    ServerWorld asWorld();

    void delete();
}
