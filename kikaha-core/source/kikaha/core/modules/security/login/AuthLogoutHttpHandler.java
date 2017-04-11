package kikaha.core.modules.security.login;

import javax.inject.Inject;
import io.undertow.server.*;
import io.undertow.util.*;
import kikaha.core.modules.security.*;
import kikaha.core.util.Redirect;

/**
 *
 */
public class AuthLogoutHttpHandler implements HttpHandler {

	final static String NOT_LOGGED_IN = "Not logged in";

	@Inject FormAuthenticationConfiguration formAuthenticationConfiguration;

	@Override
	public void handleRequest( HttpServerExchange exchange ) throws Exception {
		final SecurityContext securityContext = (SecurityContext)exchange.getSecurityContext();
		if ( securityContext == null ) {
			exchange.setStatusCode( StatusCodes.INTERNAL_SERVER_ERROR );
			exchange.getResponseSender().send( NOT_LOGGED_IN );
		} else {
			securityContext.logout();
			if ( Methods.GET.equals( exchange.getRequestMethod() ) )
				Redirect.to( exchange, formAuthenticationConfiguration.getLoginPage() );
			else
				exchange.endExchange();
		}
	}
}
