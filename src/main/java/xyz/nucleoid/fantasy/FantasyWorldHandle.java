package xyz.nucleoid.fantasy;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface FantasyWorldHandle {
    ServerWorld asWorld();

    void delete();
}
