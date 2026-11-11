package xyz.nucleoid.fantasy.fabric.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla sends rain packets to all players when rain starts in a level, even if they are not in it, meaning that if
 * it is possible to rain in the level they are in the rain effect will remain until the player changes dimension or
 * reconnects.
 * <p>
 * Fabric only: NeoForge already patches {@code advanceWeatherCycle} to only send these packets to the level's players.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelWeatherMixin {
    @Redirect(
            method = "advanceWeatherCycle",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void dontSendRainPacketsToAllLevels(PlayerList instance, Packet<?> packet) {
        instance.broadcastAll(packet, ((ServerLevel) (Object) this).dimension());
    }
}
