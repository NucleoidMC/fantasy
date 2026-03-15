package xyz.nucleoid.fantasy;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;

public final class RuntimeLevelData extends DerivedLevelData {
    final RuntimeLevelConfig config;

    public RuntimeLevelData(WorldData worldData, RuntimeLevelConfig config) {
        super(worldData, worldData.overworldData());
        this.config = config;
    }

    @Override
    public void setGameTime(long time) {
        this.config.setGameTime(time);
    }

    @Override
    public Difficulty getDifficulty() {
        if (this.config.shouldMirrorOverworldDifficulty()) {
            return super.getDifficulty();
        }
        return this.config.getDifficulty();
    }
}
