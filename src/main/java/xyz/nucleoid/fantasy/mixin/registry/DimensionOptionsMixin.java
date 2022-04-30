package xyz.nucleoid.fantasy.mixin.registry;

import net.minecraft.world.dimension.DimensionOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.nucleoid.fantasy.FantasyDimensionOptions;

@Mixin(DimensionOptions.class)
public class DimensionOptionsMixin implements FantasyDimensionOptions {
    @Unique
    private boolean fantasy$save = true;

    @Override
    public void fantasy$setSave(boolean value) {
        this.fantasy$save = value;
    }

    @Override
    public boolean fantasy$getSave() {
        return this.fantasy$save;
    }
}
