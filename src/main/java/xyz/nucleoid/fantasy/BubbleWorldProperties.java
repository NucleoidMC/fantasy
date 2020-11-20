package xyz.nucleoid.fantasy;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import xyz.nucleoid.fantasy.util.GameRuleStore;

public final class BubbleWorldProperties extends UnmodifiableLevelProperties {
    private final BubbleWorldConfig config;
    private final GameRules bubbleRules;

    public BubbleWorldProperties(SaveProperties saveProperties, BubbleWorldConfig config) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.config = config;
        this.bubbleRules = this.createBubbleRules(config);
    }

    private GameRules createBubbleRules(BubbleWorldConfig config) {
        GameRules bubbleRules = createDefaultRules();

        GameRuleStore rules = config.getGameRules();
        rules.applyTo(bubbleRules, null);

        return bubbleRules;
    }

    private static GameRules createDefaultRules() {
        GameRules rules = new GameRules();
        rules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        rules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
        rules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, null);
        return rules;
    }

    @Override
    public GameRules getGameRules() {
        return this.bubbleRules;
    }

    @Override
    public void setTimeOfDay(long timeOfDay) {
        this.config.setTimeOfDay(timeOfDay);
    }

    @Override
    public long getTimeOfDay() {
        return this.config.getTimeOfDay();
    }

    @Override
    public void setClearWeatherTime(int time) {
        this.config.setSunny(time);
    }

    @Override
    public int getClearWeatherTime() {
        return this.config.getSunnyTime();
    }

    @Override
    public void setRaining(boolean raining) {
        this.config.setRaining(raining);
    }

    @Override
    public boolean isRaining() {
        return this.config.isRaining();
    }

    @Override
    public void setRainTime(int time) {
        this.config.setRaining(time);
    }

    @Override
    public int getRainTime() {
        return this.config.getRainTime();
    }

    @Override
    public void setThundering(boolean thundering) {
        this.config.setThundering(thundering);
    }

    @Override
    public boolean isThundering() {
        return this.config.isThundering();
    }

    @Override
    public void setThunderTime(int time) {
        this.config.setThundering(time);
    }

    @Override
    public int getThunderTime() {
        return this.config.getThunderTime();
    }

    @Override
    public Difficulty getDifficulty() {
        return this.config.getDifficulty();
    }
}
