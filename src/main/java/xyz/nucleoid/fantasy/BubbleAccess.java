package xyz.nucleoid.fantasy;

import net.minecraft.server.world.ServerWorld;

public final class BubbleAccess {
    public static boolean isBubbleWorld(ServerWorld world) {
        return world instanceof BubbleWorld;
    }
}
