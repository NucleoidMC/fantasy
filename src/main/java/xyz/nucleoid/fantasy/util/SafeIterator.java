package xyz.nucleoid.fantasy.util;

import java.util.Collection;
import java.util.Iterator;

public final class SafeIterator<T> implements Iterator<T> {
    private final Object[] values;
    private int index = 0;

    public SafeIterator(Collection<T> source) {
        this.values = source.toArray();
    }

    @Override
    public boolean hasNext() {
        return this.values.length > this.index;
    }

    @Override
    public T next() {
        return (T) this.values[this.index++];
    }
}
