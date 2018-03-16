package kikaha.urouting.unit.samples;

import kikaha.urouting.ContextProducer;
import io.undertow.server.HttpServerExchange;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed( ContextProducer.class )
public class StringProducer implements ContextProducer<String> {

	public static final String HELLO_WORLD = "Hello World";

	@Override
	public String produce( HttpServerExchange exchange ) {
		return HELLO_WORLD;
	}
}
