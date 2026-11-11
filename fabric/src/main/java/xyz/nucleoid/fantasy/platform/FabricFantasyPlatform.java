package xyz.nucleoid.fantasy.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class FabricFantasyPlatform implements FantasyPlatform {
    @Override
    public void onLevelLoad(MinecraftServer server, ServerLevel level) {
        ServerLevelEvents.LOAD.invoker().onLevelLoad(server, level);
    }

    @Override
    public void onLevelUnload(MinecraftServer server, ServerLevel level) {
        ServerLevelEvents.UNLOAD.invoker().onLevelUnload(server, level);
    }
}
