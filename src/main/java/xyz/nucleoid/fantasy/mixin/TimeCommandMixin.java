package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {
    @WrapOperation(
            method = {"suggestTimeMarkers", "queryTime", "setTotalTicks", "addTime", "setTimeToTimeMarker", "setPaused", "setRate", "queryTimelineTicks", "queryTimelineRepetitions"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;")
    )
    private static ServerClockManager useRuntimeClockManager(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true) CommandSourceStack source) {
        return source.getLevel().clockManager();
    }

}
