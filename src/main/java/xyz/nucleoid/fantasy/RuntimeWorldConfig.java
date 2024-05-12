package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.util.TriState;
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
    private boolean mirrorOverworldGameRules = false;
    private boolean mirrorOverworldDifficulty = false;
    private RuntimeWorld.Constructor worldConstructor = RuntimeWorld::new;

    private int sunnyTime = Integer.MAX_VALUE;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private TriState flat = TriState.DEFAULT;

    /**
     * Sets the world seed
     *
     * @param seed The world seed to use
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Sets the world constructor
     *
     * @param constructor The world constructor to use
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setWorldConstructor(RuntimeWorld.Constructor constructor) {
        this.worldConstructor = constructor;
        return this;
    }

    /**
     * Sets the world dimension type
     *
     * @param dimensionType The dimension type to use
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setDimensionType(RegistryEntry<DimensionType> dimensionType) {
        this.dimensionType = dimensionType;
        this.dimensionTypeKey = null;
        return this;
    }

    /**
     * Sets the world dimension type
     *
     * @param dimensionType The dimension type to use
     *
     * @deprecated Pleas use {@link RuntimeWorldConfig#setDimensionType(RegistryKey)}
     * or {@link RuntimeWorldConfig#setDimensionType(RegistryEntry)} instead
     *
     * @return The same instance of RuntimeWorldConfig
     */
    @Deprecated
    public RuntimeWorldConfig setDimensionType(DimensionType dimensionType) {
        this.dimensionType = RegistryEntry.of(dimensionType);
        this.dimensionTypeKey = null;
        return this;
    }

    /**
     * Sets the world dimension type
     *
     * @param dimensionType The dimension type to use
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setDimensionType(RegistryKey<DimensionType> dimensionType) {
        this.dimensionTypeKey = dimensionType;
        this.dimensionType = null;
        return this;
    }

    /**
     * Sets the world chunk generator
     *
     * @param generator The chunk generator to use
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setGenerator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    /**
     * Defines whenever the world should tick time.
     * <br/>
     * Setting this set's the {@link GameRules#DO_DAYLIGHT_CYCLE}
     * gamerule for the world to avoid jitter
     * <br/>
     * <br/>
     * <i>The gamerule does not have effect if {@link RuntimeWorldConfig#mirrorOverworldGameRules} is set to true</i>
     *
     * @param shouldTickTime Whenever the world should tick the time
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setShouldTickTime(boolean shouldTickTime) {
        this.shouldTickTime = shouldTickTime;
        this.gameRules.set(GameRules.DO_DAYLIGHT_CYCLE, shouldTickTime);
        return this;
    }

    /**
     * Sets the world's day time
     *
     * @param timeOfDay The new time of the day
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
        return this;
    }

    /**
     * Sets the world difficulty
     *
     * @param difficulty The difficulty to use
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Modifies a gamerule
     * <br/>
     * <b>Does nothing if {@link RuntimeWorldConfig#mirrorOverworldGameRules} is true</b>
     *
     * @param key The gamerule to modify
     * @param value The value of the gamerule
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setGameRule(GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        this.gameRules.set(key, value);
        return this;
    }

    /**
     * Modifies a gamerule
     * <br/>
     * <b>Does nothing if {@link RuntimeWorldConfig#mirrorOverworldGameRules} is true</b>
     *
     * @param key The gamerule to modify
     * @param value The value of the gamerule
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setGameRule(GameRules.Key<GameRules.IntRule> key, int value) {
        this.gameRules.set(key, value);
        return this;
    }

    /**
     * Defines if the world should follow the overworld gamurules or not
     *
     * @param mirror Whenever it should mirror or not
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setMirrorOverworldGameRules(boolean mirror) {
        this.mirrorOverworldGameRules = mirror;
        return this;
    }

    /**
     * Defines if the world should follow the overworld difficulty or not
     *
     * @param mirror Whenever it should mirror or not
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setMirrorOverworldDifficulty(boolean mirror) {
        this.mirrorOverworldDifficulty = mirror;
        return this;
    }

    /**
     * Modifies the weather to sunny
     *
     * @param sunnyTime For how many ticks it should be sunny
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setSunny(int sunnyTime) {
        this.sunnyTime = sunnyTime;
        this.raining = false;
        this.thundering = false;
        return this;
    }

    /**
     * Modifies the weather to rainy
     *
     * @param rainTime For how many ticks it should be raining
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setRaining(int rainTime) {
        this.raining = rainTime > 0;
        this.rainTime = rainTime;
        return this;
    }

    /**
     * Toggles the rain in the world
     *
     * @param raining Whenever it should be raining or not
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setRaining(boolean raining) {
        this.raining = raining;
        return this;
    }


    /**
     * Modifies the weather to thundering
     *
     * @param thunderTime For how many ticks it should be thundering
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setThundering(int thunderTime) {
        this.thundering = thunderTime > 0;
        this.thunderTime = thunderTime;
        return this;
    }

    /**
     * Toggles the thunder in the world
     *
     * @param thundering Whenever it should be thundering or not
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setThundering(boolean thundering) {
        this.thundering = thundering;
        return this;
    }

    /**
     * Defines if the world is a flat world or not
     *
     * @param state If the world should be flat, not flat or use the default value
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setFlat(TriState state) {
        this.flat = state;
        return this;
    }

    /**
     * Defines if the world is a flat world or not
     *
     * @param state If the world should be flat or not
     *
     * @return The same instance of RuntimeWorldConfig
     */
    public RuntimeWorldConfig setFlat(boolean state) {
        return this.setFlat(TriState.of(state));
    }

    public long getSeed() {
        return this.seed;
    }

    /**
     * Creates new dimension options from the server
     *
     * @return The new dimension options
     */
    public DimensionOptions createDimensionOptions(MinecraftServer server) {
        var dimensionType = this.resolveDimensionType(server);
        return new DimensionOptions(dimensionType, this.generator);
    }

    /**
     * Resolves the dimension type from the server
     *
     * @return The dimension type
     */
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

    public RuntimeWorld.Constructor getWorldConstructor() {
        return this.worldConstructor;
    }

    public boolean shouldTickTime() {
        return this.shouldTickTime;
    }

    public long getTimeOfDay() {
        return this.timeOfDay;
    }

    /**
     * Gets the current configured difficulty
     * <br/>
     * <b>It may not reflect the real difficulty, also check </b>{@link RuntimeWorldConfig#shouldMirrorOverworldDifficulty()}
     *
     * @return The current difficulty stored in the config
     */
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    /**
     * Gets the current configured gamerules
     * <br/>
     * <b>It may not reflect the real gamerules, also check </b>{@link RuntimeWorldConfig#shouldMirrorOverworldGameRules()}
     *
     * @return The current gamerules stored in the config
     */
    public GameRuleStore getGameRules() {
        return this.gameRules;
    }

    public boolean shouldMirrorOverworldGameRules() {
        return this.mirrorOverworldGameRules;
    }

    public boolean shouldMirrorOverworldDifficulty() {
        return this.mirrorOverworldDifficulty;
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

    public TriState isFlat() {
        return this.flat;
    }
}
