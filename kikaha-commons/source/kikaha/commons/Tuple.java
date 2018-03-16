package kikaha.commons;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by ronei.gebert on 07/06/2017.
 */
@Value
@RequiredArgsConstructor(staticName = "of")
public class Tuple<K,V> implements Serializable, Map.Entry<K,V> {
    final K first;
    final V second;

    public static <K,V> Tuple<K,V> empty(){
        return new Tuple<>(null, null);
    }

    @Override
    public K getKey() {
        return first;
    }

    @Override
    public V getValue() {
        return second;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException( "This Entry is immutable" );
    }
}
