package xyz.nucleoid.fantasy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.util.TransientChunkGenerator;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public final class FantasyInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(Fantasy.ID, "void"), VoidChunkGenerator.CODEC);
        Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(Fantasy.ID, "transient"), TransientChunkGenerator.CODEC);
    }
}
