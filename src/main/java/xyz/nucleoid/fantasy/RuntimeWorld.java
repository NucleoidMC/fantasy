package xyz.nucleoid.fantasy;

import com.google.common.collect.ImmutableList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

import java.util.List;
import java.util.concurrent.Executor;

public class RuntimeWorld extends ServerWorld {
    final Style style;
    private boolean flat;

    protected RuntimeWorld(MinecraftServer server, RegistryKey<World> registryKey, RuntimeWorldConfig config, Style style) {
        super(
                server, Util.getMainWorkerExecutor(), ((MinecraftServerAccess) server).getSession(),
                new RuntimeWorldProperties(server.getSaveProperties(), config),
                registryKey,
                config.createDimensionOptions(server),
                VoidWorldProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime(),
                null
        );
        this.style = style;
        this.flat = config.isFlat().orElse(super.isFlat());
    }


    protected RuntimeWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState, Style style) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
        this.style = style;
    }


    @Override
    public long getSeed() {
        return ((RuntimeWorldProperties) this.properties).config.getSeed();
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean enabled) {
        if (this.style == Style.PERSISTENT || !flush) {
            super.save(progressListener, flush, enabled);
        }
    }

    @Override
    public boolean isFlat() {
        return this.flat;
    }

    public enum Style {
        PERSISTENT,
        TEMPORARY
    }

    public interface Constructor {
        RuntimeWorld createWorld(MinecraftServer server, RegistryKey<World> registryKey, RuntimeWorldConfig config, Style style);
    }
}
