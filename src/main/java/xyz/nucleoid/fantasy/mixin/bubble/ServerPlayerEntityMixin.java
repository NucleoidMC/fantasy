package xyz.nucleoid.fantasy.mixin.bubble;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.fantasy.BubbleAccess;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow
    public abstract ServerWorld getServerWorld();

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void onTeleport(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (this.world != targetWorld) {
            if (BubbleAccess.isBubbleWorld(targetWorld)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void onMoveWorld(ServerWorld targetWorld, CallbackInfoReturnable<Entity> ci) {
        if (this.world != targetWorld) {
            if (BubbleAccess.isBubbleWorld(targetWorld)) {
                ci.setReturnValue(this);
            }
        }
    }
}
