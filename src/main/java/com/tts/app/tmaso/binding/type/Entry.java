package com.tts.app.tmaso.binding.type;

public class Entry<K, V> {
    private K key;
    private V value;

    public Entry(K key, V obj) {
        this.key = key;
        this.value = obj;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
