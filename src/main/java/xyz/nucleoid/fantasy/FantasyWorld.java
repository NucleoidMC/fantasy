package xyz.nucleoid.fantasy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.util.List;
import java.util.function.BooleanSupplier;

class FantasyWorld extends ServerWorld {
    private boolean tickWhenEmpty = true;

    FantasyWorld(
            MinecraftServer server, ServerWorldProperties properties,
            RegistryKey<World> registryKey, DimensionType dimensionType,
            ChunkGenerator chunkGenerator, boolean debugWorld, long seed,
            List<Spawner> spawners, boolean tickTime
    ) {
        super(
                server, Util.getMainWorkerExecutor(), ((MinecraftServerAccess) server).getSession(),
                properties,
                registryKey, dimensionType,
                VoidWorldProgressListener.INSTANCE,
                chunkGenerator, debugWorld, seed,
                spawners, tickTime
        );
    }

    public void setTickWhenEmpty(boolean tickWhenEmpty) {
        this.tickWhenEmpty = tickWhenEmpty;
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        if (this.tickWhenEmpty || !this.isWorldEmpty()) {
            super.tick(shouldKeepTicking);
        }
    }

    private boolean isWorldEmpty() {
        return this.getPlayers().isEmpty() && this.getChunkManager().getLoadedChunkCount() > 0;
    }
}
