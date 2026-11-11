package xyz.nucleoid.fantasy.mixin.registry;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
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
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements RemoveFromRegistry<T>, WritableRegistry<T> {
    @Unique private static final Logger fantasy$LOGGER = LogUtils.getLogger();

    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private Map<Identifier, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final private ResourceKey<? extends Registry<T>> key;
    @Shadow private boolean frozen;

    @Override
    public boolean fantasy$remove(T entry) {
        var registryEntry = this.byValue.get(entry);
        int rawId = this.toId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.byKey.remove(registryEntry.key());
            this.byLocation.remove(registryEntry.key().identifier());
            this.byValue.remove(entry);
            this.byId.set(rawId, null);
            this.registrationInfos.remove(this.key);

            return true;
        } catch (Throwable e) {
            fantasy$LOGGER.error("Could not remove entry", e);
            return false;
        }
    }

    @Override
    public boolean fantasy$remove(Identifier key) {
        var entry = this.byLocation.get(key);
        return entry != null && entry.isBound() && this.fantasy$remove(entry.value());
    }

    @Override
    public void fantasy$setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean fantasy$isFrozen() {
        return this.frozen;
    }

    @ModifyReturnValue(method = "listElements", at = @At("RETURN"))
    public Stream<Holder.Reference<T>> fixEntryStream(Stream<Holder.Reference<T>> original) {
        return original.filter(Objects::nonNull);
    }
}
