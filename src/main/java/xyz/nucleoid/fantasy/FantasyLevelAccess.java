package xyz.nucleoid.fantasy;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface FantasyLevelAccess {
    void fantasy$setTickWhenEmpty(boolean tickWhenEmpty);

    boolean fantasy$shouldTick();
}
