package xyz.nucleoid.fantasy.util;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Allows chunk generators other than noise chunk generators to provide custom chunk generator settings.
 */
public interface ChunkGeneratorSettingsProvider {
    @Nullable
    ChunkGeneratorSettings getSettings();
}
