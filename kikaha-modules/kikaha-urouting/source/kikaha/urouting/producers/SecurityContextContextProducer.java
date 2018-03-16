package kikaha.urouting.producers;

import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.security.SecurityContext;
import kikaha.urouting.ContextProducer;
import kikaha.urouting.api.RoutingException;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed(  ContextProducer.class )
public class SecurityContextContextProducer implements ContextProducer<SecurityContext> {

	@Override
	public SecurityContext produce( HttpServerExchange exchange ) throws RoutingException {
		return (SecurityContext)exchange.getSecurityContext();
	}
}
