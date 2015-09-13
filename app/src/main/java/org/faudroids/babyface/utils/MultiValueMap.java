package org.faudroids.babyface.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map which allows multiple values per key.
 */
public class MultiValueMap<K, V> {

	private final Map<K, List<V>> map = new HashMap<>();

	public void put(K key, V value) {
		List<V> valuesList = get(key);
		valuesList.add(value);
		map.put(key, valuesList);
	}

	public void put(K key, Collection<V> values) {
		List<V> valuesList = get(key);
		valuesList.addAll(values);
		map.put(key, valuesList);
	}

	public List<V> get(K key) {
		List<V> valuesList = map.get(key);
		if (valuesList == null) valuesList = new ArrayList<>();
		return valuesList;
	}

	public int size() {
		return map.size();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

}
