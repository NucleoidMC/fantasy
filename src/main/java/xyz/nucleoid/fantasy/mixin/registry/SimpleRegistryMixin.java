package xyz.nucleoid.fantasy.mixin.registry;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.registry.MutableRegistry;
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
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemoveFromRegistry<T>, MutableRegistry<T> {
    @Unique private static final Logger fantasy$LOGGER = LogUtils.getLogger();

    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;
    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;
    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow @Final private RegistryKey<? extends Registry<T>> key;
    @Shadow private boolean frozen;

    @Override
    public boolean fantasy$remove(T entry) {
        var registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.keyToEntry.remove(registryEntry.registryKey());
            this.idToEntry.remove(registryEntry.registryKey().getValue());
            this.valueToEntry.remove(entry);
            this.rawIdToEntry.set(rawId, null);
            this.keyToEntryInfo.remove(this.key);

            return true;
        } catch (Throwable e) {
            fantasy$LOGGER.error("Could not remove entry", e);
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

    @ModifyReturnValue(method = "streamEntries", at = @At("RETURN"))
    public Stream<RegistryEntry.Reference<T>> fixEntryStream(Stream<RegistryEntry.Reference<T>> original) {
        return original.filter(Objects::nonNull);
    }
}
