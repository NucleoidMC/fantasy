package xyz.nucleoid.fantasy;

import net.minecraft.core.Holder;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Internal
public interface ServerClockManagerExtension {
    default Map<Holder<WorldClock>, ServerClockManager.ServerClockInstance> fantasy$getClocks() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }
}
