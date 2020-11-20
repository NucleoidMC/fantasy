package xyz.nucleoid.fantasy.mixin.bubble;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.BubbleAccess;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        BubbleAccess.removePlayer((ServerWorld) player.world, player);
    }
}
