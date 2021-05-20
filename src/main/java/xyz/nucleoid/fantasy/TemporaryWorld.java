package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.io.IOException;

class TemporaryWorld extends ServerWorld {
    private final Fantasy fantasy;
    final RuntimeWorldConfig config;

    TemporaryWorld(
            Fantasy fantasy,
            MinecraftServer server,
            RegistryKey<World> registryKey,
            RuntimeWorldConfig config
    ) {
        super(
                server, Util.getMainWorkerExecutor(), ((MinecraftServerAccess) server).getSession(),
                new RuntimeWorldProperties(server.getSaveProperties(), config),
                registryKey,
                Preconditions.checkNotNull(config.getDimensionType(server), "invalid dimension type!"),
                VoidWorldProgressListener.INSTANCE,
                config.getGenerator(),
                false,
                BiomeAccess.hashSeed(config.getSeed()),
                ImmutableList.of(),
                false
        );
        this.fantasy = fantasy;
        this.config = config;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.fantasy.enqueueWorldDeletion(this);
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean enabled) {
        if (!flush) {
            super.save(progressListener, flush, enabled);
        }
    }
}
