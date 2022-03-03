package xyz.nucleoid.fantasy.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Biome.REGISTRY_CODEC.stable().fieldOf("biome").forGetter(g -> g.biome)
        ).apply(instance, instance.stable(VoidChunkGenerator::new));
    });

    private static final VerticalBlockSample EMPTY_SAMPLE = new VerticalBlockSample(0, new BlockState[0]);

    private final RegistryEntry<Biome> biome;

    private static final Registry<StructureSet> EMPTY_STRUCTURE_REGISTRY = new SimpleRegistry<>(Registry.STRUCTURE_SET_KEY, Lifecycle.stable(), (x) -> null).freeze();

    public static final DensityFunction ZERO_DENSITY_FUNCTION = new DensityFunction() {
        @Override
        public double sample(NoisePos pos) {
            return 0;
        }

        @Override
        public void method_40470(double[] ds, class_6911 arg) { }

        @Override
        public DensityFunction apply(DensityFunctionVisitor visitor) {
            return this;
        }

        @Override
        public double minValue() {
            return 0;
        }

        @Override
        public double maxValue() {
            return 0;
        }

        @Override
        public Codec<? extends DensityFunction> getCodec() {
            return Codec.unit(this);
        }
    };

    public static final MultiNoiseUtil.MultiNoiseSampler EMPTY_SAMPLER = new MultiNoiseUtil.MultiNoiseSampler(ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, Collections.emptyList());

    public VoidChunkGenerator(RegistryEntry<Biome> biome) {
        super(EMPTY_STRUCTURE_REGISTRY, Optional.empty(), new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Deprecated
    public VoidChunkGenerator(Supplier<Biome> biome) {
        this(RegistryEntry.of(biome.get()));
    }

    public VoidChunkGenerator(Registry<Biome> biomeRegistry) {
        this(biomeRegistry, BiomeKeys.THE_VOID);
    }

    public VoidChunkGenerator(Registry<Biome> biomeRegistry, RegistryKey<Biome> biome) {
        this(biomeRegistry.getEntry(biome).get());
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler() {
        return EMPTY_SAMPLER;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver generationStep) {

    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getWorldHeight() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        return EMPTY_SAMPLE;
    }

    @Override
    public void getDebugHudText(List<String> text, BlockPos pos) {

    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, Chunk chunk) {

    }
}
