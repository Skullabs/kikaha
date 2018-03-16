package kikaha.commons;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Mutable non-thread-safe {@link Map} implementation that only holds the last defined value.
 *
 * Created by miere.teixeira on 18/06/2017.
 */
@Getter
public class LastValueOnlyMap<K,V> implements Map<K,V> {

    V value;

    @Override
    public int size() { return 1; }

    @Override
    public boolean isEmpty() { return value != null; }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value != null && this.value.equals(value);
    }

    @Override
    public V get(Object key) {
        return containsKey( key ) ? value : null;
    }

    @Override
    public V put(K key, V value) {
        this.value = value;
        return value;
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if ( m!= null )
            m.forEach((key1, value1) -> put(key1, value1));
    }

    @Override
    public void clear() {
        this.value = null;
    }

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
        return Collections.singletonList( value );
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return isEmpty()
            ? Collections.emptySet()
            : Collections.singleton( Tuple.of( null, value ) );
    }
}
