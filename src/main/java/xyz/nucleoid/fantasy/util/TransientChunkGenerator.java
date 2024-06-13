package xyz.nucleoid.fantasy.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.util.function.Function;

/**
 * A {@link ChunkGenerator} instance that does not know how to be, and does not care to be serialized.
 * This is particularly useful when creating a temporary world with Fantasy.
 * <p>
 * If serialized, however, it will be loaded as a {@link VoidChunkGenerator void world}.
 *
 * @see xyz.nucleoid.fantasy.Fantasy#openTemporaryWorld(RuntimeWorldConfig)
 */
public abstract class TransientChunkGenerator extends ChunkGenerator {
    public static final MapCodec<? extends ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RegistryOps.getEntryCodec(BiomeKeys.THE_VOID)
    ).apply(i, VoidChunkGenerator::new));

    public TransientChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    public TransientChunkGenerator(BiomeSource biomeSource, Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter) {
        super(biomeSource, generationSettingsGetter);
    }

    @Override
    protected final MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }
}
