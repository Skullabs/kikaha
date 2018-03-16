package kikaha.commons;

import java.util.*;
import lombok.experimental.Delegate;

/**
 * Convenient Map implementation.
 */
public class ChainedMap<K,V> implements Map<K,V> {

	@Delegate
	final Map<K,V> internalMap = new HashMap<>();

	public ChainedMap<K,V> set( K key, V value ) {
		internalMap.put( key, value );
		return this;
	}

	public ChainedMap<K,V> and( K key, V value ) {
		return set( key, value );
	}

	public static <K,V> ChainedMap<K,V> with( K key, V value ) {
		return new ChainedMap<K,V>().and( key, value );
	}
}
