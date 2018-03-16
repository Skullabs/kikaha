package kikaha.urouting.producers;

import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import kikaha.urouting.ContextProducer;
import kikaha.urouting.api.RoutingException;
import lombok.val;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed(  ContextProducer.class )
public class AccountProducer implements ContextProducer<Account> {

	@Override
	public Account produce( final HttpServerExchange exchange ) throws RoutingException {
		val securityContext = exchange.getSecurityContext();
		if ( securityContext == null )
			return null;
		return securityContext.getAuthenticatedAccount();
	}
}
