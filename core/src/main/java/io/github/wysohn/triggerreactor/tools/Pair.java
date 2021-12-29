package io.github.wysohn.triggerreactor.tools;

import java.util.Objects;

public class Pair<K, V> {
    public final K key;
    public final V value;

    private Pair(K key, V value) {
        ValidationUtil.notNull(key);
        ValidationUtil.notNull(value);

        this.key = key;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) && value.equals(pair.value);
    }

    @Override
    public String toString() {
        return "Pair{key=" + key + ", value=" + value + '}';
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}