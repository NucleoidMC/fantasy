package xyz.nucleoid.fantasy.util;

import com.google.common.collect.Iterators;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredRegistry<T> extends SimpleRegistry<T> {
    private final Registry<T> source;
    private final Predicate<T> check;

    public FilteredRegistry(Registry<T> source, Predicate<T> check) {
        super(source.getKey(), source.getLifecycle());
        this.source = source;
        this.check = check;
    }

    public Registry<T> getSource() {
        return this.source;
    }

    @Nullable
    @Override
    public Identifier getId(T value) {
        return check.test(value) ? this.source.getId(value) : null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return check.test(entry) ? this.source.getKey(entry) : Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        return check.test(value) ? this.source.getRawId(value) : -1;
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
        return this.get(id);
    }

    @Override
    public Lifecycle getEntryLifecycle(T entry) {
        return this.source.getEntryLifecycle(entry);
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.source.getLifecycle();
    }

    @Override
    public Set<Identifier> getIds() {
        return this.getIds();
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        Set<Map.Entry<RegistryKey<T>, T>> set = new HashSet<>();
        for (Map.Entry<RegistryKey<T>, T> e : this.source.getEntrySet()) {
            if (this.check.test(e.getValue())) {
                set.add(e);
            }
        }
        return set;
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return null;
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(net.minecraft.util.math.random.Random random) {
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
    public Registry<T> freeze() {
        return this;
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
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
        return this.source.streamEntries().filter((e) -> this.check.test(e.value()));
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return Optional.empty();
    }

    @Override
    public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> tag) {
        return null;
    }

    @Override
    public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return null;
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return null;
    }

    @Override
    public void clearTags() {

    }

    @Override
    public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {

    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.source.iterator(), e -> this.check.test(e));
    }
}
