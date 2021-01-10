package xyz.nucleoid.fantasy.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.util.PlayerResetter;

import java.util.function.Function;

public interface PlayerManagerAccess {
    void teleportAndRecreate(ServerPlayerEntity player, Function<ServerPlayerEntity, ServerWorld> recreate);

    void loadIntoPlayer(ServerPlayerEntity player);

    PlayerResetter getPlayerResetter();
}
