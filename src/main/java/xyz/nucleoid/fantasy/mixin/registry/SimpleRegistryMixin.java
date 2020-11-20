package xyz.nucleoid.fantasy.mixin.registry;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin<T> implements RemoveFromRegistry<T> {
    @Shadow
    @Final
    private ObjectList<T> rawIdToEntry;
    @Shadow
    @Final
    private Object2IntMap<T> entryToRawId;
    @Shadow
    @Final
    private BiMap<Identifier, T> idToEntry;
    @Shadow
    @Final
    private BiMap<RegistryKey<T>, T> keyToEntry;
    @Shadow
    @Final
    private Map<T, Lifecycle> entryToLifecycle;

    @Shadow
    protected Object[] randomEntries;

    @Override
    public boolean remove(T entry) {
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        this.idToEntry.inverse().remove(entry);
        this.keyToEntry.inverse().remove(entry);
        this.entryToLifecycle.remove(entry);

        this.rawIdToEntry.set(rawId, null);

        this.randomEntries = null;

        return true;
    }

    @Override
    public boolean remove(Identifier key) {
        T entry = this.idToEntry.get(key);
        return entry != null && this.remove(entry);
    }
}
