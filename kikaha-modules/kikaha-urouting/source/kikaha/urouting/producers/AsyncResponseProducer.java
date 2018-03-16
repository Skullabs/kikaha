package kikaha.urouting.producers;

import javax.inject.*;
import io.undertow.server.HttpServerExchange;
import kikaha.urouting.*;
import kikaha.urouting.api.*;

/**
 *
 */
@Singleton
public class AsyncResponseProducer implements ContextProducer<AsyncResponse> {

	@Inject RoutingMethodResponseWriter responseWriter;
	@Inject RoutingMethodExceptionHandler exceptionHandler;

	@Override
	public AsyncResponse produce( HttpServerExchange exchange ) throws RoutingException {
		return new DefaultAsyncResponse( exchange, responseWriter, exceptionHandler );
	}
}
