package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.util.ChunkGeneratorSettingsProvider;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/core/HolderGetter;JZLnet/minecraft/world/level/block/state/BlockState;ILnet/minecraft/world/level/levelgen/NoiseRouter;)Lnet/minecraft/world/level/levelgen/RandomState;"))
    private RandomState fantasy$useProvidedChunkGeneratorSettings(HolderGetter<NormalNoise> noises, long seed, boolean useLegacyRandom, BlockState defaultBlock, int seaLevel, NoiseRouter noiseRouter, Operation<RandomState> original, @Local(argsOnly = true) ChunkGenerator chunkGenerator) {
    	if (chunkGenerator instanceof ChunkGeneratorSettingsProvider provider) {
            NoiseGeneratorSettings settings = provider.getSettings();
            if (settings != null) return RandomState.create(noises, seed, settings);
        }

        return original.call(noises, seed, useLegacyRandom, defaultBlock, seaLevel, noiseRouter);
    }
}
