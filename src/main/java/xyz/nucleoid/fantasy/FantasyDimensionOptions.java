package xyz.nucleoid.fantasy;

import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.Internal
public interface FantasyDimensionOptions {
    Predicate<DimensionOptions> SAVE_PREDICATE = (e) -> ((FantasyDimensionOptions) (Object) e).fantasy$getSave();
    Predicate<DimensionOptions> SAVE_PROPERTIES_PREDICATE = (e) -> ((FantasyDimensionOptions) (Object) e).fantasy$getSaveProperties();

    void fantasy$setSave(boolean value);
    boolean fantasy$getSave();
    void fantasy$setSaveProperties(boolean value);
    boolean fantasy$getSaveProperties();
}
