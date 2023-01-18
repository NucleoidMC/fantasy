package xyz.nucleoid.fantasy.mixin.registry;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.fantasy.FantasyDimensionOptions;
import xyz.nucleoid.fantasy.util.FilteredRegistry;

import java.util.function.Function;

@Mixin(GeneratorOptions.class)
public class GeneratorOptionsMixin {
    @ModifyArg(method = "method_28606", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/MapCodec;forGetter(Ljava/util/function/Function;)Lcom/mojang/serialization/codecs/RecordCodecBuilder;", ordinal = 3))
    private static Function<GeneratorOptions, Registry<DimensionOptions>> fantasy$wrapRegistry(Function<GeneratorOptions, Registry<DimensionOptions>> getter) {
        return (e) -> new FilteredRegistry<>(e.getDimensions(), FantasyDimensionOptions.SAVE_PREDICATE);
    }
}
