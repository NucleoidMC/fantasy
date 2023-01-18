package xyz.nucleoid.fantasy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public final class FantasyInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(
                Registry.CHUNK_GENERATOR,
                Fantasy.VOID_CHUNK_GENERATOR,
                VoidChunkGenerator.CODEC
        );
    }
}
