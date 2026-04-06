package xyz.nucleoid.fantasy.mixin.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.timeline.Timeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeServerClockManager;

import java.util.Map;
import java.util.Optional;

@Mixin(RegistryLoadTask.class)
public abstract class RegistryLoadTaskMixin<T> {
    @Shadow
    protected abstract <E> ResourceKey<? extends Registry<E>> registryKey();

    @Shadow
    @Final
    private WritableRegistry<T> registry;
    @Unique
    private static Registry<WorldClock> worldClockRegistry;
    @Unique
    private static Registry<Timeline> timelineRegistry;

    @Inject(method = "freezeRegistry", at = @At("HEAD"))
    private void onFreezeRegistry(Map<ResourceKey<?>, Exception> loadingErrors, CallbackInfoReturnable<Boolean> cir) {
        boolean isWorldClock = this.registryKey().equals(Registries.WORLD_CLOCK);
        boolean isTimeline = this.registryKey().equals(Registries.TIMELINE);
        boolean isEither = isWorldClock || isTimeline;

        if (!isEither) {
            return;
        }

        if (isWorldClock) {
            //noinspection unchecked
            worldClockRegistry = (Registry<WorldClock>) this.registry;
            RuntimeServerClockManager.worldClockRegistry = worldClockRegistry;
        } else {
            //noinspection unchecked
            timelineRegistry = (Registry<Timeline>) this.registry;
        }

        Registry<WorldClock> worldClockRegistry = RegistryLoadTaskMixin.worldClockRegistry;
        Registry<Timeline> timelineRegistry = RegistryLoadTaskMixin.timelineRegistry;
        Holder<WorldClock> worldClock;

        if (this.registry.equals(worldClockRegistry) && !worldClockRegistry.getOrThrow(Fantasy.DEFAULT_WORLD_CLOCK).isBound()) {
            worldClock = Registry.registerForHolder(worldClockRegistry, Fantasy.DEFAULT_WORLD_CLOCK, new WorldClock());
        } else {
            Optional<Holder.Reference<WorldClock>> optionalWorldClock = worldClockRegistry.get(Fantasy.DEFAULT_WORLD_CLOCK);
            worldClock = optionalWorldClock.orElseGet(() -> Registry.registerForHolder(worldClockRegistry, Fantasy.DEFAULT_WORLD_CLOCK, new WorldClock()));
        }

        if (this.registry.equals(timelineRegistry) && !timelineRegistry.getOrThrow(Fantasy.DEFAULT_TIMELINE).isBound()) {
            Timeline timeline = RuntimeServerClockManager.createDefaultTimeline(timelineRegistry, worldClock);
            Registry.register(timelineRegistry, Fantasy.DEFAULT_TIMELINE, timeline);
        }
    }
}
