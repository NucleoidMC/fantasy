package xyz.nucleoid.fantasy;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.PackedClockStates;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Internal
public interface ServerClockManagerExtension {
    default void fantasy$setServer(MinecraftServer server) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default MinecraftServer fantasy$getServer() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default PackedClockStates fantasy$getPackedClockStates() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default void fantasy$registerTimeMarker(final ResourceKey<ClockTimeMarker> timeMarkerId, final ClockTimeMarker timeMarker) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default Map<Holder<WorldClock>, ServerClockManager.ClockInstance> fantasy$getClocks() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }
}
