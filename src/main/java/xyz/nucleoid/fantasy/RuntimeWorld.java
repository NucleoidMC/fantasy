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
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.util.List;
import java.util.concurrent.Executor;

public class RuntimeWorld extends ServerLevel {
    final Style style;
    private boolean flat;
    private final GameRules rules;
    private final WeatherData weatherData;

    protected RuntimeWorld(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeWorldConfig config, Style style) {
        super(
                server, Util.backgroundExecutor(), ((MinecraftServerAccess) server).getStorageSource(),
                new RuntimeWorldProperties(server.getWorldData(), config),
                registryKey,
                config.createDimensionOptions(server),
                false,
                BiomeManager.obfuscateSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime()
        );
        this.style = style;
        this.flat = config.isFlat().orElse(super.isFlat());
        this.rules = new GameRules(server.getWorldData().enabledFeatures());
        config.getGameRules().applyTo(this.rules, null);
        this.weatherData = new RuntimeWeatherData(config);

    }

    protected RuntimeWorld(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, LevelStem dimensionOptions, boolean debugWorld, long seed, List<CustomSpawner> spawners, boolean shouldTickTime, Style style) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, debugWorld, seed, spawners, shouldTickTime);
        this.style = style;
        this.rules = new GameRules(server.getWorldData().enabledFeatures());
        this.weatherData = server.getWeatherData();
    }


    @Override
    public long getSeed() {
        return ((RuntimeWorldProperties) this.levelData).config.getSeed();
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean enabled) {
        if (this.style == Style.PERSISTENT || !flush) {
            super.save(progressListener, flush, enabled);
        }
    }

    /**
    * Only use the time update code from super as the immutable world proerties runtime dimensions breaks scheduled functions
    */
    @Override
    protected void tickTime() {
        if (this.tickTime) {
            if (this.getGameRules().get(GameRules.ADVANCE_TIME)) {
                this.serverLevelData.setGameTime(this.levelData.getGameTime() + 1L);
            }
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

    @Override
    public @NonNull GameRules getGameRules() {
        RuntimeWorldProperties properties = (RuntimeWorldProperties) this.levelData;
        if (properties.config.shouldMirrorOverworldGameRules()) {
            return super.getGameRules();
        }
        return this.rules;
    }

    @Override
    public WeatherData getWeatherData() {
        return this.weatherData;
    }

    public interface Constructor {
        RuntimeWorld createWorld(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeWorldConfig config, Style style);
    }
}
