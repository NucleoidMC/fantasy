package xyz.nucleoid.fantasy.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface PlayerManagerAccess {
    void teleportAndRecreate(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, ServerWorld world);

    ServerPlayerEntity createLoadedPlayer(GameProfile profile);
}
