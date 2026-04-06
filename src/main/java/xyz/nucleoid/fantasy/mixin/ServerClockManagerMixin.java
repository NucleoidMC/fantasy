package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.PackedClockStates;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.ServerClockManagerExtension;

import java.util.Map;

@Mixin(ServerClockManager.class)
public abstract class ServerClockManagerMixin implements ServerClockManagerExtension {
    @Shadow
    private MinecraftServer server;

    @Shadow
    @Final
    private PackedClockStates packedClockStates;

    @Shadow
    protected abstract ServerClockManager.ClockInstance getInstance(Holder<WorldClock> definition);

    @Shadow
    protected abstract void registerTimeMarker(ResourceKey<ClockTimeMarker> timeMarkerId, ClockTimeMarker timeMarker);

    @Shadow
    @Final
    private Map<Holder<WorldClock>, ServerClockManager.ClockInstance> clocks;

    @Override
    public void fantasy$setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public MinecraftServer fantasy$getServer() {
        return this.server;
    }

    @Override
    public PackedClockStates fantasy$getPackedClockStates() {
        return this.packedClockStates;
    }

    @Override
    public void fantasy$registerTimeMarker(ResourceKey<ClockTimeMarker> timeMarkerId, ClockTimeMarker timeMarker) {
        this.registerTimeMarker(timeMarkerId, timeMarker);
    }

    @Override
    public Map<Holder<WorldClock>, ServerClockManager.ClockInstance> fantasy$getClocks() {
        return this.clocks;
    }

    @WrapOperation(method = "modifyClock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void updateTimeOnlyOverworld(PlayerList instance, Packet<?> packet, Operation<Void> original) {
        instance.broadcastAll(packet, this.server.overworld().dimension());
    }
}
