package kikaha.commons;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
public interface Lang {

	static <O,N> List<N> convert(Iterable<O>originalItems, Function<O, N> converter) {
		final List<N> newItems = new ArrayList<>();
		for ( O original : originalItems )
			newItems.add( converter.apply( original ) );
		return newItems;
	}

	static <T> Optional<T> first( Iterable<T> list, Function<T, Boolean> matcher ) {
		for ( T t : list )
			if ( matcher.apply(t) )
				return Optional.of(t);
		return Optional.empty();
	}

	static <T> List<T> filter( Iterable<T> list, Function<T, Boolean> matcher ) {
		final List<T> newItems = new ArrayList<>();
		for ( T t : list )
			if ( matcher.apply(t) )
				newItems.add( t );
		return newItems;
	}

	static boolean isUndefined(String s) {
		return s == null || s.isEmpty();
	}
}
