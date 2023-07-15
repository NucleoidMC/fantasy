package xyz.nucleoid.fantasy.mixin;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.fantasy.util.ChunkGeneratorSettingsProvider;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
    @Shadow
    @Final
    private ChunkGenerator chunkGenerator;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;createMissingSettings()Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;"))
    private ChunkGeneratorSettings fantasy$useProvidedChunkGeneratorSettings() {
        if (this.chunkGenerator instanceof ChunkGeneratorSettingsProvider provider) {
            ChunkGeneratorSettings settings = provider.getSettings();
            if (settings != null) return settings;
        }

        return ChunkGeneratorSettings.createMissingSettings();
    }
}
