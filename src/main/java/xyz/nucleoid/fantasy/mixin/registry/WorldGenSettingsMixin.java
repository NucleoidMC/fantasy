package xyz.nucleoid.fantasy.mixin.registry;

import com.google.common.collect.Maps;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.level.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.fantasy.FantasyDimensionOptions;

@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {

    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)Lcom/mojang/serialization/DataResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenSettings;<init>(Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)V"), index = 1)
    private static DimensionOptionsRegistryHolder fantasy$wrapWorldGenSettings(DimensionOptionsRegistryHolder original) {
        var dimensions = original.dimensions();
        var saveDimensions = Maps.filterEntries(dimensions, entry -> FantasyDimensionOptions.SAVE_PROPERTIES_PREDICATE.test(entry.getValue()));

        return new DimensionOptionsRegistryHolder(saveDimensions);
    }
}
