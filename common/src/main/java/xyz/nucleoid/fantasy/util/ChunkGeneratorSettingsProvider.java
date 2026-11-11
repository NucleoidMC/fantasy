package xyz.nucleoid.fantasy.util;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Allows chunk generators other than noise chunk generators to provide custom chunk generator settings.
 */
public interface ChunkGeneratorSettingsProvider {
    @Nullable
    NoiseGeneratorSettings getSettings();
}
