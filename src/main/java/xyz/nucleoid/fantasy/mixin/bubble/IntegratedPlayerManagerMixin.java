package xyz.nucleoid.fantasy.mixin.bubble;

import net.minecraft.server.integrated.IntegratedPlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.BubbleAccess;

@Mixin(IntegratedPlayerManager.class)
public abstract class IntegratedPlayerManagerMixin {
    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void savePlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        if (BubbleAccess.isPlayedBubbled(player)) {
            ci.cancel();
        }
    }
}
