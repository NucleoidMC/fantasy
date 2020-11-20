package xyz.nucleoid.fantasy;

import net.minecraft.server.network.ServerPlayerEntity;

public interface WorldPlayerListener {
    default void onAddPlayer(ServerPlayerEntity player) {
    }

    default void onRemovePlayer(ServerPlayerEntity player) {
    }
}
