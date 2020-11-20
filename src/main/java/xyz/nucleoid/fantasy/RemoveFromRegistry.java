package xyz.nucleoid.fantasy;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;

public interface RemoveFromRegistry<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, Identifier key) {
        return ((RemoveFromRegistry<T>) registry).remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, T value) {
        return ((RemoveFromRegistry<T>) registry).remove(value);
    }

    boolean remove(T value);

    boolean remove(Identifier key);
}
