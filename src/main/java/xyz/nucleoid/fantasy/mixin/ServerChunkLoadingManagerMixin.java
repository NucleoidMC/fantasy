package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.util.ChunkGeneratorSettingsProvider;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {

    @Shadow
    private ChunkGenerator getChunkGenerator() {
    	// Shadowed Method
    	return null;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;createMissingSettings()Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;"))
    private ChunkGeneratorSettings fantasy$useProvidedChunkGeneratorSettings(Operation<ChunkGeneratorSettings> original) {
    	if (this.getChunkGenerator() instanceof ChunkGeneratorSettingsProvider provider) {
            ChunkGeneratorSettings settings = provider.getSettings();
            if (settings != null) return settings;
        }

        return original.call();
    }
}
