package xyz.nucleoid.fantasy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class RuntimeLevelHandle {
    private final Fantasy fantasy;
    private final ServerLevel level;

    RuntimeLevelHandle(Fantasy fantasy, ServerLevel level) {
        this.fantasy = fantasy;
        this.level = level;
    }

    public void setTickWhenEmpty(boolean tickWhenEmpty) {
        ((FantasyLevelAccess) this.level).fantasy$setTickWhenEmpty(tickWhenEmpty);
    }

    /**
     * Deletes the level, including all stored files
     */
    public void delete() {
        this.fantasy.enqueueLevelDeletion(this.level);
    }

    /**
     * Unloads the level. It only deletes the files if the level is temporary.
     */
    public void unload() {
        if (this.level instanceof RuntimeLevel runtimeLevel && runtimeLevel.style == RuntimeLevel.Style.TEMPORARY) {
            this.fantasy.enqueueLevelDeletion(this.level);
        } else {
            this.fantasy.enqueueLevelUnloading(this.level);
        }
    }

    public ServerLevel asLevel() {
        return this.level;
    }

    public ResourceKey<Level> getRegistryKey() {
        return this.level.dimension();
    }
}
