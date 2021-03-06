package kikaha.urouting.api;

import io.undertow.server.HttpServerExchange;

public interface ContextProducer<T> {

	T produce( HttpServerExchange exchange ) throws RoutingException;
}
