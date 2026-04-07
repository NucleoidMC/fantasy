package xyz.nucleoid.fantasy.mixin.clock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
    @Final
    private Map<Holder<WorldClock>, ServerClockManager.ClockInstance> clocks;

    @Override
    public Map<Holder<WorldClock>, ServerClockManager.ClockInstance> fantasy$getClocks() {
        return this.clocks;
    }

    @WrapOperation(method = "modifyClock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void updateTimeOnlyOverworld(PlayerList instance, Packet<?> packet, Operation<Void> original) {
        for(ServerPlayer player : instance.getPlayers()) {
            if (player.level().clockManager() == (Object) this) {
                player.connection.send(packet);
            }
        }
    }
}
