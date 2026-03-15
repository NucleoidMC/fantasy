package xyz.nucleoid.fantasy;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.util.List;
import java.util.concurrent.Executor;

public class RuntimeLevel extends ServerLevel {
    final Style style;
    private boolean flat;
    @Nullable
    private GameRules rules;

    protected RuntimeLevel(MinecraftServer server, ResourceKey<Level> dimension, RuntimeLevelConfig config, Style style) {
        super(
                server, Util.backgroundExecutor(), ((MinecraftServerAccess) server).getStorageSource(),
                new RuntimeLevelData(server.getWorldData(), config),
                dimension,
                config.createDimensionOptions(server),
                false,
                BiomeManager.obfuscateSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime()
        );
        this.style = style;
        this.flat = config.isFlat().orElse(super.isFlat());

        if(!config.shouldMirrorOverworldGameRules()) {
            this.rules = new GameRules(server.getWorldData().enabledFeatures());
            config.getGameRules().applyTo(this.rules, null);
        }
    }

    protected RuntimeLevel(MinecraftServer server, Executor executor, LevelStorageSource.LevelStorageAccess levelStorage, ServerLevelData levelData, ResourceKey<Level> dimension, LevelStem levelStem, boolean isDebug, long biomeZoomSeed, List<CustomSpawner> customSpawners, boolean tickTime, Style style) {
        super(server, executor, levelStorage, levelData, dimension, levelStem, isDebug, biomeZoomSeed, customSpawners, tickTime);
        this.style = style;
    }

    @Override
    public GameRules getGameRules() {
        if(this.rules != null) {
            return this.rules;
        }
        return super.getGameRules();
    }

    @Override
    public long getSeed() {
        return ((RuntimeLevelData) this.levelData).config.getSeed();
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
        RuntimeLevel createLevel(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeLevelConfig config, Style style);
    }
}
