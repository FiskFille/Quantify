package com.fiskmods.quantify.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

public interface IndexMap<K> {
    int size();

    int get(K key);

    int getOrDefault(K key, int defaultValue);

    boolean contains(K key);

    Collection<K> keys();

    IntStream values();

    void forEach(ObjIntConsumer<? super K> action);

    static <K> IndexMap<K> of(final Map<K, ? extends Number> map) {
        return new MapBacked<>(map);
    }

    static <K> IndexMap<K> of(final List<K> list) {
        return new ListBacked<>(list);
    }

    final class MapBacked<K> implements IndexMap<K> {
        private final Map<K, ? extends Number> map;

        private MapBacked(final Map<K, ? extends Number> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public int get(final K key) {
            return getOrDefault(key, -1);
        }

        @Override
        public int getOrDefault(final K key, final int defaultValue) {
            final Number n = map.get(key);
            return n != null ? n.intValue() : defaultValue;
        }

        @Override
        public boolean contains(final K key) {
            return map.containsKey(key);
        }

        @Override
        public Collection<K> keys() {
            return map.keySet();
        }

        @Override
        public IntStream values() {
            return map.values().stream().mapToInt(Number::intValue);
        }

        @Override
        public void forEach(final ObjIntConsumer<? super K> action) {
            map.forEach((k, v) -> action.accept(k, v.intValue()));
        }
    }

    final class ListBacked<K> implements IndexMap<K> {
        private final List<K> list;

        private ListBacked(final List<K> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public int get(final K key) {
            return list.indexOf(key);
        }

        @Override
        public int getOrDefault(final K key, final int defaultValue) {
            final int index = get(key);
            return index == -1 ? defaultValue : index;
        }

        @Override
        public boolean contains(final K key) {
            return list.contains(key);
        }

        @Override
        public Collection<K> keys() {
            return list;
        }

        @Override
        public IntStream values() {
            return IntStream.range(0, list.size());
        }

        @Override
        public void forEach(final ObjIntConsumer<? super K> action) {
            for (int i = 0; i < list.size(); i++) {
                action.accept(list.get(i), i);
            }
        }
    }
}
