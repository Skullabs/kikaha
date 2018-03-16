package kikaha.urouting;

import java.util.Map;

import kikaha.urouting.api.RoutingException;
import lombok.RequiredArgsConstructor;

@SuppressWarnings( { "rawtypes", "unchecked" } )
@RequiredArgsConstructor
public class ContextProducerFactory {

	final Map<Class, ContextProducer> producers;

	public <T> ContextProducer<T> producerFor( final Class<T> clazz ) throws RoutingException {
		return producers.get( clazz );
	}

	/**
	 * Will register a producer if no producer was already registered.
	 *
	 * @param clazz
	 * @param producer
	 * @param <T>
 	 * @return {@code true} if the producer was registered.
	 */
	synchronized public <T> boolean registerProducer( final Class<T> clazz, final ContextProducer<T> producer ) {
		return producers.putIfAbsent( clazz, producer ) == null;
	}
}