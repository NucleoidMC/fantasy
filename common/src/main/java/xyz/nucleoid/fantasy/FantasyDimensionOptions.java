package xyz.nucleoid.fantasy;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;
import net.minecraft.world.level.dimension.LevelStem;

@ApiStatus.Internal
public interface FantasyDimensionOptions {
    Predicate<LevelStem> SAVE_PREDICATE = (e) -> ((FantasyDimensionOptions) (Object) e).fantasy$getSave();
    Predicate<LevelStem> SAVE_PROPERTIES_PREDICATE = (e) -> ((FantasyDimensionOptions) (Object) e).fantasy$getSaveProperties();

    void fantasy$setSave(boolean value);
    boolean fantasy$getSave();
    void fantasy$setSaveProperties(boolean value);
    boolean fantasy$getSaveProperties();
}
