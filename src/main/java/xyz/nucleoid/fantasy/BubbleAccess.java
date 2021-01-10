package xyz.nucleoid.fantasy;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class BubbleAccess {
    public static boolean isPlayedBubbled(ServerPlayerEntity player) {
        return isBubbleWorld(player.getServerWorld());
    }

    public static boolean isBubbleWorld(ServerWorld world) {
        return world instanceof BubbleWorld;
    }

    @Nullable
    public static BubbleWorldConfig getBubbleConfig(ServerWorld world) {
        if (world instanceof BubbleWorld) {
            return ((BubbleWorld) world).config;
        }
        return null;
    }
}
