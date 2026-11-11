package xyz.nucleoid.fantasy.mixin;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.fantasy.FantasyLevelAccess;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
    @Shadow
    @Final
    ServerLevel level;

    @Inject(method = "pollTask", at = @At("HEAD"), cancellable = true)
    private void executeQueuedTasks(CallbackInfoReturnable<Boolean> ci) {
        if (!((FantasyLevelAccess) this.level).fantasy$shouldTick()) {
            ci.setReturnValue(false);
        }
    }
}
