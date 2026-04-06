package xyz.nucleoid.fantasy.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.timeline.AttributeTrack;
import net.minecraft.world.timeline.Timeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Optional;

@Mixin(Timeline.class)
public interface TimelineAccess {
    @Invoker("<init>")
    static Timeline init(
            final Holder<WorldClock> clock,
            final Optional<Integer> periodTicks,
            final Map<EnvironmentAttribute<?>, AttributeTrack<?, ?>> tracks,
            final Map<ResourceKey<ClockTimeMarker>, Timeline.TimeMarkerInfo> timeMarkers
    ) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    @Accessor
    Optional<Integer> getPeriodTicks();

    @Accessor
    Map<EnvironmentAttribute<?>, AttributeTrack<?, ?>> getTracks();

    @Accessor
    Map<ResourceKey<ClockTimeMarker>, Timeline.TimeMarkerInfo> getTimeMarkers();
}
