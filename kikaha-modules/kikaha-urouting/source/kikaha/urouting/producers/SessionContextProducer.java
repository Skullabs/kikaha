package kikaha.urouting.producers;

import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.security.SecurityContext;
import kikaha.core.modules.security.Session;
import kikaha.urouting.ContextProducer;
import kikaha.urouting.api.RoutingException;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed(  ContextProducer.class )
public class SessionContextProducer implements ContextProducer<Session> {

	@Override
	public Session produce( HttpServerExchange exchange ) throws RoutingException {
		final SecurityContext context = (SecurityContext)exchange.getSecurityContext();
		return context != null ? context.getCurrentSession() : null;
	}
}
