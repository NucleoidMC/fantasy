package xyz.nucleoid.fantasy.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import org.spongepowered.asm.mixin.Mixin;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeServerClockManager;

import java.util.Optional;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    @WrapMethod(method = "timelines")
    private HolderSet<Timeline> fixTimelines(Operation<HolderSet<Timeline>> original) {
        HolderSet<Timeline> originalTimelines = original.call();
        Optional<TagKey<Timeline>> left = originalTimelines.unwrap().left();

        if (left.isPresent() && left.get().equals(Fantasy.DEFAULT_TIMELINES)) {
            Holder<Timeline>[] holders =
                    RuntimeServerClockManager.DIMENSION_TYPE_2_TIMELINES.get((DimensionType) (Object) this);

            if (holders == null) {
                return originalTimelines;
            }

            return HolderSet.direct(holders);
        } else {
            return originalTimelines;
        }
    }

    @WrapMethod(method = "defaultClock")
    private Optional<Holder<WorldClock>> fixDefaultClock(Operation<Optional<Holder<WorldClock>>> original) {
        Optional<Holder<WorldClock>> worldClock = original.call();

        if (worldClock.isPresent() && worldClock.get().is(Fantasy.DEFAULT_WORLD_CLOCK)) {
            return Optional.ofNullable(RuntimeServerClockManager.DIMENSION_TYPE_2_WORLD_CLOCKS.get((DimensionType) (Object) this));
        } else {
            return worldClock;
        }
    }
}
