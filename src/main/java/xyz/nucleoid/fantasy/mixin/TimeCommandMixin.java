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
    @WrapOperation(method = "suggestTimeMarkers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager suggestRunTimeMarkers(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "queryTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager queryRunTime(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "queryTimelineTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager queryRunTimelineTicks(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true, ordinal = 0) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "queryTimelineRepetitions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager queryRunTimelineRepetitions(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true, ordinal = 0) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "setTotalTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager setRunTotalTicks(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "addTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager addTime(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "setTimeToTimeMarker", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager setTimeToTimeMarker(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "setPaused", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager setPaused(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
        return getClockManager(instance, original, clock);
    }

    @WrapOperation(method = "setRate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private static ServerClockManager setRate(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) Holder<WorldClock> clock) {
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
