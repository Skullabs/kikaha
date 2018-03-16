package kikaha.urouting.producers;

import io.undertow.server.HttpServerExchange;
import kikaha.urouting.ContextProducer;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed(  ContextProducer.class )
public class HttpServerExchangeProducer implements ContextProducer<HttpServerExchange> {

	@Override
	public HttpServerExchange produce( HttpServerExchange exchange ) {
		return exchange;
	}
}
