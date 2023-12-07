package xyz.nucleoid.fantasy.mixin.registry;

import net.minecraft.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.fantasy.FantasyDimensionOptions;
import xyz.nucleoid.fantasy.util.FilteredRegistry;

import java.util.function.Function;

@Mixin(DimensionOptionsRegistryHolder.class)
public class DimensionOptionsRegistryHolderMixin {
    @ModifyArg(method = "method_45516", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/MapCodec;forGetter(Ljava/util/function/Function;)Lcom/mojang/serialization/codecs/RecordCodecBuilder;"))
    private static Function<Object, Registry<DimensionOptions>> fantasy$swapRegistryGetter(Function<Object, Registry<DimensionOptions>> getter) {
        return (x) -> new FilteredRegistry<>(getter.apply(x), FantasyDimensionOptions.SAVE_PROPERTIES_PREDICATE);
    }
}
