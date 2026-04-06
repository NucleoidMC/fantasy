package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.RuntimeLevel;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ServerClockManager;createFullSyncPacket()Lnet/minecraft/network/protocol/game/ClientboundSetTimePacket;"))
    private ClientboundSetTimePacket onSyncLevelTime(ServerClockManager instance, Operation<ClientboundSetTimePacket> original, @Local(argsOnly = true) ServerLevel level) {
        ClientboundSetTimePacket packet = original.call(instance);

        if (level instanceof RuntimeLevel runtimeLevel) {
            int period = runtimeLevel.timeline.value().periodTicks().orElse(24000);
            return new ClientboundSetTimePacket(
                    packet.gameTime() - (packet.gameTime() % period),
                    runtimeLevel.clockManager().createFullSyncPacket().clockUpdates()
            );
        } else {
            return packet;
        }
    }
}
