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
    private static final int TICK_TIMEOUT = 20 * 15;

    @Unique
    private boolean fantasy$tickWhenEmpty = true;
    @Unique
    private int fantasy$tickTimeout;

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Override
    public void fantasy$setTickWhenEmpty(boolean tickWhenEmpty) {
        this.fantasy$tickWhenEmpty = tickWhenEmpty;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        boolean shouldTick = this.fantasy$tickWhenEmpty || !this.isWorldEmpty();
        if (shouldTick) {
            this.fantasy$tickTimeout = TICK_TIMEOUT;
        } else if (this.fantasy$tickTimeout-- <= 0) {
            ci.cancel();
        }
    }

    @Override
    public boolean fantasy$shouldTick() {
        boolean shouldTick = this.fantasy$tickWhenEmpty || !this.isWorldEmpty();
        return shouldTick || this.fantasy$tickTimeout > 0;
    }

    private boolean isWorldEmpty() {
        return this.getPlayers().isEmpty() && this.getChunkManager().getLoadedChunkCount() <= 0;
    }
}
