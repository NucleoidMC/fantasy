package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.util.GameRuleStore;

/**
 * A configuration describing how a runtime world should be constructed. This includes properties such as the dimension
 * type, chunk generator, and game rules.
 *
 * @see Fantasy
 */
public final class RuntimeWorldConfig {
    private long seed = 0;
    private RegistryKey<DimensionType> dimensionTypeKey = Fantasy.DEFAULT_DIM_TYPE;
    private RegistryEntry<DimensionType> dimensionType;
    private ChunkGenerator generator = null;
    private boolean shouldTickTime = false;
    private long timeOfDay = 6000;
    private Difficulty difficulty = Difficulty.NORMAL;
    private final GameRuleStore gameRules = new GameRuleStore();

    private int sunnyTime = Integer.MAX_VALUE;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;

    public RuntimeWorldConfig setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public RuntimeWorldConfig setDimensionType(RegistryEntry<DimensionType> dimensionType) {
        this.dimensionType = dimensionType;
        this.dimensionTypeKey = null;
        return this;
    }

    @Deprecated
    public RuntimeWorldConfig setDimensionType(DimensionType dimensionType) {
        this.dimensionType = RegistryEntry.of(dimensionType);
        this.dimensionTypeKey = null;
        return this;
    }

    public RuntimeWorldConfig setDimensionType(RegistryKey<DimensionType> dimensionType) {
        this.dimensionTypeKey = dimensionType;
        this.dimensionType = null;
        return this;
    }

    public RuntimeWorldConfig setGenerator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }
	
    public RuntimeWorldConfig setShouldTickTime(boolean shouldTickTime) {
        this.shouldTickTime = shouldTickTime;
        return this;
    }

    public RuntimeWorldConfig setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
        return this;
    }

    public RuntimeWorldConfig setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public RuntimeWorldConfig setGameRule(GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        this.gameRules.set(key, value);
        return this;
    }

    public RuntimeWorldConfig setGameRule(GameRules.Key<GameRules.IntRule> key, int value) {
        this.gameRules.set(key, value);
        return this;
    }

    public RuntimeWorldConfig setSunny(int sunnyTime) {
        this.sunnyTime = sunnyTime;
        this.raining = false;
        this.thundering = false;
        return this;
    }

    public RuntimeWorldConfig setRaining(int rainTime) {
        this.raining = rainTime > 0;
        this.rainTime = rainTime;
        return this;
    }

    public RuntimeWorldConfig setRaining(boolean raining) {
        this.raining = raining;
        return this;
    }

    public RuntimeWorldConfig setThundering(int thunderTime) {
        this.thundering = thunderTime > 0;
        this.thunderTime = thunderTime;
        return this;
    }

    public RuntimeWorldConfig setThundering(boolean thundering) {
        this.thundering = thundering;
        return this;
    }

    public long getSeed() {
        return this.seed;
    }

    public DimensionOptions createDimensionOptions(MinecraftServer server) {
        var dimensionType = this.resolveDimensionType(server);
        return new DimensionOptions(dimensionType, this.generator);
    }

    private RegistryEntry<DimensionType> resolveDimensionType(MinecraftServer server) {
        var dimensionType = this.dimensionType;
        if (dimensionType == null) {
            dimensionType = server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getEntry(this.dimensionTypeKey).orElse(null);
            Preconditions.checkNotNull(dimensionType, "invalid dimension type " + this.dimensionTypeKey);
        }
        return dimensionType;
    }

    @Nullable
    public ChunkGenerator getGenerator() {
        return this.generator;
    }
	
    public boolean shouldTickTime() {
        return this.shouldTickTime;
    }

    public long getTimeOfDay() {
        return this.timeOfDay;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public GameRuleStore getGameRules() {
        return this.gameRules;
    }

    public int getSunnyTime() {
        return this.sunnyTime;
    }

    public int getRainTime() {
        return this.rainTime;
    }

    public int getThunderTime() {
        return this.thunderTime;
    }

    public boolean isRaining() {
        return this.raining;
    }

    public boolean isThundering() {
        return this.thundering;
    }
}
