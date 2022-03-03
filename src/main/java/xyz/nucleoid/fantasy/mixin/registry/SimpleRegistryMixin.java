package xyz.nucleoid.fantasy.mixin.registry;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemoveFromRegistry<T> {

    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Shadow @Nullable private Map<T, RegistryEntry.Reference<T>> unfrozenValueToEntry;

    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    @Shadow @Final private Map<T, Lifecycle> entryToLifecycle;

    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow @Final private Object2IntMap<T> entryToRawId;

    @Shadow public abstract Optional<RegistryEntry<T>> getEntry(int rawId);

    @Shadow private boolean frozen;

    @Shadow @Nullable private List<RegistryEntry.Reference<T>> cachedEntries;

    @Override
    public boolean remove(T entry) {
        var registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        this.rawIdToEntry.set(rawId, null);
        this.idToEntry.remove(registryEntry);
        this.keyToEntry.remove(registryEntry);
        this.entryToLifecycle.remove(entry);
        this.valueToEntry.remove(entry);
        if (this.cachedEntries != null) {
            this.cachedEntries.remove(registryEntry);
        }
        if (this.unfrozenValueToEntry != null) {
            this.unfrozenValueToEntry.remove(entry);
        }

        return true;
    }

    @Override
    public boolean remove(Identifier key) {
        var entry = this.idToEntry.get(key);
        return entry != null && entry.hasKeyAndValue() && this.remove(entry.value());
    }

    @Override
    public void setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean isFrozen() {
        return this.frozen;
    }
}
