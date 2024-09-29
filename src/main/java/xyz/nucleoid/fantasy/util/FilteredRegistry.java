package xyz.nucleoid.fantasy.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredRegistry<T> extends SimpleRegistry<T> {
    private final @NotNull Registry<T> source;
    private final Predicate<T> filter;

    public FilteredRegistry(@NotNull Registry<T> source, Predicate<T> filter) {
        super(source.getKey(), source.getLifecycle());
        this.source = source;
        this.filter = filter;
    }

    public @NotNull Registry<T> getSource() {
        return this.source;
    }

    @Nullable
    @Override
    public Identifier getId(T value) {
        return this.filter.test(value) ? this.source.getId(value) : null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return this.filter.test(entry) ? this.source.getKey(entry) : Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        return this.filter.test(value) ? this.source.getRawId(value) : -1;
    }

    @Nullable
    @Override
    public T get(int index) {
        return this.source.get(index);
    }

    @Override
    public int size() {
        return this.source.size();
    }

    @Nullable
    @Override
    public T get(@Nullable RegistryKey<T> key) {
        return this.source.get(key);
    }

    @Nullable
    @Override
    public T get(@Nullable Identifier id) {
        return this.source.get(id);
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.source.getLifecycle();
    }

    @Override
    public Set<Identifier> getIds() {
        return this.source.getIds();
    }

    @Override
    public @NotNull Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        Set<Map.Entry<RegistryKey<T>, T>> set = new HashSet<>();
        for (Map.Entry<RegistryKey<T>, T> e : this.source.getEntrySet()) {
            if (this.filter.test(e.getValue())) {
                set.add(e);
            }
        }
        return set;
    }

    @Override
    public @Nullable Set<RegistryKey<T>> getKeys() {
        return null;
    }

    @Override
    public @NotNull Optional<RegistryEntry.Reference<T>> getRandom(net.minecraft.util.math.random.Random random) {
        return Optional.empty();
    }

    @Override
    public boolean containsId(Identifier id) {
        return this.source.containsId(id);
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return this.source.contains(key);
    }

    @Override
    public @NotNull Registry<T> freeze() {
        return this;
    }

    @Override
    public RegistryEntry.@Nullable Reference<T> createEntry(T value) {
        return null;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return this.source.getEntry(rawId);
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key) {
        return this.source.getEntry(key);
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return this.source.streamEntries().filter((e) -> this.filter.test(e.value()));
    }

    @Override
    public @NotNull Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return Optional.empty();
    }

    @Override
    public RegistryEntryList.@Nullable Named<T> getOrCreateEntryList(TagKey<T> tag) {
        return null;
    }

    @Override
    public @Nullable Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return null;
    }

    @Override
    public @Nullable Stream<TagKey<T>> streamTags() {
        // no-op
        return null;
    }

    @Override
    public void clearTags() {
        // no-op
    }

    @Override
    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {
        // no-op
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.source.stream().filter(this.filter).iterator();
    }
}
