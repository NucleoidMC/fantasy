package xyz.nucleoid.fantasy;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RemoveFromRegistry<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(MappedRegistry<T> registry, Identifier key) {
        return ((RemoveFromRegistry<T>) registry).fantasy$remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(MappedRegistry<T> registry, T value) {
        return ((RemoveFromRegistry<T>) registry).fantasy$remove(value);
    }

    @SuppressWarnings("unchecked")
    static <T> RegistryRemoval thaw(Registry<T> registry) {
        RemoveFromRegistry<T> registry1 = ((RemoveFromRegistry<T>) registry);
        boolean priorStateOfMatter = registry1.fantasy$isFrozen();
        registry1.fantasy$setFrozen(false);
        return () -> registry1.fantasy$setFrozen(priorStateOfMatter);
    }

    boolean fantasy$remove(T value);

    boolean fantasy$remove(Identifier key);

    void fantasy$setFrozen(boolean value);

    boolean fantasy$isFrozen();

    @ApiStatus.NonExtendable
    @FunctionalInterface
    interface RegistryRemoval extends AutoCloseable {
        @Override
        void close();
    }
}
