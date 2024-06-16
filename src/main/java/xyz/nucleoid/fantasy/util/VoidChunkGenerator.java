package xyz.nucleoid.fantasy.util;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final MapCodec<VoidChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.REGISTRY_CODEC.stable().fieldOf("biome").forGetter(VoidChunkGenerator::getBiome)
    ).apply(instance, instance.stable(VoidChunkGenerator::new)));

    private static final VerticalBlockSample EMPTY_SAMPLE = new VerticalBlockSample(0, new BlockState[0]);

    private final RegistryEntry<Biome> biome;

    public static final DensityFunction ZERO_DENSITY_FUNCTION = new DensityFunction() {
        @Override
        public double sample(NoisePos pos) {
            return 0;
        }

        @Override
        public void fill(double[] ds, EachApplier arg) { }

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
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CodecHolder.of(MapCodec.unit(this));
        }
    };

    public static final MultiNoiseUtil.MultiNoiseSampler EMPTY_SAMPLER = new MultiNoiseUtil.MultiNoiseSampler(ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, ZERO_DENSITY_FUNCTION, Collections.emptyList());

    public VoidChunkGenerator(RegistryEntry<Biome> biome) {
        super(new FixedBiomeSource(biome));
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
        this(biomeRegistry.getEntry(biome).orElseThrow());
    }

    // Create an empty (void) world!
    public VoidChunkGenerator(MinecraftServer server) {
        this(server.getRegistryManager().get(RegistryKeys.BIOME), BiomeKeys.THE_VOID);
    }

    // Create a world with a given Biome (as an ID)
    public VoidChunkGenerator(MinecraftServer server, Identifier biome) {
        this(server, RegistryKey.of(RegistryKeys.BIOME, biome));
    }

    // Create a world with a given Biome (as a RegistryKey)
    public VoidChunkGenerator(MinecraftServer server, RegistryKey<Biome> biome) {
        this(server.getRegistryManager().get(RegistryKeys.BIOME), biome);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    protected RegistryEntry<Biome> getBiome() {
        return this.biome;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess world, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
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
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return EMPTY_SAMPLE;
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {

    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {

    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getWorldHeight() {
        return 0;
    }

    @Nullable
    @Override
    public Pair<BlockPos, RegistryEntry<Structure>> locateStructure(ServerWorld world, RegistryEntryList<Structure> structures, BlockPos center, int radius, boolean skipReferencedStructures) {
        return null;
    }

    @Override
    public Pool<SpawnSettings.SpawnEntry> getEntitySpawnList(RegistryEntry<Biome> biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
        return Pool.empty();
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, StructurePlacementCalculator placementCalculator, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager) {

    }

    @Override
    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry, NoiseConfig noiseConfig, long seed) {
        return StructurePlacementCalculator.create(noiseConfig, seed, biomeSource, Stream.empty());
    }
}
