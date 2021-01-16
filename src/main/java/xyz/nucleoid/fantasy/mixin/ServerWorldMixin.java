package xyz.nucleoid.fantasy.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.FantasyWorldAccess;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements FantasyWorldAccess {
    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();
    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Unique
    private boolean tickWhenEmpty = true;

    @Override
    public void setTickWhenEmpty(boolean tickWhenEmpty) {
        this.tickWhenEmpty = tickWhenEmpty;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!this.tickWhenEmpty && this.isWorldEmpty()) {
            ci.cancel();
        }
    }

    private boolean isWorldEmpty() {
        return this.getPlayers().isEmpty() && this.getChunkManager().getLoadedChunkCount() > 0;
    }
}
