package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.FantasyLevelAccess;

import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xyz.nucleoid.fantasy.MinecraftServerExtension;
import xyz.nucleoid.fantasy.RuntimeLevel;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements FantasyLevelAccess {
    @Unique
    private static final int TICK_TIMEOUT = 20 * 15;

    @Unique
    private boolean fantasy$tickWhenEmpty = true;
    @Unique
    private int fantasy$tickTimeout;

    @Shadow
    public abstract List<ServerPlayer> players();

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @Shadow
    @Final
    private MinecraftServer server;

    @Override
    public void fantasy$setTickWhenEmpty(boolean tickWhenEmpty) {
        this.fantasy$tickWhenEmpty = tickWhenEmpty;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        boolean shouldTick = this.fantasy$tickWhenEmpty || !this.isLevelEmpty();
        if (shouldTick) {
            this.fantasy$tickTimeout = TICK_TIMEOUT;
        } else if (this.fantasy$tickTimeout-- <= 0) {
            ci.cancel();
        }
    }

    @Override
    public boolean fantasy$shouldTick() {
        boolean shouldTick = this.fantasy$tickWhenEmpty || !this.isLevelEmpty();
        return shouldTick || this.fantasy$tickTimeout > 0;
    }

    @Unique
    private boolean isLevelEmpty() {
        return this.players().isEmpty() && this.getChunkSource().getLoadedChunksCount() <= 0;
    }

    @Redirect(
            method = "advanceWeatherCycle",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
            )

    )
    private void dontSendRainPacketsToAllLevels(PlayerList instance, Packet<?> packet) {
        // Vanilla sends rain packets to all players when rain starts in a world,
        // even if they are not in it, meaning that if it is possible to rain in the world they are in
        // the rain effect will remain until the player changes dimension or reconnects.
        // TODO: check if this still behaves the same following 26.1 changes
        instance.broadcastAll(packet, this.getChunkSource().getLevel().dimension());
    }
    
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Z"))
    private boolean onTickMoveToTimeMarkerWakeUp(ServerClockManager instance, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> timeMarkerId, Operation<Boolean> original) {
        if ((Object) this instanceof RuntimeLevel) {
            return ((MinecraftServerExtension) this.server)
                    .fantasy$clockManager()
                    .moveToTimeMarker(clock, ClockTimeMarkers.WAKE_UP_FROM_SLEEP);
        } else {
            return original.call(instance, clock, timeMarkerId);
        }
    }
}
