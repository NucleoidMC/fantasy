package xyz.nucleoid.fantasy.mixin.registry;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemoveFromRegistry<T> {
    @Unique private static final Logger fantasy$LOGGER = LogUtils.getLogger();

    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    // @Shadow @Final private Map<T, Lifecycle> entryToLifecycle;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;

    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow @Final private Reference2IntMap<T> entryToRawId;

    @Shadow private boolean frozen;

    //@Shadow @Nullable private List<RegistryEntry.Reference<T>> cachedEntries;

    @Shadow @Final RegistryKey<? extends Registry<T>> key;

    @Override
    public boolean fantasy$remove(T entry) {
        var registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.rawIdToEntry.set(rawId, null);
            this.idToEntry.remove(registryEntry.registryKey().getValue());
            this.keyToEntry.remove(registryEntry.registryKey());
            this.keyToEntryInfo.remove(this.key);
            this.valueToEntry.remove(entry);
            /*if (this.cachedEntries != null) {
                this.cachedEntries.remove(registryEntry);
            }*/

            return true;
        } catch (Throwable e) {
            fantasy$LOGGER.error("Fantasy: Could not remove entry", e);
            return false;
        }
    }

    @Override
    public boolean fantasy$remove(Identifier key) {
        var entry = this.idToEntry.get(key);
        return entry != null && entry.hasKeyAndValue() && this.fantasy$remove(entry.value());
    }

    @Override
    public void fantasy$setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean fantasy$isFrozen() {
        return this.frozen;
    }
}
