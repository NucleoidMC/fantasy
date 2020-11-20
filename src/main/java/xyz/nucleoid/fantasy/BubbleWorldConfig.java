package xyz.nucleoid.fantasy;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.util.GameRuleStore;

public final class BubbleWorldConfig {
    private long seed = 0;
    private RegistryKey<DimensionType> dimensionType = DimensionType.OVERWORLD_REGISTRY_KEY;
    private ChunkGenerator generator = null;
    private GameMode defaultGameMode = GameMode.ADVENTURE;
    private BubbleWorldSpawner spawner = BubbleWorldSpawner.at(new Vec3d(0.0, 64.0, 0.0));
    private long timeOfDay = 6000;
    private Difficulty difficulty = Difficulty.NORMAL;
    private final GameRuleStore gameRules = new GameRuleStore();

    private int sunnyTime = Integer.MAX_VALUE;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;

    public BubbleWorldConfig setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public BubbleWorldConfig setDimensionType(RegistryKey<DimensionType> dimensionType) {
        this.dimensionType = dimensionType;
        return this;
    }

    public BubbleWorldConfig setGenerator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    public BubbleWorldConfig setDefaultGameMode(GameMode gameMode) {
        this.defaultGameMode = gameMode;
        return this;
    }

    public BubbleWorldConfig setSpawnAt(Vec3d spawnPos) {
        return this.setSpawner(BubbleWorldSpawner.at(spawnPos));
    }

    public BubbleWorldConfig setSpawner(BubbleWorldSpawner spawner) {
        this.spawner = spawner;
        return this;
    }

    public BubbleWorldConfig setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
        return this;
    }

    public BubbleWorldConfig setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public BubbleWorldConfig setGameRule(GameRules.Key<GameRules.BooleanRule> key, boolean value) {
        this.gameRules.set(key, value);
        return this;
    }

    public BubbleWorldConfig setGameRule(GameRules.Key<GameRules.IntRule> key, int value) {
        this.gameRules.set(key, value);
        return this;
    }

    public BubbleWorldConfig setSunny(int sunnyTime) {
        this.sunnyTime = sunnyTime;
        this.raining = false;
        this.thundering = false;
        return this;
    }

    public BubbleWorldConfig setRaining(int rainTime) {
        this.raining = rainTime > 0;
        this.rainTime = rainTime;
        return this;
    }

    public BubbleWorldConfig setRaining(boolean raining) {
        this.raining = raining;
        return this;
    }

    public BubbleWorldConfig setThundering(int thunderTime) {
        this.thundering = thunderTime > 0;
        this.thunderTime = thunderTime;
        return this;
    }

    public BubbleWorldConfig setThundering(boolean thundering) {
        this.thundering = thundering;
        return this;
    }

    public long getSeed() {
        return this.seed;
    }

    public RegistryKey<DimensionType> getDimensionType() {
        return this.dimensionType;
    }

    @Nullable
    public ChunkGenerator getGenerator() {
        return this.generator;
    }

    public GameMode getDefaultGameMode() {
        return this.defaultGameMode;
    }

    public BubbleWorldSpawner getSpawner() {
        return this.spawner;
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
