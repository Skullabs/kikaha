package kikaha.urouting;

import io.undertow.server.HttpServerExchange;
import kikaha.urouting.api.RoutingException;

public interface ContextProducer<T> {

	T produce( HttpServerExchange exchange ) throws RoutingException;
}
