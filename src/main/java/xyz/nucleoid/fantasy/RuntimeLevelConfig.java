package xyz.nucleoid.fantasy;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.clock.PackedClockStates;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.util.GameRuleStore;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * A configuration describing how a runtime level should be constructed. This includes properties such as the dimension
 * type, chunk generator, and game rules.
 *
 * @see Fantasy
 */
public final class RuntimeLevelConfig {
    private long seed = 0;
    private ResourceKey<DimensionType> dimensionTypeKey = Fantasy.DEFAULT_DIM_TYPE;
    private Holder<DimensionType> dimensionType;
    private ChunkGenerator generator = null;
    private boolean shouldTickTime = false;
    private Difficulty difficulty = Difficulty.NORMAL;
    private final GameRuleStore gameRules = new GameRuleStore();
    private boolean mirrorOverworldGameRules = false;
    private boolean mirrorOverworldDifficulty = false;
    private boolean mirrorOverworldClocks = false;
    private RuntimeLevel.Constructor levelConstructor = RuntimeLevel::new;
    private Function<BooleanSupplier, RuntimeClockManager> clockManagerConstructor = s -> new RuntimeClockManager(PackedClockStates.EMPTY, s);

    private long gameTime = 0;
    private TriState flat = TriState.DEFAULT;

    /**
     * Sets the level seed
     *
     * @param seed The level seed to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Sets the level constructor
     *
     * @param constructor The level constructor to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setLevelConstructor(RuntimeLevel.Constructor constructor) {
        this.levelConstructor = constructor;
        return this;
    }

    /**
     * Sets the clock manager constructor to {@link RuntimeClockManager} with provided clock states as the default.
     *
     * @param clockStates The clock states to initialize the clock manager from
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setClockManagerConstructor(PackedClockStates clockStates) {
        this.clockManagerConstructor = s -> new RuntimeClockManager(clockStates, s);
        return this;
    }

    /**
     * Sets the clock manager constructor
     *
     * @param clockManagerConstructor The clock manager constructor to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setClockManagerConstructor(Function<BooleanSupplier, RuntimeClockManager> clockManagerConstructor) {
        this.clockManagerConstructor = clockManagerConstructor;
        return this;
    }

    /**
     * Sets the level dimension type
     *
     * @param dimensionType The dimension type to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setDimensionType(Holder<DimensionType> dimensionType) {
        this.dimensionType = dimensionType;
        this.dimensionTypeKey = null;
        return this;
    }

    /**
     * Sets the level dimension type
     *
     * @param dimensionType The dimension type to use
     *
     * @deprecated Pleas use {@link RuntimeLevelConfig#setDimensionType(ResourceKey)}
     * or {@link RuntimeLevelConfig#setDimensionType(Holder)} instead
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    @Deprecated
    public RuntimeLevelConfig setDimensionType(DimensionType dimensionType) {
        this.dimensionType = Holder.direct(dimensionType);
        this.dimensionTypeKey = null;
        return this;
    }

    /**
     * Sets the level dimension type
     *
     * @param dimensionType The dimension type to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setDimensionType(ResourceKey<DimensionType> dimensionType) {
        this.dimensionTypeKey = dimensionType;
        this.dimensionType = null;
        return this;
    }

    /**
     * Sets the level chunk generator
     *
     * @param generator The chunk generator to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setGenerator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    /**
     * Defines whenever the level should tick time.
     * <br/>
     * Setting this set's the {@link GameRules#ADVANCE_TIME}
     * gamerule for the level to avoid jitter
     * <br/>
     * <br/>
     * <i>The gamerule does not have effect if {@link RuntimeLevelConfig#mirrorOverworldGameRules} is set to true</i>
     *
     * @param shouldTickTime Whenever the level should tick the time
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setShouldTickTime(boolean shouldTickTime) {
        this.shouldTickTime = shouldTickTime;
        this.gameRules.set(GameRules.ADVANCE_TIME, shouldTickTime);
        return this;
    }

    /**
     * Sets the level's game time
     *
     * @param gameTime The new time of the game
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setGameTime(long gameTime) {
        this.gameTime = gameTime;
        return this;
    }

    /**
     * Sets the level difficulty
     *
     * @param difficulty The difficulty to use
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Modifies a gamerule
     * <br/>
     * <b>Does nothing if {@link RuntimeLevelConfig#mirrorOverworldGameRules} is true</b>
     *
     * @param key The gamerule to modify
     * @param value The value of the gamerule
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public <T> RuntimeLevelConfig setGameRule(GameRule<T> key, T value) {
        this.gameRules.set(key, value);
        return this;
    }

    /**
     * Defines if the level should follow the overworld gamerules or not
     *
     * @param mirror Whenever it should mirror or not
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setMirrorOverworldGameRules(boolean mirror) {
        this.mirrorOverworldGameRules = mirror;
        return this;
    }

    /**
     * Defines if the level should follow the overworld difficulty or not
     *
     * @param mirror Whenever it should mirror or not
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setMirrorOverworldDifficulty(boolean mirror) {
        this.mirrorOverworldDifficulty = mirror;
        return this;
    }

    /**
     * Defines if the level should follow the overworld clock values or not
     *
     * @param mirror Whenever it should mirror or not
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setMirrorOverworldClocks(boolean mirror) {
        this.mirrorOverworldClocks = mirror;
        return this;
    }

    /**
     * Defines if the level is a flat level or not
     *
     * @param state If the level should be flat, not flat or use the default value
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setFlat(TriState state) {
        this.flat = state;
        return this;
    }

    /**
     * Defines if the level is a flat level or not
     *
     * @param state If the level should be flat or not
     *
     * @return The same instance of {@link RuntimeLevelConfig}
     */
    public RuntimeLevelConfig setFlat(boolean state) {
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
    public LevelStem createDimensionOptions(MinecraftServer server) {
        var dimensionType = this.resolveDimensionType(server);
        return new LevelStem(dimensionType, this.generator);
    }

    /**
     * Resolves the dimension type from the server
     *
     * @return The dimension type
     */
    private Holder<DimensionType> resolveDimensionType(MinecraftServer server) {
        var dimensionType = this.dimensionType;
        if (dimensionType == null) {
            dimensionType = server.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).get(this.dimensionTypeKey).orElse(null);
            Preconditions.checkNotNull(dimensionType, "invalid dimension type " + this.dimensionTypeKey);
        }
        return dimensionType;
    }

    @Nullable
    public ChunkGenerator getGenerator() {
        return this.generator;
    }

    public RuntimeLevel.Constructor getLevelConstructor() {
        return this.levelConstructor;
    }

    public boolean shouldTickTime() {
        return this.shouldTickTime;
    }

    /**
     * Gets the current configured difficulty
     * <br/>
     * <b>It may not reflect the real difficulty, also check </b>{@link RuntimeLevelConfig#shouldMirrorOverworldDifficulty()}
     *
     * @return The current difficulty stored in the config
     */
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    /**
     * Gets the current configured gamerules
     * <br/>
     * <b>It may not reflect the real gamerules, also check </b>{@link RuntimeLevelConfig#shouldMirrorOverworldGameRules()}
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

    public boolean shouldMirrorOverworldClocks() {
        return this.mirrorOverworldClocks;
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public TriState isFlat() {
        return this.flat;
    }

    public ServerClockManager getClockManager(MinecraftServer server, GameRules gameRules) {
        if (this.mirrorOverworldClocks) {
            return server.clockManager();
        }

        var t = this.clockManagerConstructor.apply(() -> gameRules.get(GameRules.ADVANCE_TIME));
        t.init(server);

        return t;
    }
}
