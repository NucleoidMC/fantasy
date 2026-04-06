package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.MinecraftServerExtension;
import xyz.nucleoid.fantasy.RuntimeServerClockManager;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {
    @WrapOperation(
            method = {"suggestTimeMarkers", "queryTime", "setTotalTicks", "addTime", "setTimeToTimeMarker", "setPaused", "setRate"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;")
    )
    private static ServerClockManager useRuntimeClockManager(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = {"queryTimelineTicks", "queryTimelineRepetitions"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager useRuntimeClockManagerByOrdinal(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true, ordinal = 0) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @Unique
    private static ServerClockManager getClockManager(MinecraftServer instance, Operation<ServerClockManager> original, Holder<WorldClock> clock) {
        if (RuntimeServerClockManager.hasClock(clock)) {
            return ((MinecraftServerExtension) instance).fantasy$clockManager();
        } else {
            return original.call(instance);
        }
    }
}
