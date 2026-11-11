package xyz.nucleoid.fantasy.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class NeoForgeFantasyPlatform implements FantasyPlatform {
    @Override
    public void onLevelLoad(MinecraftServer server, ServerLevel level) {
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
    }

    @Override
    public void onLevelUnload(MinecraftServer server, ServerLevel level) {
        NeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));
    }
}
