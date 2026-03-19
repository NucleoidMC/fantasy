package xyz.nucleoid.fantasy;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;

public final class RuntimeWorldProperties extends DerivedLevelData {
    final RuntimeWorldConfig config;

    public RuntimeWorldProperties(WorldData saveProperties, RuntimeWorldConfig config) {
        super(saveProperties, saveProperties.overworldData());
        this.config = config;
    }


    @Override
    public void setGameTime(long timeOfDay) {
        this.config.setTimeOfDay(timeOfDay);
    }

    @Override
    public long getGameTime() {
        return this.config.getTimeOfDay();
    }

    @Override
    public Difficulty getDifficulty() {
        if (this.config.shouldMirrorOverworldDifficulty()) {
            return super.getDifficulty();
        }
        return this.config.getDifficulty();
    }
}
