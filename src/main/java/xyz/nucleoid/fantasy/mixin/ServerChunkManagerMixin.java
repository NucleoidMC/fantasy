package xyz.nucleoid.fantasy.mixin;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.fantasy.FantasyWorldAccess;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Shadow
    @Final
    private ServerWorld world;

    @Inject(method = "executeQueuedTasks", at = @At("HEAD"), cancellable = true)
    private void executeQueuedTasks(CallbackInfoReturnable<Boolean> ci) {
        if (!((FantasyWorldAccess) this.world).fantasy$shouldTick()) {
            ci.setReturnValue(false);
        }
    }
}
