package xyz.nucleoid.fantasy;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public final class BubbleAccess {
    public static boolean removePlayer(ServerWorld world, ServerPlayerEntity player) {
        if (world instanceof BubbleWorld) {
            return ((BubbleWorld) world).removeBubblePlayer(player, true);
        }
        return false;
    }

    public static boolean canPlayerJoin(ServerWorld world, UUID id) {
        if (world instanceof BubbleWorld) {
            return ((BubbleWorld) world).containsBubblePlayer(id);
        }
        return true;
    }
}
