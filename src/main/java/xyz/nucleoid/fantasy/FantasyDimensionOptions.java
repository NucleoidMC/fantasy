package xyz.nucleoid.fantasy;

import net.minecraft.world.dimension.DimensionOptions;

import java.util.function.Predicate;

public interface FantasyDimensionOptions {
    Predicate<DimensionOptions> SAVE_PREDICATE = (e) -> ((FantasyDimensionOptions) (Object) e).fantasy$getSave();

    void fantasy$setSave(boolean value);
    boolean fantasy$getSave();
}
